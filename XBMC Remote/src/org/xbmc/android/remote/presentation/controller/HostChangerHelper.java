package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.xbmc.api.object.Host;
import org.xbmc.android.util.HostFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class HostChangerHelper implements OnClickListener {
    protected ArrayList<? extends Host> HostFactoryGetHosts() {
        return HostFactory.getHosts(mActivity.getApplicationContext());
    }
    protected Host HostFactoryCurrentHost() {
        return HostFactory.host;
    }
    protected void HostFactorySaveHost(Context context, Host host) {
        HostFactory.saveHost(context, host);
    }
    
    private Activity mActivity;
    private final HashMap<Integer, Host> mHostMap = new HashMap<Integer, Host>();;

    protected String TAG = "onHostClickListener";  
    
    public HostChangerHelper(Activity activity) {
        mActivity = activity;
        
        // granted, this is butt-ugly. better ideas, be my guest.
        final ArrayList<? extends Host> hosts = HostFactoryGetHosts();
        final CharSequence[] names = new CharSequence[hosts.size()];
        int i = 0;
        for (Host host : hosts) {
            names[i] = host.name;
            mHostMap.put(i, host);
            i++;
        }
        if (hosts.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Pick your XBMC!");
            builder.setItems(names, this);
            AlertDialog dialog = builder.create();
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            dialog.show();
        } else {
            Toast.makeText(activity.getApplicationContext(), "No XBMC hosts defined, please do that first.", Toast.LENGTH_LONG).show();
        }
    }
    
    public void onClick(DialogInterface dialog, int which) {
        final Host host = mHostMap.get(which);
        if (HostFactoryCurrentHost() != null && HostFactoryCurrentHost().id == host.id) {
            Toast.makeText(mActivity.getApplicationContext(), "You've picked the same host as the current.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i(TAG, "Switching host to " + (host == null ? "<null>" : host.addr) + ".");
            HostFactorySaveHost(mActivity.getApplicationContext(), host);
            Toast.makeText(mActivity.getApplicationContext(), "Changed host to " + host.toString() + ".", Toast.LENGTH_SHORT).show();
        }
    }
}
