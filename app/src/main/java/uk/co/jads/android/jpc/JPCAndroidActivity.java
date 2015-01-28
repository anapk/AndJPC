package uk.co.jads.android.jpc;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.LinearLayout;

public class JPCAndroidActivity extends JPCAndroidActivityHelper implements Runnable
{
    @NonNull
    private static final String[] DEFAULT_ARGS;
    
    static {
        DEFAULT_ARGS = new String[] { "-fda", "mem:resources/images/floppy.img", "-hda", "mem:resources/images/dosgames.img", "-boot", "fda" };
    }
    
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
        init(JPCAndroidActivity.DEFAULT_ARGS, v);
    }
}
