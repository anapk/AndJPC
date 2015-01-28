package uk.co.jads.android.jpc;

import android.view.*;
import android.content.*;
import android.util.*;
import java.io.*;
import android.graphics.*;
import org.jpc.emulator.pci.peripheral.*;
import org.jpc.emulator.*;

public class PCMonitor extends View
{
    private Bitmap bmp;
    private int[] buffer;
    private OnScreenButtons overlay;
    private PC pc;
    private final Paint solidPaint;
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
    
    public boolean isRunning() {
        synchronized (this) {
            return this.updater != null && this.updater.running;
        }
    }
    
    public void loadState(final InputStream inputStream) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(inputStream);
        final int int1 = dataInputStream.readInt();
        final int[] displayBuffer = this.vgaCard.getDisplayBuffer();
        if (int1 != displayBuffer.length) {
            throw new IOException("Image size not consistent with saved image state");
        }
        final byte[] array = new byte[int1 * 4];
        dataInputStream.readFully(array);
        int i = 0;
        int n = 0;
        while (i < int1) {
            final int n2 = n + 1;
            final int n3 = (0xFF & array[n]) << 24;
            final int n4 = n2 + 1;
            final int n5 = n3 | (0xFF & array[n2]) << 16;
            final int n6 = n4 + 1;
            final int n7 = n5 | (0xFF & array[n4]) << 8;
            n = n6 + 1;
            displayBuffer[i] = (n7 | (0xFF & array[n6]));
            ++i;
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
    
    public void saveState(final OutputStream outputStream) throws IOException {
        final int[] displayBuffer = this.vgaCard.getDisplayBuffer();
        final byte[] array = new byte[4 * displayBuffer.length];
        int i = 0;
        int n = 0;
        while (i < displayBuffer.length) {
            final int n2 = displayBuffer[i];
            final int n3 = n + 1;
            array[n] = (byte)(n2 >> 24);
            final int n4 = n3 + 1;
            array[n3] = (byte)(n2 >> 16);
            final int n5 = n4 + 1;
            array[n4] = (byte)(n2 >> 8);
            n = n5 + 1;
            array[n5] = (byte)n2;
            ++i;
        }
        new DataOutputStream(outputStream).writeInt(displayBuffer.length);
        outputStream.write(array);
        outputStream.flush();
    }
    
    public void setPC(final PC pc) {
        this.pc = pc;
        this.solidPaint.setStyle(Paint.Style.FILL);
        this.solidPaint.setARGB(255, 255, 255, 255);
        (this.vgaCard = (DefaultVGACard)pc.getComponent(VGACard.class)).setMonitor(this);
        this.vgaCard.resizeDisplay(640, 480);
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
    
    class Updater extends Thread
    {
        private volatile boolean running;
        
        public Updater() {
            super("PC Monitor Updater Task");
            this.running = true;
        }
        
        public void halt() {
            try {
                this.running = false;
                this.interrupt();
            }
            catch (SecurityException ignored) {}
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
                    }
                    catch (InterruptedException ex) {
                        continue;
                    }
                    break;
                }
            }
        }
    }
}
