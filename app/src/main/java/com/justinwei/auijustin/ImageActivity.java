package com.justinwei.auijustin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity implements ImageTaskDelegate{

    private DrawView capturedPhoto;
    private Uri selectedImage;
    private static final String TAG = "ImageActivity";
    private int actualImageHeight, actualImageWidth;

    Paint red;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        capturedPhoto = (DrawView) findViewById(R.id.capturedPhoto);

        Uri imageUri = getIntent().getParcelableExtra("imageUri");


        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            actualImageWidth = bitmap.getWidth();
            actualImageHeight = bitmap.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }


        capturedPhoto.setImageURI(imageUri);

        selectedImage = imageUri;

        final Button btnIdentifyObjects = (Button) findViewById(R.id.btnIdentifyObjects);
        btnIdentifyObjects.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageTask imageTask = new ImageTask(ImageActivity.this, ImageActivity.this);
                imageTask.execute(selectedImage);

            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    public void taskCompletionResult(ArrayList<IdentifiedImageObject> result) {
        ArrayList<IdentifiedImageObject> boxes = result;
        showBoxes(boxes);
        capturedPhoto.setBoxes(boxes);
        if (boxes.size() == 0) {
            Toast.makeText(this, "No objects identified.", Toast.LENGTH_SHORT).show();
        } else if (boxes.size() >= 1) {
            Toast.makeText(this, "Objects identified.", Toast.LENGTH_SHORT).show();
        }

        capturedPhoto.setActualImageWidthAndHeight(actualImageWidth, actualImageHeight);


    }

    private void showBoxes(ArrayList<IdentifiedImageObject> boxes){

        for (IdentifiedImageObject identifiedImageObject: boxes) {
            Log.d(TAG, identifiedImageObject.getTag());
        }
    }
}

