package com.gofirst.ion.snowsquirrel;

/**
 * Created by server on 11/23/18.
 */

public class PathStorage
{
    public double[][] DimensionArray;
    public String[] Names;

    public PathStorage()
    {
        DimensionArray = new double[10][];

        for (int index = 0; index<10; index++)
            DimensionArray[index] = new double[2];

        Names = new String[10];
    }
}
