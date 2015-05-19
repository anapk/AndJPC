package uk.co.jads.android.jpc;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.jpc.emulator.PC;
import org.jpc.emulator.pci.peripheral.DefaultVGACard;
import org.jpc.emulator.pci.peripheral.VGACard;
import org.jpc.interop.IPCMonitor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class PCMonitor extends View implements IPCMonitor {
    @NonNull
    private final Paint solidPaint;
    private Bitmap bmp;
    private OnScreenButtons overlay;
    private PC pc;
    private Updater updater;
    private DefaultVGACard vgaCard;

    @Override
    public void keyPressed(int keyCode) {

    }

    @Override
    public void keyReleased(int keyCode) {

    }

    @Override
    public void mouseEventReceived(int dx, int dy, int dz, int buttons) {

    }

    @Override
    public void scaleDisplay(int width, int height) {
        System.out.println("Scale display");
    }

    @Override
    public File saveScreenshot() {
        File baseddir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File out = null;
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(out = new File(baseddir.getPath() + "/Screenshot.png"));
            os.write(0);
            os.close();
            os = new FileOutputStream(out);
        } catch (FileNotFoundException e) {
            out = null;
        } catch (IOException e) {
            out = null;
        }
        if (out == null) throw new RuntimeException("cant get external media dir");
        assert os != null;
        System.out.println("bmp width " + bmp.getWidth());
        System.out.println("bmp height " + bmp.getHeight());
        try {
            bmp.compress(Bitmap.CompressFormat.PNG, 50, os);
        } finally {
            if (os != null) try {
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return baseddir;
    }

    Context context;

    public PCMonitor(final Context context) {
        super(context);
        solidPaint = new Paint();
        this.context = context;
    }

    public PCMonitor(@NonNull final Context context, final AttributeSet set) {
        super(context, set);
        solidPaint = new Paint();
        this.context = context;
    }

    protected PC getPC() {
        return this.pc;
    }

    public void setPC(@NonNull final PC pc) {
        this.pc = pc;
        solidPaint.setStyle(Paint.Style.FILL);
        solidPaint.setARGB(255, 255, 255, 255);
        (vgaCard = (DefaultVGACard) pc.getComponent(VGACard.class)).setMonitor(this);
        vgaCard.resizeDisplay(640, 480);
    }

    @Override
    public boolean isRunning() {
        synchronized (this) {
            return updater != null && updater.running;
        }
    }

    public void loadState(@NonNull InputStream in) throws IOException {
        DataInputStream input = new DataInputStream(in);
        int len = input.readInt();
        int[] rawImageData = vgaCard.getDisplayBuffer();
        if (len != rawImageData.length) {
            throw new IOException("Image size not consistent with saved image state");
        }
        byte[] dummy = new byte[len * 4];
        input.readFully(dummy);
        for (int i = 0, j = 0; i < len; i++) {
            int val = 0;
            val |= (0xff & dummy[j++]) << 24;
            val |= (0xff & dummy[j++]) << 16;
            val |= (0xff & dummy[j++]) << 8;
            val |= 0xff & dummy[j++];

            rawImageData[i] = val;
        }
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        if (vgaCard != null) {
            final int width = vgaCard.getDisplaySize().width;
            final int height = vgaCard.getDisplaySize().height;
            if (width * height > 0) {
                if (false && rand.nextDouble() < 0.05d) {
                    byte[] bytes = ByteBuffer.allocate(height * width * 4).array();
                    rand.nextBytes(bytes);
                    IntBuffer intBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                    intBuf.get(vgaCard.getDisplayBuffer());
                }
                int[] arr = vgaCard.getDisplayBuffer();
                for (int i=0; i<arr.length; i++) {
                    arr[i] |= 0xff000000;
                }

                try {
                    bmp.setPixels(arr, 0, width, 0, 0, width, height);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                canvas.drawBitmap(bmp, 0.0f, 0.0f, solidPaint);
                System.out.println("drew bitmap");
            } else {
                System.out.println("didn't draw, h/w zero");
            }
            if (overlay != null) {
                overlay.onDraw(canvas);
            }
        } else {
            System.out.println("vgaCard null");
        }
    }

    Random rand = new Random();

    @Override
    public void resizeDisplay(final int width, final int height) {
        if (vgaCard == null || width == 0 || height == 0) {
            System.out.println("Not resizing");
            return;
        }
        vgaCard.setDisplayBuffer(new int[width * height], width, height);
        Arrays.fill(vgaCard.getDisplayBuffer(), Integer.MAX_VALUE);
        if (vgaCard.getDisplaySize().width != width) {
            System.out.println(String.format("displaysize %d width %d", vgaCard.getDisplaySize().width, width));
        }
        bmp = Bitmap.createBitmap(vgaCard.getDisplaySize().width, vgaCard.getDisplaySize().height, Bitmap.Config.ARGB_8888);
        postInvalidate();
    }

    public void saveState(@NonNull OutputStream out) throws IOException {
        int[] rawImageData = vgaCard.getDisplayBuffer();
        byte[] dummy = new byte[rawImageData.length * 4];
        for (int i = 0, j = 0; i < rawImageData.length; i++) {
            int val = rawImageData[i];
            dummy[j++] = (byte) (val >> 24);
            dummy[j++] = (byte) (val >> 16);
            dummy[j++] = (byte) (val >> 8);
            dummy[j++] = (byte) (val);
        }

        DataOutputStream output = new DataOutputStream(out);
        output.writeInt(rawImageData.length);
        out.write(dummy);
        out.flush();
    }

    public void setScreenOverlay(final OnScreenButtons overlay) {
        this.overlay = overlay;
    }

    @Override
    public void startUpdateThread() {
        synchronized (this) {
            stopUpdateThread();
            (updater = new Updater(context)).start();
        }
    }

    @Override
    public void stopUpdateThread() {
        synchronized (this) {
            if (updater != null) {
                updater.halt();
            }
        }
    }

    class Updater extends Thread {
        private volatile boolean running;
        Context context;

        public Updater(Context context) {
            super("PC Monitor Updater Task");
            running = true;
            this.context = context;
        }

        public void halt() {
            try {
                running = false;
                interrupt();
            } catch (SecurityException ignored) {
            }
        }

        @Override
        public void run() {
            while (running) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                    }
                    vgaCard.prepareUpdate();
                try {
                    vgaCard.updateDisplay();
                    int height = vgaCard.getDisplaySize().getHeight();
                    int width = vgaCard.getDisplaySize().getWidth();
//
//                    if (false && rand.nextDouble() < 0.05d) {
//                        byte[] bytes = ByteBuffer.allocate(height * width * 4).array();
//                        rand.nextBytes(bytes);
//                        IntBuffer intBuf = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
//                        intBuf.get(vgaCard.getDisplayBuffer());
//                    }
//
//                    bmp.setPixels(vgaCard.getDisplayBuffer(), 0, width, 0, 0, width, height);
                } catch (OutOfMemoryError e) {
                    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    activityManager.getMemoryInfo(mi);
                    long availableMegs = mi.availMem / 1048576L;
                    System.err.println(String.format("available megs %l", availableMegs));
                    throw e;
                }
                    postInvalidate();
            }
        }
    }
}
