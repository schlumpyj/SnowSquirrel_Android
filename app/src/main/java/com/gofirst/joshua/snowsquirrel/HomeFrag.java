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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GraphView graph;

    private View view;

    private Button startPath;

    private ConnectionProcessor connectionProcessor;

    private LineGraphSeries<DataPoint> series;

    private PathStorage paths;
    private ArrayAdapter<String> adapter;
    List<String> spinnerOptions;

    public HomeFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFrag.
     */
    // TODO: Rename and change types and number of parameters
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

        setConnectionGUIState(connectionProcessor.getConnectionState());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        graph = (GraphView)view.findViewById(R.id.graph);

        startPath = (Button)view.findViewById(R.id.start_path);
        startPath.setText("START PATH");
        startPath.setEnabled(true);
        startPath.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.colorEnable));

        Intent i = getActivity().getIntent();

        connectionProcessor = (ConnectionProcessor)i.getSerializableExtra("Connection");

        spinnerOptions =  new ArrayList<String>();
        spinnerOptions.add("");

        adapter = new ArrayAdapter<String>(
                this.getContext(), R.layout.spinner, spinnerOptions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) view.findViewById(R.id.path_chooser);
        sItems.setAdapter(adapter);

        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                showGraph(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

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
        SharedPreferences settings = getContext().getSharedPreferences(MainActivity.SETTINGS_LOCATION, Context.MODE_PRIVATE);
        String pathsString = settings.getString(MainActivity.PATH_LOCATION, "-1");
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
        if (connected)
        {
            ((RelativeLayout)view.findViewById(R.id.connected_layout)).setBackgroundResource(R.color.colorEnable);
            ((TextView)view.findViewById(R.id.connected_text)).setText("Connected!");
        }
        else
        {
            ((RelativeLayout)view.findViewById(R.id.connected_layout)).setBackgroundResource(R.color.colorAccent);
            ((TextView)view.findViewById(R.id.connected_text)).setText("Not Connected!");
        }
    }

}
