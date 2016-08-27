package com.justinwei.auijustin;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.net.URL;
import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity implements ImageTaskDelegate{

    public static int x1, y1, x2, y2;
    private DrawView capturedPhoto;
    private Uri selectedImage;
    private static final String TAG = "ImageActivity";
    Paint red;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        capturedPhoto = (DrawView) findViewById(R.id.capturedPhoto);

        Uri imageUri = getIntent().getParcelableExtra("imageUri");
        capturedPhoto.setImageURI(imageUri);
        selectedImage = imageUri;

        final Button btnIdentifyObjects = (Button) findViewById(R.id.btnIdentifyObjects);
        btnIdentifyObjects.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageTask imageTask = new ImageTask(ImageActivity.this, ImageActivity.this);
                imageTask.execute(selectedImage);

            }
        });



        /*
        Bitmap imageBitmap = getIntent().getParcelableExtra("imageBitmap");
        if (imageBitmap != null)
            capturedPhoto.setImageBitmap(imageBitmap);
        else {

        }
        */

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        /*
        x1 = 98;

        y1 = 451;
        x2 = 643;
        y2 = 972;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(y2 - y1 - capturedPhoto.getHeight(), x2 - x1 - capturedPhoto.getWidth());
        layoutParams.setMargins(0,0,0,0);
        final ImageView boundingBox = (ImageView) findViewById(R.id.boundingBox);
        boundingBox.setLayoutParams(layoutParams);
        */




    }


    @Override
    public void taskCompletionResult(ArrayList<IdentifiedImageObject> result) {
        ArrayList<IdentifiedImageObject> boxes = result;
        showBoxes(boxes);
        capturedPhoto.setBoxes(boxes);
    }

    private void showBoxes(ArrayList<IdentifiedImageObject> boxes){

        for (IdentifiedImageObject identifiedImageObject: boxes) {
            Log.d(TAG, identifiedImageObject.getTag());
        }
    }

}

