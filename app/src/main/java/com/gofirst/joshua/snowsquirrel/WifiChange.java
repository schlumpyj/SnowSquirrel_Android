package com.gofirst.joshua.snowsquirrel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by server on 7/7/18.
 */

public class WifiChange extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null) {

            if (info.isConnected())
            {
                if (MainActivity.LOGS)
                    Log.i("network_change", "NETWORK CONNECTED");

                MainActivity instance = MainActivity.getInstance();
                if (instance != null)
                    MainActivity.getInstance().changeIP();

            }
            else
            {
                if (MainActivity.LOGS)
                    Log.i("network_change", "NETWORK NOT CONNECTED");
                MainActivity instance = MainActivity.getInstance();
                if (instance != null)
                    MainActivity.getInstance().endConnection();
            }

        }
    }
}

