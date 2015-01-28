package uk.co.jads.android.jpc;

import android.content.*;
import org.jpc.emulator.peripheral.*;
import android.view.*;
import android.util.*;

class KeyboardEmulator
{
    private boolean isShiftHeld;
    private final Keyboard keyboard;
    
    public KeyboardEmulator(final Keyboard keyboard, final Context context) {
        this.isShiftHeld = false;
        this.keyboard = keyboard;
        Context context1 = context;
    }
    
    public boolean onKeyDown(final int n, final KeyEvent keyEvent) {
        boolean isShiftHeld = true;
        if (n == KeyEvent.KEYCODE_MENU) {
            isShiftHeld = false;
        }
        else {
            Log.i("JPC", "Keyboard input with key code " + n);
            if (this.isShiftHeld && n == KeyEvent.KEYCODE_PERIOD) {
                this.keyboard.keyPressed((byte)39);
            }
            else if (n == KeyEvent.KEYCODE_DEL) {
                this.keyboard.keyPressed((byte)14);
            }
            else {
                this.keyboard.keyPressed(KeyMapping.getScancode(n));
            }
            if (n == KeyEvent.KEYCODE_SHIFT_LEFT || n == KeyEvent.KEYCODE_SHIFT_RIGHT) { // use isShiftPressed()?
                return this.isShiftHeld = isShiftHeld;
            }
        }
        return isShiftHeld;
    }
    
    public boolean onKeyUp(final int n, final KeyEvent keyEvent) {
        if (n == KeyEvent.KEYCODE_MENU) {
            return false;
        }
        this.keyboard.keyReleased(KeyMapping.getScancode(n));
        if (n == KeyEvent.KEYCODE_SHIFT_LEFT || n == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            this.isShiftHeld = false;
        }
        return true;
    }
}
