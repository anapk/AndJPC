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
        if (n == 82) {
            isShiftHeld = false;
        }
        else {
            Log.i("JPC", "Keyboard input with key code " + n);
            if (this.isShiftHeld && n == 56) {
                this.keyboard.keyPressed((byte)39);
            }
            else if (n == 67) {
                this.keyboard.keyPressed((byte)14);
            }
            else {
                this.keyboard.keyPressed(KeyMapping.getScancode(n));
            }
            if (n == 59 || n == 60) {
                return this.isShiftHeld = isShiftHeld;
            }
        }
        return isShiftHeld;
    }
    
    public boolean onKeyUp(final int n, final KeyEvent keyEvent) {
        if (n == 82) {
            return false;
        }
        this.keyboard.keyReleased(KeyMapping.getScancode(n));
        if (n == 59 || n == 60) {
            this.isShiftHeld = false;
        }
        return true;
    }
}
