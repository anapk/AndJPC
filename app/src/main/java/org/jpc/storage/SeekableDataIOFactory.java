/*
    JPC: An x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.4.1

    A project from the eMediaTrack ltd.

    Copyright (C) 2007-2010 The eMediaTrack ltd.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Details (including contact information) can be found at:

    http://jpc.sourceforge.net/

    End of licence header
*/

/**
 *
 * @author Rhys Newman & Pin Hu
 */

package org.jpc.storage;

import java.net.*;
import java.io.*;

class SeekableDataIOFactory {

    private static final String RAW = "raw";
    private static final String VMDK = "vmdk";

    public static SeekableDataIO open(URI uri, String format) throws IOException
    {
        switch (format) {
            case RAW:
                SeekableDataIO raw = new Raw(uri);
                return raw;
            case VMDK:
                SeekableDataIO vmdk = new Vmdk(uri);
                return vmdk;
            default:
                throw new IOException("The image format \"" + format + "\" is not supported");
        }
    }

    
    public static SeekableDataIO open(URI uri) throws IOException
    {
        SeekableDataIO sid = new Raw(uri);
        byte[] header = new byte[512];
        sid.read(header, 0, 512);

        //detect whether the file is a VMDK image
        byte[] vmdkMagic = Vmdk.VMDK4MAGIC.getBytes("US-ASCII");
        if(vmdkMagic[0] == header[0] && vmdkMagic[1] == header[1] && vmdkMagic[2] == header[2] && vmdkMagic[3] == header[3]){
            sid.close();
            sid = null;
            sid = new Vmdk(uri);
        }

        return sid;
    }


    public static SeekableDataIO create(URI uri, long length, String format) throws IOException
    {
        switch (format) {
            case RAW:
                return Raw.create(uri, length);
            case VMDK:
                return Vmdk.create(uri, length);
            default:
                throw new IOException("The image format \"" + format + "\" is not supported");
        }
    }
}
