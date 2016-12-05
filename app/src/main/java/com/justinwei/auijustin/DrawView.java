package com.justinwei.auijustin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by justinwei on 8/26/2016.
 */
class DrawView extends ImageView {

    private static String TAG = "ImageView";
    private String tag;
    private ArrayList<IdentifiedImageObject> boxes;
    private int actualImageWidth, actualImageHeight;
    public DrawView(Context context) {
        super(context);


    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
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


        float ih=this.getMeasuredHeight();//height of imageView
        float iw=this.getMeasuredWidth();//width of imageView
        int iH=this.getDrawable().getIntrinsicHeight();//original height of underlying image
        int iW=this.getDrawable().getIntrinsicWidth();//original width of underlying image

        float ratio;
        if (ih/iH<=iw/iW) ratio=ih/iH;//rescaled width of image within ImageView
        else ratio= iw/iW;//rescaled height of image within ImageView


        if (boxes != null) {
            for (IdentifiedImageObject identifiedImageObject : boxes) {
                float leftx = identifiedImageObject.getX0(); //98;
                float topy = identifiedImageObject.getY0(); //302;
                float width = identifiedImageObject.getWidth();
                float height = identifiedImageObject.getHeight();
                String tag = identifiedImageObject.getTag();
                //Log.d(TAG, getMeasuredHeight() + " is the measured height and " + getMeasuredWidth() + " is the measured width.");

                //need to get the actual image width display in image view
                //as in portraint mode, the width of image is not the same as
                //the width of the image view.   12/4/2016

                //int getMeasuredWidth = getMeasuredWidth();

                int getMeasuredWidth = getImageWidthInView();
                float scaledPictureRatio = (float)getMeasuredWidth / actualImageWidth;

                float newLeftX = leftx*scaledPictureRatio;
                float newTopY = topy*scaledPictureRatio;
                float newWidth = width*scaledPictureRatio;
                float newHeight = height*scaledPictureRatio;
                Log.d(TAG, "Image actual size: " + actualImageWidth + "x" + actualImageHeight);
                Log.d(TAG, "Scale ratio: " + scaledPictureRatio);

                    canvas.drawRect(newLeftX, newTopY, (newLeftX + newWidth), (newTopY + newHeight), paint);

                canvas.drawText("" + tag, newLeftX, newTopY + 35, textPaint);
            }
        }
    }

    public void setBoxes(ArrayList<IdentifiedImageObject> boxes){
        this.boxes = boxes;

        //android redraws DrawView
        this.invalidate();
    }


    public void setActualImageWidthAndHeight(int width, int height){
        actualImageWidth = width;
        actualImageHeight = height;
    }

    private int getImageWidthInView() {
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

        int getMeasuredWidth = actW;
        //int w = getMeasuredWidth();
        Log.d(TAG, "on screen image width is "  + actW);
        Log.d(TAG, "on screen image height is "  + actH);

        return getMeasuredWidth;
    }
}