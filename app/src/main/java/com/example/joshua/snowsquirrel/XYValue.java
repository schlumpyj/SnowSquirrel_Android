package com.example.joshua.snowsquirrel;

/**
 * Created by Joshua on 3/16/2018.
 */

public class XYValue {

    private double X, Y;

    public XYValue(double X, double Y){
        this.X = X;
        this.Y = Y;
    }

    public double getX(){
        return X;
    }
    public double getY(){
        return Y;
    }

    public void setX(double value){
        X = value;
    }
    public void setY(double value){
        Y = value;
    }
}
