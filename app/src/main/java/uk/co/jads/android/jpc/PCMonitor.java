package uk.co.jads.android.jpc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.jpc.emulator.PC;
import org.jpc.emulator.pci.peripheral.DefaultVGACard;
import org.jpc.emulator.pci.peripheral.VGACard;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PCMonitor extends View {
    private final Paint solidPaint;
    private Bitmap bmp;
    private int[] buffer;
    private OnScreenButtons overlay;
    private PC pc;
    private Updater updater;
    private DefaultVGACard vgaCard;

    public PCMonitor(final Context context) {
        super(context);
        this.solidPaint = new Paint();
    }

    public PCMonitor(final Context context, final AttributeSet set) {
        super(context, set);
        this.solidPaint = new Paint();
    }

    int getOutputHeight() {
        if (this.vgaCard == null) {
            return 0;
        }
        return this.vgaCard.getHeight();
    }

    int getOutputWidth() {
        if (this.vgaCard == null) {
            return 0;
        }
        return this.vgaCard.getWidth();
    }

    protected PC getPC() {
        return this.pc;
    }

    public void setPC(final PC pc) {
        this.pc = pc;
        this.solidPaint.setStyle(Paint.Style.FILL);
        this.solidPaint.setARGB(255, 255, 255, 255);
        (this.vgaCard = (DefaultVGACard) pc.getComponent(VGACard.class)).setMonitor(this);
        this.vgaCard.resizeDisplay(640, 480);
    }

    public boolean isRunning() {
        synchronized (this) {
            return this.updater != null && this.updater.running;
        }
    }

    public void loadState(InputStream in) throws IOException {
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

    protected void onDraw(final Canvas canvas) {
        if (this.vgaCard != null) {
            final int width = this.vgaCard.getWidth();
            final int height = this.vgaCard.getHeight();
            if (width * height > 0) {
                this.bmp.setPixels(this.buffer, 0, width, 0, 0, width, height);
                canvas.drawBitmap(this.bmp, 0.0f, 0.0f, this.solidPaint);
            }
            if (this.overlay != null) {
                this.overlay.onDraw(canvas);
            }
        }
    }

    public void resizeDisplay(final int n, final int n2) {
        if (this.vgaCard == null || n == 0 || n2 == 0) {
            return;
        }
        if (n * n2 > 0) {
            this.buffer = new int[n2 * n];
            this.vgaCard.setDisplayBuffer(this.buffer, n, n2);
            this.bmp = Bitmap.createBitmap(this.getOutputWidth(), this.getOutputHeight(), Bitmap.Config.ARGB_8888);
        }
        this.postInvalidate();
    }

    public void saveState(OutputStream out) throws IOException {
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

    public void startUpdateThread() {
        synchronized (this) {
            this.stopUpdateThread();
            (this.updater = new Updater()).start();
        }
    }

    public void stopUpdateThread() {
        synchronized (this) {
            if (this.updater != null) {
                this.updater.halt();
            }
        }
    }

    class Updater extends Thread {
        private volatile boolean running;

        public Updater() {
            super("PC Monitor Updater Task");
            this.running = true;
        }

        public void halt() {
            try {
                this.running = false;
                this.interrupt();
            } catch (SecurityException ignored) {
            }
        }

        @Override
        public void run() {
            while (this.running) {
                while (true) {
                    try {
                        Thread.sleep(50L);
                        PCMonitor.this.vgaCard.prepareUpdate();
                        PCMonitor.this.vgaCard.updateDisplay();
                        PCMonitor.this.postInvalidate();
                    } catch (InterruptedException ex) {
                        continue;
                    }
                    break;
                }
            }
        }
    }
}
