package com.gofirst.joshua.snowsquirrel;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.gofirst.joshua.snowsquirrel.Enums.PacketID;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFrag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private GraphView graph;

    private View view;

    private Button startPath;

    private ConnectionProcessor connectionProcessor;

    private LineGraphSeries<DataPoint> series;

    private RelativeLayout connectionLabelLayout;
    private TextView connectionLabel;

    private PathStorage paths;
    private ArrayAdapter<String> adapter;
    private Spinner path_spinner;
    List<String> spinnerOptions;

    private boolean is_auto_path_running;

    public HomeFrag() {
        // Required empty public constructor
    }

    public static HomeFrag newInstance(String param1, String param2) {
        HomeFrag fragment = new HomeFrag();
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
    public void onResume() {

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        graph = (GraphView)view.findViewById(R.id.graph);

        startPath = (Button)view.findViewById(R.id.start_path);
        connectionLabelLayout = (RelativeLayout)view.findViewById(R.id.connected_layout);
        connectionLabel = (TextView)view.findViewById(R.id.connected_text);

        Intent i = getActivity().getIntent();

        connectionProcessor = (ConnectionProcessor)i.getSerializableExtra("Connection");

        spinnerOptions =  new ArrayList<String>();
        spinnerOptions.add("");

        adapter = new ArrayAdapter<String>(
                this.getContext(), R.layout.spinner, spinnerOptions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        path_spinner = (Spinner) view.findViewById(R.id.path_chooser);
        path_spinner.setAdapter(adapter);

        path_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                showGraph(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

        connectionProcessor = (ConnectionProcessor)i.getSerializableExtra("Connection");
        connectionProcessor.setOnConnectionListener(
            new ConnectionListener() {
                @Override
                public void onConnected() {
                    if (isVisible()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setConnectionGUIState(true);
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
                                setConnectionGUIState(false);
                            }
                        });
                    }
                }
            }
        );

        setConnectionGUIState(connectionProcessor.getConnectionState());

        startPath.setOnClickListener(
            view -> {
                is_auto_path_running = !is_auto_path_running;

                int selected = path_spinner.getSelectedItemPosition();

                CommClass commClass = new CommClass();
                commClass.packetID = PacketID.PATH_CONTROL.ordinal();
                commClass.status = is_auto_path_running ? 1 : 0;
                commClass.data = new double[] { paths.DimensionArray[selected][0], paths.DimensionArray[selected][1] };

                ((MainActivity)getActivity()).sendData(commClass);

                setPathButtonState(is_auto_path_running);
            }
        );

        updateSpinner();
        showGraph(0);

        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);

        if (!hidden)
            updateSpinner();
    }


    private void showGraph(int index)
    {
        graph.removeAllSeries();

        double width = paths.DimensionArray[index][0];
        double length = paths.DimensionArray[index][1];

        LineGraphSeries<DataPoint> newSeries = new LineGraphSeries<>(
                new DataPoint[]{
                        new DataPoint(0,0),
                        new DataPoint(0, length),
                        new DataPoint(width,length),
                        new DataPoint(width,0)
                });

        double max;
        if (width > length)
            max = width;
        else
            max = length;

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(max);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(max);

        newSeries.setDrawBackground(true);

        graph.addSeries(newSeries);
    }

    private void updateSpinner()
    {
        SharedPreferences settings = getContext().getSharedPreferences(Constants.SETTINGS_LOCATION, Context.MODE_PRIVATE);
        String pathsString = settings.getString(Constants.PATH_LOCATION, "-1");
        if (pathsString.equals("-1"))
        {
            paths = new PathStorage();
        }
        else
        {
            Gson gson = new Gson();
            paths = gson.fromJson(pathsString, PathStorage.class);
        }

        spinnerOptions.clear();
        for (int index = 0; index<10;index++)
        {
            if (paths.Names[index] == null)
                spinnerOptions.add((index+1)+")");
            else
                spinnerOptions.add((index+1)+") "+paths.Names[index]);
        }

        adapter.notifyDataSetChanged();
    }

    public void setConnectionGUIState(boolean connected)
    {
        startPath.setEnabled(connected);

        if (connected)
        {
            connectionLabelLayout.setBackgroundResource(R.color.colorEnable);
            connectionLabel.setText(R.string.connected_text);

            setPathButtonState(connectionProcessor.isPathEnabled());

        }
        else
        {
            startPath.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorDisable));
            startPath.setText(R.string.path_not_connected_text);
            connectionLabelLayout.setBackgroundResource(R.color.colorDisable);
            connectionLabel.setText(R.string.disconnected_text);
            path_spinner.setEnabled(true);
            connectionProcessor.setPathEnabled(false);
        }
    }

    private void setPathButtonState(boolean state)
    {
        connectionProcessor.setPathEnabled(state);

        if (state)
        {
            startPath.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorDisable));
            startPath.setText(R.string.stop_path_text);
            path_spinner.setEnabled(false);
        }
        else
        {
            startPath.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorEnable));
            startPath.setText(R.string.start_path_text);
            path_spinner.setEnabled(true);
        }
    }

}
