package com.example.joshua.snowsquirrel;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.esotericsoftware.kryonet.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;

// TODO: TCP connection code!

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Settings.OnFragmentInteractionListener {

    private Fragment homeFrag, manualFrag, settingsFrag;

    private String IP_ADDR = "10.0.0.83";
    private int PORT = 5500;
    private TcpClient mTcpClient;
    private boolean isConnected = false;
    private Toolbar toolbar;

    private TextView connected_text;
    private RelativeLayout connected_layout;

    //private Ros ros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        connected_text = (TextView)findViewById(R.id.connected_text);
        connected_layout = (RelativeLayout)findViewById(R.id.connected_layout);

        homeFrag = new HomeFrag();
        manualFrag = new ManualControl();
        settingsFrag = new Settings();

        ConnectTask yes = new ConnectTask();
        (new Thread(yes)).start();

        ConnectionTester no = new ConnectionTester();
        (new Thread(no)).start();

        selectFrag(homeFrag);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            selectFrag(settingsFrag);
        } else if (id == R.id.nav_paths) {

        } else if (id == R.id.nav_manual_control) {
            selectFrag(manualFrag);
        } else if (id == R.id.home) {
            selectFrag(homeFrag);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void selectFrag(Fragment frag) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, frag);
        fragmentTransaction.commit();

    }

    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    public class ConnectTask implements Runnable {
        public void run()
        {
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    Log.e("message", message);
                }
            });
            mTcpClient.run();
        }
    }

    public class SendMessage implements Runnable {
        private String message;
        public SendMessage(String message)
        {
            this.message = message;
        }
        public void run()
        {
            mTcpClient.sendMessage(message);
        }
    }

    public class ConnectionTester implements Runnable {

        public void run()
        {
            while (true)
            {
                if (mTcpClient != null)
                {
                    // send heartbeat message
                    sendData("heartbeat");

                    // check to see how long it was since the last message
                    if (System.currentTimeMillis() - mTcpClient.getLastUpdateTime() > 1000)
                    {
                        if (isConnected)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setConnectionGUIState(false);
                                }
                            });
                        isConnected = false;
                    }

                    else
                    {
                        if (!isConnected)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setConnectionGUIState(true);
                                }
                            });
                        isConnected = true;
                    }
                }

                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendData(String data)
    {
        SendMessage stuff = new SendMessage(data);
        (new Thread(stuff)).start();
    }

    public boolean isConnected(){
        return isConnected;
    }

    private void setConnectionGUIState(boolean connected)
    {
        if (homeFrag.isVisible())
        {
            if (connected)
            {
                ((RelativeLayout)findViewById(R.id.connected_layout)).setBackgroundResource(R.color.colorEnable);
                ((TextView)findViewById(R.id.connected_text)).setText("Connected!");
            }
            else
            {
                ((RelativeLayout)findViewById(R.id.connected_layout)).setBackgroundResource(R.color.colorAccent);
                ((TextView)findViewById(R.id.connected_text)).setText("Not Connected!");
            }
        }

    }

}
