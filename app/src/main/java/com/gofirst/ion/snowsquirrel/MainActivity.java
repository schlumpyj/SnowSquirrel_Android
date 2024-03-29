package com.gofirst.ion.snowsquirrel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gofirst.ion.snowsquirrel.Enums.PacketID;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Settings.OnFragmentInteractionListener {

    private Fragment homeFrag, manualFrag, pathSetter, settingsFrag;

    private TcpClient mTcpClient;
    private boolean isConnected = false;
    private Toolbar toolbar;

    private TextView connected_text;
    private RelativeLayout connected_layout;

    private ManualControl fragment;

    private String ip, port;

    private ConnectionProcessor connectionProcessor;

    private CommClass commClass;

    private static MainActivity ins;

    public static boolean LOGS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
            @Override
            public void onDrawerOpened(@NonNull View drawerView) { }
            @Override
            public void onDrawerClosed(@NonNull View drawerView) { }

            @Override
            public void onDrawerStateChanged(int newState)
            {
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        connected_text = (TextView)findViewById(R.id.connected_text);
        connected_layout = (RelativeLayout)findViewById(R.id.connected_layout);

        commClass = new CommClass();

        connectionProcessor = new ConnectionProcessor();

        getIntent().putExtra("Connection", connectionProcessor);

        homeFrag = new HomeFrag();
        manualFrag = new ManualControl();
        settingsFrag = new Settings();
        pathSetter = new PathSetter();

        SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_LOCATION, Context.MODE_PRIVATE);
        ip = preferences.getString("robot_ip", Constants.DEFAULT_IP);
        port = preferences.getString("robot_tcp_port", Constants.DEFAULT_PORT);

        ConnectTask connectTask = new ConnectTask();
        (new Thread(connectTask)).start();

        ConnectionTester connectionTester = new ConnectionTester();
        (new Thread(connectionTester)).start();

        selectFrag(homeFrag);
        navigationView.getMenu().getItem(0).setChecked(true);

        ins = this;

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
        if (id == R.id.action_change_wifi) {
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (connectionProcessor.isManualEnabled() && id != R.id.nav_manual_control)
            Toast.makeText(this, "Manual Control Disabled", Toast.LENGTH_LONG).show();

        if (id == R.id.nav_settings) {
            selectFrag(settingsFrag);
        } else if (id == R.id.nav_paths) {
            selectFrag(pathSetter);
        } else if (id == R.id.nav_manual_control) {
            selectFrag(manualFrag);
        } else if (id == R.id.home) {
            selectFrag(homeFrag);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static MainActivity getInstance() {
        return ins;
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
                    if (LOGS)
                        Log.e("message", message);
                }
            });
            mTcpClient.setIPandPORT(ip, Integer.valueOf(port));

            mTcpClient.run();
        }
    }

    public class SendMessage implements Runnable {
        private String message;
        public SendMessage(CommClass message)
        {
            Gson gson = new Gson();
            this.message = gson.toJson(message);
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
                if (mTcpClient != null && mTcpClient.isConnected())
                {
                    // send heartbeat message
                    commClass.data = new double[]{0};
                    commClass.packetID = PacketID.HEARTBEAT.ordinal();
                    sendData(commClass);

                    // check to see how long it was since the last message
                    if (System.currentTimeMillis() - mTcpClient.getLastUpdateTime() > 1000)
                    {
                        if (isConnected)
                        {
                            connectionProcessor.setDisconnected();

                            isConnected = false;
                        }
                    }

                    else
                    {
                        if (LOGS)
                            Log.d("Connected", isConnected?"Yes":"No");

                        if (!isConnected)
                        {
                            connectionProcessor.setConnected();

                            isConnected = true;
                        }
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

    public void sendData(CommClass data)
    {
        SendMessage sender = new SendMessage(data);
        (new Thread(sender)).start();
    }

    public void setConnected(boolean state)
    {
        isConnected = state;
    }

    public void saveSettings()
    {
        SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_LOCATION, MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();

        ip = ((TextInputEditText)findViewById(R.id.robot_ip_address)).getText().toString();
        port = ((TextInputEditText)findViewById(R.id.robot_tcp_port)).getText().toString();

        edit.putString("robot_ip" , ip);
        edit.putString("robot_tcp_port" , port);
        edit.putString("robot_password" , ((TextInputEditText)findViewById(R.id.robot_password)).getText().toString());

        edit.apply();

        changeIP();
    }

    public void changeIP() {
        ChangeIPTask changer = new ChangeIPTask();
        (new Thread(changer)).start();
    }

    public void endConnection()
    {
        if (mTcpClient != null)
        {
            mTcpClient.stopClient();
        }
    }

    public class ChangeIPTask implements Runnable {
        public void run()
        {
            mTcpClient.stopClient();

            ConnectTask connectTask = new ConnectTask();
            (new Thread(connectTask)).start();

        }
    }

}
