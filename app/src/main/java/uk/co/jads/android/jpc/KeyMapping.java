package uk.co.jads.android.jpc;

import java.util.HashMap;
import java.util.Map;
import static android.view.KeyEvent.*;

class KeyMapping {
    private static Map<Integer, Byte> scancodeTable;

    static {
        // lines marked with missing were mapped in the original JPC
        scancodeTable = new HashMap<>();
        scancodeTable.put(KEYCODE_BACK          , (byte) 1);
        scancodeTable.put(KEYCODE_1             , (byte) 2);
        scancodeTable.put(KEYCODE_2             , (byte) 3);
        scancodeTable.put(KEYCODE_3             , (byte) 4);
        scancodeTable.put(KEYCODE_4             , (byte) 5);
        scancodeTable.put(KEYCODE_5             , (byte) 6);
        scancodeTable.put(KEYCODE_6             , (byte) 7);
        scancodeTable.put(KEYCODE_7             , (byte) 8);
        scancodeTable.put(KEYCODE_8             , (byte) 9);
        scancodeTable.put(KEYCODE_9             , (byte) 10);
        scancodeTable.put(KEYCODE_0             , (byte) 11);
        scancodeTable.put(KEYCODE_MINUS         , (byte) 12);
        scancodeTable.put(KEYCODE_EQUALS        , (byte) 13);
        // missing KeyEvent.VK_BACK_SPACE, keycode 14
        scancodeTable.put(KEYCODE_TAB           , (byte) 15);
        scancodeTable.put(KEYCODE_Q             , (byte) 16);
        scancodeTable.put(KEYCODE_W             , (byte) 17);
        scancodeTable.put(KEYCODE_E             , (byte) 18);
        scancodeTable.put(KEYCODE_R             , (byte) 19);
        scancodeTable.put(KEYCODE_T             , (byte) 20);
        scancodeTable.put(KEYCODE_Y             , (byte) 21);
        scancodeTable.put(KEYCODE_U             , (byte) 22);
        scancodeTable.put(KEYCODE_I             , (byte) 23);
        scancodeTable.put(KEYCODE_O             , (byte) 24);
        scancodeTable.put(KEYCODE_P             , (byte) 25);
        scancodeTable.put(KEYCODE_LEFT_BRACKET  , (byte) 26);
        scancodeTable.put(KEYCODE_RIGHT_BRACKET , (byte) 27);
        scancodeTable.put(KEYCODE_ENTER         , (byte) 28);
        // missing KeyEvent.VK_CONTROL, keycode 29
        scancodeTable.put(KEYCODE_A             , (byte) 30);
        scancodeTable.put(KEYCODE_S             , (byte) 31);
        scancodeTable.put(KEYCODE_D             , (byte) 32);
        scancodeTable.put(KEYCODE_F             , (byte) 33);
        scancodeTable.put(KEYCODE_G             , (byte) 34);
        scancodeTable.put(KEYCODE_H             , (byte) 35);
        scancodeTable.put(KEYCODE_J             , (byte) 36);
        scancodeTable.put(KEYCODE_K             , (byte) 37);
        scancodeTable.put(KEYCODE_L             , (byte) 38);
        scancodeTable.put(KEYCODE_SEMICOLON     , (byte) 39);
        // missing KeyEvent.VK_QUOTE, keycode 40
        // missing KeyEvent.VK_BACK_QUOTE, keycode 41
        scancodeTable.put(KEYCODE_SHIFT_LEFT    , (byte) 42);
        // missing KeyEvent.VK_BACK_SLASH, keycode 43
        scancodeTable.put(KEYCODE_Z             , (byte) 44);
        scancodeTable.put(KEYCODE_X             , (byte) 45);
        scancodeTable.put(KEYCODE_C             , (byte) 46);
        scancodeTable.put(KEYCODE_V             , (byte) 47);
        scancodeTable.put(KEYCODE_B             , (byte) 48);
        scancodeTable.put(KEYCODE_N             , (byte) 49);
        scancodeTable.put(KEYCODE_M             , (byte) 50);
        scancodeTable.put(KEYCODE_COMMA         , (byte) 51);
        scancodeTable.put(KEYCODE_PERIOD        , (byte) 52);
        scancodeTable.put(KEYCODE_SLASH         , (byte) 53);
        scancodeTable.put(KEYCODE_SHIFT_RIGHT   , (byte) 54);
        // 55 == 0x37 KPad * (star)
        // missing KeyEvent.VK_ALT, keycode 56
        scancodeTable.put(KEYCODE_SPACE         , (byte) 57);
        // 71-83 (decimal) are numpad keys

        // these were mapped in original JPC
        // scancodeTable.put(Integer.valueOf(122), (byte) 87); // F11
        // scancodeTable.put(Integer.valueOf(123), (byte) 88); // F12
        
        //Extended Keys
        //-100, KPad Enter
        // -99, R-Ctrl
        // -86, fake L-Shift
        // -75, KPad /
        // -74, fake R-Shift
        // -73, Ctrl + Print Screen
        // this one was mapped in the original JPC
        // scancodeTable.put(Integer.valueOf(KeyEvent.VK_ALT_GRAPH), Byte.valueOf((byte) -72)); //AltGr
        // -58, Ctrl + Break

        scancodeTable.put(KEYCODE_HOME          , (byte)-57); // VK_HOME, keycode 0x47 bit-or 0x80
        scancodeTable.put(KEYCODE_DPAD_UP       , (byte)-56);
        scancodeTable.put(KEYCODE_PAGE_UP       , (byte)-55);
        scancodeTable.put(KEYCODE_DPAD_LEFT     , (byte)-53);
        scancodeTable.put(KEYCODE_DPAD_RIGHT    , (byte)-51);
        // missing KeyEvent.VK_END, keycode -49 == (0x4f | 0x80) - 256
        scancodeTable.put(KEYCODE_DPAD_DOWN     , (byte)-48);
        scancodeTable.put(KEYCODE_PAGE_DOWN     , (byte)-47);
        // missing KeyEvent.VK_INSERT, keycode -46, hex((-46 & 0xff) - 0x80) == 0x52
        scancodeTable.put(KEYCODE_DEL           , (byte)-45);
        // -37, L-Win
        // -36, R-Win
        // -35, Context-Menu
        // this one was also mapped in original JPC:
        // scancodeTable.put(Integer.valueOf(19), (byte) 0xFF); //Pause
    }

    public static byte getScancode(final int n) {
        return scancodeTable.get(n);
    }
}
