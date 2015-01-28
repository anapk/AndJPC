package uk.co.jads.android.jpc;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.jpc.emulator.peripheral.Keyboard;

class MouseEmulator implements View.OnTouchListener
{
    private final OnScreenButtons buttons;
    private final Keyboard keyboard;
    private int lastMouseX;
    private int lastMouseY;
    
    public MouseEmulator(final Keyboard keyboard, final OnScreenButtons buttons) {
        this.lastMouseX = 0;
        this.lastMouseY = 0;
        this.keyboard = keyboard;
        this.buttons = buttons;
    }
    
    public boolean onTouch(final View view, @NonNull final MotionEvent motionEvent) {
        this.buttons.onTouch(view, motionEvent);
        final int lastMouseX = (int)motionEvent.getX();
        final int lastMouseY = (int)motionEvent.getY();
        final int n = 0xFF & motionEvent.getAction();
        final int buttonsXPosition = this.buttons.getButtonsXPosition();
        int n2 = 0;
        int n3 = 0;
        if (lastMouseX < buttonsXPosition) {
            n2 = 0;
            n3 = 0;
            if (n == 2) {
                n2 = lastMouseX - this.lastMouseX;
                n3 = lastMouseY - this.lastMouseY;
                if (n2 > 0) {
                    n2 = Math.max((int)(0.7f * n2), 1);
                }
                else if (n2 < 0) {
                    n2 = Math.min((int)(0.7f * n2), -1);
                }
                if (n3 > 0) {
                    n3 = Math.max((int)(0.7f * n3), 1);
                }
                else if (n3 < 0) {
                    n3 = Math.min((int)(0.7f * n3), -1);
                }
            }
            this.lastMouseX = lastMouseX;
            this.lastMouseY = lastMouseY;
        }
        if (n == 2 || n == 1 || n == 0) {
            int mouseButtonState = this.buttons.getMouseButtonState();
            if (mouseButtonState == 0 && motionEvent.getAction() == 0 && Math.abs(lastMouseX - this.lastMouseX) < 10 && Math.abs(lastMouseY - this.lastMouseY) < 10) {
                mouseButtonState = 1;
            }
            Log.i("JPC", "Mouse event: " + n2 + "," + n3 + "(" + mouseButtonState + ")");
            this.keyboard.putMouseEvent(n2, n3, 0, mouseButtonState);
        }
        return true;
    }
}
