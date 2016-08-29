package com.justinwei.auijustin;

/**
 * Created by justinwei on 8/26/2016.
 */
public class IdentifiedImageObject {

    //upper left corner
    private int x0, y0;

    //lower right corner
    private int width, height;

    //tag
    private String tag;


    public void setUpperLeft(int x, int y){
        x0 = x;
        y0 = y;
    }

    public void setBoxWidthandHeight(int x, int y){
        width = x;
        height = y;
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
        return width;
    }

    public String getTag() {
        return tag;
    }

    public int getHeight() {
        return height;
    }
}
