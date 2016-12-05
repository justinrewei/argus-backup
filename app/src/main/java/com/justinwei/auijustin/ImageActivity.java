package com.justinwei.auijustin;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.media.ExifInterface;
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
import android.graphics.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

            ExifInterface exif = new ExifInterface(JustinsUtility.getRealPathFromURI(this, imageUri));
            int actualImageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
            int actualImageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

            if (orientation != ExifInterface.ORIENTATION_NORMAL || actualImageWidth > MAX_WIDTH || actualImageHeight > MAX_HEIGTH) {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageUri = scaleAndRotateImageUri(bitmap, orientation);
            }
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

    //image exceeds max value either in width, or in height (or both).
    //We will scale it down and return the input stream.
    //   12/1/2016
    private static final int MAX_WIDTH = 850;
    private static final int MAX_HEIGTH = 850;
    private Uri scaleAndRotateImageUri(final Bitmap inputImage, final int orientation) throws IOException {
        int originalWidth = inputImage.getWidth();
        int originalHeight = inputImage.getHeight();

        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > MAX_WIDTH) {
            if (originalHeight > MAX_HEIGTH) {
                //too wide and too tall; need to figure out the correct ratio
                float ratio = ((float)MAX_WIDTH) / originalWidth;
                newHeight = (int)(originalHeight * ratio);
                if (newHeight > MAX_HEIGTH) {
                    ratio = ((float)MAX_HEIGTH) / originalHeight;
                }

                //now we have the correct ratio, we calculate the new width and height
                newWidth = (int)(originalWidth * ratio);
                newHeight = (int)(originalHeight * ratio);
            } else {
                //too wide only
                float ratio = ((float)MAX_WIDTH) / originalWidth;
                newWidth = MAX_WIDTH;
                newHeight = (int)(originalHeight *  ratio);
            }
        }
        else if (originalHeight > MAX_HEIGTH) {
            //too high only
            float ratio = ((float)MAX_HEIGTH) / originalHeight;
            newHeight = MAX_HEIGTH;
            newWidth = (int)(originalWidth * ratio);
        }

        //now we have the new width and height, we can scale it
        boolean filter = false;
        Bitmap scaledBitmap = inputImage.createScaledBitmap(inputImage, newWidth, newHeight, filter);

        //since we have scaled the image, we now have a new image width and height.
        //These values are used when displaying boxes returned by server.
        actualImageWidth = scaledBitmap.getWidth();
        actualImageHeight = scaledBitmap.getHeight();

        int rotationInDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationInDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationInDegrees = 270;
                break;
        }
        if (rotationInDegrees != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        }

        File tempOutputDir = this.getCacheDir();
        File tempOutputFile = File.createTempFile("scaled", "jpeg", tempOutputDir);
        OutputStream output = new FileOutputStream(tempOutputFile);
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        output.close();
        return Uri.fromFile(tempOutputFile);

    }


}

