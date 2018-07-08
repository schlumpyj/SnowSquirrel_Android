package com.gofirst.joshua.snowsquirrel;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


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

    private EditText x_coord, y_coord;

    private Button add, save;

    private double maxX, maxY;

    public PathSetter() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PathSetter.
     */
    // TODO: Rename and change types and number of parameters
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

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
            new DataPoint(0,0)
        });
        graph.addSeries(series2);

        SharedPreferences settings = getContext().getSharedPreferences("snowsquirrelPaths", 0);
        SharedPreferences.Editor editor = settings.edit();

        x_coord = (EditText)view.findViewById(R.id.x_coord);
        y_coord = (EditText)view.findViewById(R.id.y_coord);

        add = (Button)view.findViewById(R.id.add_button);
        save = (Button)view.findViewById(R.id.save_button);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double x = Double.parseDouble(x_coord.getText().toString());
                double y = Double.parseDouble(y_coord.getText().toString());

                if (x>maxX) maxX = x;
                if (y>maxY) maxY = y;
                series2.appendData(new DataPoint(x, y),
                        false, 100);

                graph.getViewport().setMinX(maxX);
                graph.getViewport().setMinY(maxY);

                graph.addSeries(series2);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Alert with the ability to set name of path
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                final EditText editText = new EditText(getContext());
                builder.setMessage("Enter in a name for this Path");
                builder.setTitle("Name the Path");

                builder.setView(editText);

                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: need to figure out how to save all the arrays
                    }
                });

                builder.show();
            }
        });

        return view;
    }


}
