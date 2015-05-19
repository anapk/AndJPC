package uk.co.jads.android.jpc;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;

import org.jpc.j2se.Option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JPCAndroidActivity extends JPCAndroidActivityHelper implements Runnable
{
    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(128);
        getWindow().addFlags(1024);
        getWindow().clearFlags(2048);
        requestWindowFeature(1);
        LinearLayout ll;
        setContentView(ll = new LinearLayout(this));
        PCMonitor v = new PCMonitor(this);
        ll.addView(v);

        CopiedResourceFile f1, f2;

        try {
            f1 = new CopiedResourceFile(getBaseContext().getCacheDir(), JPCAndroidActivity.class.getResourceAsStream("/resources/images/freedos-ipx1.img"), "freedos-ipx1.img");
            f2 = new CopiedResourceFile(getBaseContext().getCacheDir(), JPCAndroidActivity.class.getResourceAsStream("/resources/images/doom19.img"), "doom19.img");
        } catch (IOException e) {
            throw new RuntimeException();
        }

        String[] DEFAULT_ARGS = new String[] {
                //"-fda", "mem:resources/images/floppy.img", "-hda", "mem:resources/images/dosgames.img", "-boot", "fda"
                //"-cdrom", "mem:resources/images/ttylinux-i386-5.3.iso", "-boot", "cdrom", "-ss", "resources/BOOT-ttylinux-5.3-cd-shell.zip", "-ethernet", "-net", "hub:relay.widgetry.org:80"
                //"-fda", "mem:resources/images/freedos-ipx1.img", "-hda", "mem:resources/images/doom19.img", "-boot", "fda", "-ethernet", "-net", "hub:relay.widgetry.org:80", "-no-pc-speaker"
                "-fda", "mem:resources/images/freedos-ipx1.img" /*f1.getFile().getPath()*/, "-hda", f2.getFile().getPath(), "-boot", "fda", "-ethernet", "-net", "hub:relay.widgetry.org:80", "-no-pc-speaker"
        };

        init(DEFAULT_ARGS, v);

        // TODO close f1 f2
    }
}

class CopiedResourceFile implements java.io.Closeable {

    File cacheFile;

    CopiedResourceFile(File cacheDir, InputStream inputStream, String identifier) throws IOException {
        cacheFile = new File(cacheDir, identifier);

        if (cacheFile.createNewFile() == false) {
            cacheFile.delete();
            cacheFile.createNewFile();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(cacheFile);

        byte[] buffer = new byte[1024 * 512];
        while (inputStream.read(buffer, 0, 1024 * 512) != -1) {
            fileOutputStream.write(buffer);
        }

        fileOutputStream.close();
    }

    File getFile() {
        return cacheFile;
    }

    @Override
    public void close() {
        cacheFile.delete();
    }
}