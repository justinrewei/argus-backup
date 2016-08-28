package com.justinwei.auijustin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by justinwei on 8/26/2016.
 */
class DrawView extends ImageView {

    private String tag;
    private ArrayList<IdentifiedImageObject> boxes;
    public DrawView(Context context) {
        super(context);
    }

    DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAlpha(90);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(10);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(60);




        if (boxes != null) {
            for (IdentifiedImageObject identifiedImageObject : boxes) {
                float leftx = identifiedImageObject.getY0(); //98;
                float topy = identifiedImageObject.getX0(); //302;
                float width = identifiedImageObject.getWidth();
                float height = identifiedImageObject.getHeight();
                String tag = identifiedImageObject.getTag();
                //canvas.drawRect(380, 125, 575, 400, paint); //dog coordinates

                //canvas.drawRect(100, 100, 300, 200, paint); //dog coordinates
                canvas.drawRect(leftx, topy, leftx + width, topy + height, paint);
                canvas.drawText("" + tag, leftx, topy + 35, textPaint);
                //canvas.drawRect(leftx/(float)2.4533334, (491-topy)/(float)2.4533334, (leftx+width)/(float)2.4533334, (491-topy+height)/(float)2.4533334, paint);
            }

       /*
        float leftx = 302; //98;
        float topy = 98; //302;
        float width = 170;
        float height = 231;
        //canvas.drawRect(380, 125, 575, 400, paint); //dog coordinates

        float scale = (float)2.4533334;
        //canvas.drawRect(100, 100, 300, 200, paint); //dog coordinates
        canvas.drawRect(leftx, topy, leftx+width, topy+height, paint);
        //canvas.drawRect(leftx/(float)2.4533334, (491-topy)/(float)2.4533334, (leftx+width)/(float)2.4533334, (491-topy+height)/(float)2.4533334, paint);
    */
        }
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get image matrix values and place them in an array
        float[] f = new float[9];
        getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        Log.e("DBG", "["+origW+","+origH+"] -> ["+actW+","+actH+"] & scales: x="+scaleX+" y="+scaleY);
    }

    public void setBoxes(ArrayList<IdentifiedImageObject> boxes){
        this.boxes = boxes;

        //android redraws DrawView
        this.invalidate();
    }

}