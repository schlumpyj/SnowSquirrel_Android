package com.gofirst.joshua.snowsquirrel;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import yjkim.mjpegviewer.MjpegView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManualControl#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualControl extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private Button enable_disable;
    private JoystickView joystick;

    private boolean isEnabled = false;

    private ConnectionProcessor connectionProcessor;
    private CommClass commClass;

    public ManualControl() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManualControl.
     */
    // TODO: Rename and change types and number of parameters
    public static ManualControl newInstance(String param1, String param2) {
        ManualControl fragment = new ManualControl();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        connectionState(connectionProcessor.getConnectionState());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manual_control, container, false);

        Intent i = getActivity().getIntent();

        commClass = new CommClass();

        connectionProcessor = (ConnectionProcessor)i.getSerializableExtra("Connection");

        joystick = (JoystickView)view.findViewById(R.id.joystick);

        enable_disable = (Button)view.findViewById(R.id.enable_manual_control);

        enable_disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEnabled = !isEnabled;
                connectionProcessor.setEnabled(isEnabled);
                if (isEnabled)
                {
                    ConstantUpdater stuff = new ConstantUpdater();
                    (new Thread(stuff)).start();
                    enable_disable.setText("Disable");
                    enable_disable.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorDisable));
                }
                else
                {
                    enable_disable.setText("Enable");
                    enable_disable.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorEnable));
                }

            }
        });

        connectionProcessor.setOnConnectionListener(
            new ConnectionListener() {
                @Override
                public void onConnected() {
                    if (isVisible()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setConnectionState(true);
                            }
                        });
                    }
                }

                @Override
                public void onDisconnected() {
                    if (isVisible()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setConnectionState(false);
                            }
                        });
                    }
                }
            }
        );

        SharedPreferences preferences = this.getActivity().getSharedPreferences(((MainActivity)getActivity()).SETTINGS_LOCATION, Context.MODE_PRIVATE);
        String address = "http://"+(preferences.getString("robot_ip", "10.24.67.20"))+":8080/stream?topic=/camera/image_raw&type=ros_compressed";

        MjpegView mv = (MjpegView) view.findViewById(R.id.videwView);

        mv.Start(address);

        setConnectionState(connectionProcessor.getConnectionState());

        return view;
    }

    class ConstantUpdater implements Runnable
    {
        public void run()
        {
            while (isEnabled)
            {
                if (((MainActivity)getActivity())== null)
                {
                    isEnabled = false;
                    connectionProcessor.setEnabled(false);
                    break;
                }
                commClass.packetID = PacketID.MANUAL_CONTROL.ordinal();
                commClass.data = new double[]{
                        -1*standardizeJoystickValue(joystick.getNormalizedX()),
                        -1*standardizeJoystickValue(joystick.getNormalizedY())
                };
                ((MainActivity)getActivity()).sendData(commClass);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private double standardizeJoystickValue(int value)
    {
        value = value - 50;
        return value/44.0;
    }

    private void connectionState(boolean state)
    {
        if (state)
        {
            enable_disable.setText("Enable");
            enable_disable.setEnabled(true);
            enable_disable.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorEnable));
        }
        else
        {
            enable_disable.setText("Not connected");
            enable_disable.setEnabled(false);
            enable_disable.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorDisable));
        }
    }

    public void setConnectionState(boolean state)
    {
        if (state)
        {
            if (isVisible())
                connectionState(true);
        }
        else
        {
            if (isVisible())
            {
                Toast.makeText(getContext(), "Lost connection with robot!", Toast.LENGTH_LONG).show();
                connectionState(false);
                connectionProcessor.setEnabled(false);
            }

        }
    }
}
