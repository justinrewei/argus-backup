package com.justinwei.auijustin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by justinwei on 8/26/2016.
 */
class DrawView extends ImageView {

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
        paint.setColor(Color.GREEN);
        paint.setAlpha(100);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(10);
        float leftx = 98;
        float topy = 302;
        float rightx = 170;
        float bottomy = 231;
        canvas.drawRect(leftx, topy, rightx, bottomy, paint);
    }
}