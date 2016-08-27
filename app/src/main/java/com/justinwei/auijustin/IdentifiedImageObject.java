package com.justinwei.auijustin;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by justinwei on 8/26/2016.
 */
public class IdentifiedImageObject {

    //upper left corner
    private int x0, y0;

    //lower right corner
    private int x1, y1;

    //tag
    private String tag;


    public void setUpperLeft(int x, int y){
        x0 = x;
        y0 = y;
    }

    public void setLowerRight(int x, int y){
        x1 = x;
        y1 = y;
    }

    public void setTag(String tag){
       this.tag = tag;
    }


    public int getY0() {
        return y0;
    }

    public int getX0() {
        return x0;
    }

    public int getWidth() {
        return x1;
    }

    public String getTag() {
        return tag;
    }

    public int getHeight() {
        return y1;
    }
}
