package com.gofirst.ion.snowsquirrel;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.gofirst.ion.snowsquirrel.R;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PathSetter#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PathSetter extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GraphView graph;
    private Spinner path_selector;

    private EditText width_coord, length_coord, name;

    private Button save;
    private PathStorage paths;

    private List<String> spinnerOptions;
    private ArrayAdapter<String> adapter;

    public PathSetter() {
        // Required empty public constructor
    }

    public static PathSetter newInstance(String param1, String param2) {
        PathSetter fragment = new PathSetter();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_path_setter, container, false);

        graph = (GraphView)view.findViewById(R.id.graphBuilder);
        path_selector = (Spinner)view.findViewById(R.id.path_edit_chooser);

        spinnerOptions =  new ArrayList<String>();
        spinnerOptions.add("");

        adapter = new ArrayAdapter<String>(
                this.getContext(), R.layout.spinner, spinnerOptions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        path_selector.setAdapter(adapter);

        width_coord = (EditText)view.findViewById(R.id.width_coord);
        length_coord = (EditText)view.findViewById(R.id.length_coord);
        name = (EditText)view.findViewById(R.id.path_name);

        save = (Button)view.findViewById(R.id.save_button);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double width = Double.parseDouble(width_coord.getText().toString());
                double length = Double.parseDouble(length_coord.getText().toString());

                double[] newData = new double[] {width, length};
                int index = path_selector.getSelectedItemPosition();
                paths.DimensionArray[index] = newData;
                paths.Names[index] = name.getText().toString();

                showGraph(index);

                SharedPreferences settings = getContext().getSharedPreferences(Constants.SETTINGS_LOCATION, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit= settings.edit();

                edit.putString(Constants.PATH_LOCATION, new Gson().toJson(paths));
                edit.apply();
            }
        });

        path_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                showGraph(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        width_coord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try
                {
                    Double.parseDouble(width_coord.getText().toString());
                    save.setEnabled(true);
                }
                catch (NumberFormatException e)
                {
                    save.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        length_coord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try
                {
                    Double.parseDouble(length_coord.getText().toString());
                    save.setEnabled(true);
                }
                catch (NumberFormatException e)
                {
                    save.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


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

        showGraph(0);

        return view;
    }

    private void showGraph(int index)
    {
        updateSpinner();

        graph.removeAllSeries();

        double width = paths.DimensionArray[index][0];
        double length = paths.DimensionArray[index][1];
        String nameFromStorage = paths.Names[index];

        if (nameFromStorage != null)
            name.setText(nameFromStorage);
        else
            name.setText("");

        width_coord.setText(Double.toString(width));
        length_coord.setText(Double.toString(length));

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


}
