package com.gofirst.joshua.snowsquirrel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by server on 7/3/18.
 */

public class ConnectionProcessor implements Serializable
{
    ArrayList<ConnectionListener> listeners = new ArrayList<ConnectionListener>();

    private boolean state = false;
    private boolean isManualEnabled, isPathEnabled;

    public void setOnConnectionListener(ConnectionListener listener)
    {
        // Store the listener object
        this.listeners.add(listener);
    }

    public void setConnected()
    {
        state = true;
        for (ConnectionListener listener : listeners)
        {
            listener.onConnected();
        }

    }

    public void setDisconnected()
    {
        state = false;
        for (ConnectionListener listener : listeners)
        {
            listener.onDisconnected();
        }
    }

    public boolean getConnectionState()
    {
        return state;
    }

    public void setManualEnabled(boolean isEnabled)
    {
        this.isManualEnabled = isEnabled;
    }

    public boolean isManualEnabled()
    {
        return isManualEnabled;
    }

    public void setPathEnabled(boolean isEnabled)
    {
        this.isPathEnabled = isEnabled;
    }

    public boolean isPathEnabled()
    {
        return isPathEnabled;
    }
}