package com.example.joshua.snowsquirrel;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

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

    private MyView canvas;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);





        final RelativeLayout test = (RelativeLayout)view.findViewById(R.id.canvas_test);




        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("Triple I");
        spinnerArray.add("Single I");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this.getContext(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) view.findViewById(R.id.path_chooser);
        sItems.setAdapter(adapter);

        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                ArrayList<Integer[]> yes = new ArrayList<>();
;
                if (position==1){
                    test.removeView(canvas);
                    yes.clear();
                    yes.add(new Integer[]{ 0, 100});
                    yes.add(new Integer[]{ 30, 100});
                    yes.add(new Integer[]{ 30, 0});
                    yes.add(new Integer[]{ 10, 0});

                    canvas = new MyView(getContext(), yes, test);

                    test.addView(canvas);
                }
                else{
                    test.removeView(canvas);

                    yes.clear();

                    for (int i = 0; i<100; i+=20){
                        yes.add(new Integer[]{ i, 0});
                        yes.add(new Integer[]{ i, 100});
                        yes.add(new Integer[]{ i+10, 100});
                        yes.add(new Integer[]{ i+10, 0});
                    }

                    canvas = new MyView(getContext(), yes, test);

                    test.addView(canvas);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        return view;
    }

    public class MyView extends View {
        ArrayList<Integer[]> points;
        Integer[] start;
        int x, y;

        public MyView(Context context, ArrayList<Integer[]> points, RelativeLayout relativeLayout) {
            super(context);
            // TODO Auto-generated constructor stub
            this.points = points;
            start = new Integer[]{ 0, 0 };
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            int x = getWidth();
            int y = getHeight() - 300;

            Paint p1 = new Paint();
            p1.setAntiAlias(true);
            p1.setColor(Color.BLACK);
            p1.setStrokeWidth(20);

            Integer[] result = findMax();
            float other, multiX, multiY;
            if (result[0]>result[1]){
                other = result[0]/result[1];
                multiX = x/result[0];
                multiY = (y/result[1])*other;
            }
            else{
                other = result[1]/result[0];
                multiX = x/result[0]*(1/other);
                multiY = (y/result[1]);
            }



            for (Integer[] pointPair: points){
                canvas.drawLine(start[0]*multiX, start[1]*multiY, pointPair[0]*multiX, pointPair[1]*multiY, p1);
                start = pointPair;
            }
        }

        private Integer[] findMax(){
            int largeX = 0;
            int largeY = 0;

            for (Integer[] pointPair: points) {
                if (pointPair[0]>largeX)
                    largeX = pointPair[0];
                if (pointPair[1]>largeY)
                    largeY = pointPair[1];
            }
            return new Integer[]{ largeX, largeY };

        }
    }

}
