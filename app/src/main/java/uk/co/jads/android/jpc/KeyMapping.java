package uk.co.jads.android.jpc;

import java.util.*;

class KeyMapping
{
    private static Map<Integer, Byte> scancodeTable;
    
    static {
        (KeyMapping.scancodeTable = new HashMap<>()).put(4, (byte)1);
        KeyMapping.scancodeTable.put(8, (byte)2);
        KeyMapping.scancodeTable.put(9, (byte)3);
        KeyMapping.scancodeTable.put(10, (byte)4);
        KeyMapping.scancodeTable.put(11, (byte)5);
        KeyMapping.scancodeTable.put(12, (byte)6);
        KeyMapping.scancodeTable.put(13, (byte)7);
        KeyMapping.scancodeTable.put(14, (byte)8);
        KeyMapping.scancodeTable.put(15, (byte)9);
        KeyMapping.scancodeTable.put(16, (byte)10);
        KeyMapping.scancodeTable.put(7, (byte)11);
        KeyMapping.scancodeTable.put(69, (byte)12);
        KeyMapping.scancodeTable.put(70, (byte)13);
        KeyMapping.scancodeTable.put(61, (byte)15);
        KeyMapping.scancodeTable.put(45, (byte)16);
        KeyMapping.scancodeTable.put(51, (byte)17);
        KeyMapping.scancodeTable.put(33, (byte)18);
        KeyMapping.scancodeTable.put(46, (byte)19);
        KeyMapping.scancodeTable.put(48, (byte)20);
        KeyMapping.scancodeTable.put(53, (byte)21);
        KeyMapping.scancodeTable.put(49, (byte)22);
        KeyMapping.scancodeTable.put(37, (byte)23);
        KeyMapping.scancodeTable.put(43, (byte)24);
        KeyMapping.scancodeTable.put(44, (byte)25);
        KeyMapping.scancodeTable.put(71, (byte)26);
        KeyMapping.scancodeTable.put(72, (byte)27);
        KeyMapping.scancodeTable.put(66, (byte)28);
        KeyMapping.scancodeTable.put(29, (byte)30);
        KeyMapping.scancodeTable.put(47, (byte)31);
        KeyMapping.scancodeTable.put(32, (byte)32);
        KeyMapping.scancodeTable.put(34, (byte)33);
        KeyMapping.scancodeTable.put(35, (byte)34);
        KeyMapping.scancodeTable.put(36, (byte)35);
        KeyMapping.scancodeTable.put(38, (byte)36);
        KeyMapping.scancodeTable.put(39, (byte)37);
        KeyMapping.scancodeTable.put(40, (byte)38);
        KeyMapping.scancodeTable.put(74, (byte)39);
        KeyMapping.scancodeTable.put(59, (byte)42);
        KeyMapping.scancodeTable.put(54, (byte)44);
        KeyMapping.scancodeTable.put(52, (byte)45);
        KeyMapping.scancodeTable.put(31, (byte)46);
        KeyMapping.scancodeTable.put(50, (byte)47);
        KeyMapping.scancodeTable.put(30, (byte)48);
        KeyMapping.scancodeTable.put(42, (byte)49);
        KeyMapping.scancodeTable.put(41, (byte)50);
        KeyMapping.scancodeTable.put(55, (byte)51);
        KeyMapping.scancodeTable.put(56, (byte)52);
        KeyMapping.scancodeTable.put(76, (byte)53);
        KeyMapping.scancodeTable.put(60, (byte)54);
        KeyMapping.scancodeTable.put(62, (byte)57);
        KeyMapping.scancodeTable.put(3, (byte)(-57));
        KeyMapping.scancodeTable.put(19, (byte)(-56));
        KeyMapping.scancodeTable.put(92, (byte)(-55));
        KeyMapping.scancodeTable.put(21, (byte)(-53));
        KeyMapping.scancodeTable.put(22, (byte)(-51));
        KeyMapping.scancodeTable.put(20, (byte)(-48));
        KeyMapping.scancodeTable.put(93, (byte)(-47));
        KeyMapping.scancodeTable.put(67, (byte)(-45));
    }
    
    public static byte getScancode(final Integer n) {
        try {
            return KeyMapping.scancodeTable.get(n);
        }
        catch (NullPointerException ex) {
            return 0;
        }
    }
}
