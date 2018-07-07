package com.example.joshua.snowsquirrel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by server on 7/7/18.
 */

public class WifiChange extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null && info.isConnected()) {
            // Do your work.
            Log.e("network_change", "NETWORK CONNECTED");
            MainActivity.getInstance().changeIP();
        }
    }
}

