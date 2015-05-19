package uk.co.jads.android.jpc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.jpc.emulator.peripheral.Keyboard;

class OnScreenButtons implements View.OnTouchListener
{
    private static final int BUTTON_SPACING = 75;
    @NonNull
    private final String[] BUTTON_LABELS;
    @NonNull
    private final Paint buttonHeldPaint;
    @NonNull
    private final Paint buttonPaint;
    private int buttonState;
    private int buttonsXPosition;
    private final Context context;
    private final Keyboard keyboard;
    @NonNull
    private final Paint labelPaint;
    
    public OnScreenButtons(final Keyboard keyboard, final Context context) {
        this.BUTTON_LABELS = new String[] { "Keyboard", "Left mouse", "Right mouse", "Esc" };
        this.buttonPaint = new Paint();
        this.buttonHeldPaint = new Paint();
        this.labelPaint = new Paint();
        this.buttonsXPosition = 700;
        this.buttonState = 0;
        this.keyboard = keyboard;
        this.context = context;
        this.buttonPaint.setStyle(Paint.Style.FILL);
        this.buttonPaint.setARGB(128, 64, 64, 64);
        this.buttonHeldPaint.setStyle(Paint.Style.FILL);
        this.buttonHeldPaint.setARGB(128, 64, 64, 128);
        this.labelPaint.setTextAlign(Paint.Align.CENTER);
        this.labelPaint.setStyle(Paint.Style.FILL);
        this.labelPaint.setARGB(255, 255, 255, 255);
    }
    
    private boolean press(final float n, final float n2) {
        if (n > this.buttonsXPosition) {
            if (n2 < 75.0f) {
                this.buttonState |= 0x1;
            }
            else if (n2 < 150.0f) {
                this.buttonState |= 0x2;
            }
            else if (n2 < 225.0f) {
                this.buttonState |= 0x4;
            }
            else if (n2 < 300.0f) {
                this.buttonState |= 0x8;
            }
            else if (n2 < 375.0f) {
                this.buttonState |= 0x10;
            }
            else if (n2 < 450.0f) {
                this.buttonState |= 0x20;
            }
            if ((0x8 & this.buttonState) == 0x8) {
                this.keyboard.keyPressed((byte)1);
            }
            return true;
        }
        return false;
    }
    
    private boolean release(final float n, final float n2) {
        if (n > this.buttonsXPosition) {
            final int buttonState = this.buttonState;
            if (n2 < 75.0f) {
                this.buttonState &= 0xFFFFFFFE;
            }
            else if (n2 < 150.0f) {
                this.buttonState &= 0xFFFFFFFD;
            }
            else if (n2 < 225.0f) {
                this.buttonState &= 0xFFFFFFFB;
            }
            else if (n2 < 300.0f) {
                this.buttonState &= 0xFFFFFFF7;
            }
            else if (n2 < 375.0f) {
                this.buttonState &= 0xFFFFFFEF;
            }
            else if (n2 < 450.0f) {
                this.buttonState &= 0xFFFFFFDF;
            }
            if ((buttonState & 0x1) == 0x1 && (0x1 & this.buttonState) != 0x1) {
                ((InputMethodManager)this.context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, 0);
            }
            if ((buttonState & 0x8) == 0x8 && (0x8 & this.buttonState) != 0x8) {
                this.keyboard.keyReleased((byte)1);
            }
            return true;
        }
        return false;
    }
    
    public int getButtonsXPosition() {
        return this.buttonsXPosition;
    }
    
    public int getMouseButtonState() {
        int n = 2;
        if ((0x2 & this.buttonState) == n) {
            n = 1;
        }
        else if ((0x4 & this.buttonState) != 0x4) {
            return 0;
        }
        return n;
    }
    
    void onDraw(@NonNull final Canvas canvas) {
        this.buttonsXPosition = -100 + canvas.getWidth();
        final int n = this.buttonsXPosition + (canvas.getWidth() - this.buttonsXPosition) / 2;
        for (int i = 0; i < 6; ++i) {
            if (i < this.BUTTON_LABELS.length) {
                Paint paint = this.buttonPaint;
                final int n2 = (int)Math.pow(2.0, i);
                if ((n2 & this.buttonState) == n2) {
                    paint = this.buttonHeldPaint;
                }
                final int n3 = 10 + i * 75;
                canvas.drawRect((float)this.buttonsXPosition, (float)n3, (float)canvas.getWidth(), (float)(n3 + 65), paint);
                canvas.drawText(this.BUTTON_LABELS[i], (float)n, (float)(n3 + 37), this.labelPaint);
            }
        }
    }
    
    public boolean onTouch(final View view, @NonNull final MotionEvent motionEvent) {
        boolean b = true;
        final int n = 0xFF & motionEvent.getAction();
        final int n2 = (0xFF00 & motionEvent.getAction()) >> 8;
        switch (n) {
            default: {
                b = false;
                return b;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < motionEvent.getPointerCount(); ++i) {
                    this.release(motionEvent.getX(i), motionEvent.getY(i));
                    this.press(motionEvent.getX(i), motionEvent.getY(i));
                }
            }
            case MotionEvent.ACTION_CANCEL: {
                return b;
            }
            case MotionEvent.ACTION_DOWN: {
                return this.press(motionEvent.getX(), motionEvent.getY());
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                return this.press(motionEvent.getX(n2), motionEvent.getY(n2));
            }
            case MotionEvent.ACTION_POINTER_UP: {
                return this.release(motionEvent.getX(n2), motionEvent.getY(n2));
            }
            case MotionEvent.ACTION_UP: {
                this.release(motionEvent.getX(), motionEvent.getY());
                return b;
            }
        }
    }
}
