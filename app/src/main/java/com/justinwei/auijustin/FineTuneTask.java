/**
 * Created by justinwei on 12/5/2016.
 */
package com.justinwei.auijustin;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class FineTuneTask extends AsyncTask<Uri, Void, ArrayList<IdentifiedImageObject>> {

    private static final String FINE_TUNE_CMD = "http://130.245.169.183/IS_argus/modified_api.php?op=learn_features";


    private Context myContext;
    private String boundary = "------WebKitFormBoundaryChxiHEU3U8MFB0zg\r\n";
    private String lastBoundary = "------WebKitFormBoundaryChxiHEU3U8MFB0zg--\r\n";
    private String response, line;
    private static final String TAG = "ImageTask";
    private ArrayList<IdentifiedImageObject> boxes = new ArrayList<>();
    private ImageTaskDelegate delegate;

    public FineTuneTask(Context context, ImageTaskDelegate delegate) {
        myContext = context;
        this.delegate = delegate;
    }

    // max image width and height that we will upload;
    //if image exceeds either max value, we will scale it down
    private final int  MAX_WIDTH = 800;
    private final int MAX_HEIGTH = 800;

    @Override
    protected ArrayList<IdentifiedImageObject> doInBackground(Uri... uris) {

        try {
            Uri image = uris[0];
            String path = image.getEncodedPath();

            //what is the size of the image? If it is too big, scale it down to
            //get faster upload time.  12/1/2016
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(myContext.getContentResolver(), image);
            InputStream fileInputStream = null; // = myContext.getContentResolver().openInputStream(image);

            fileInputStream = myContext.getContentResolver().openInputStream(image);

            URL url = new URL(FINE_TUNE_CMD);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            //write HTTP headers
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundaryChxiHEU3U8MFB0zg");
            urlConnection.setUseCaches(false); // Don't use a Cached Copy
            urlConnection.setRequestMethod("POST");

            DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());

            dos.writeBytes(boundary);
            dos.writeBytes("Content-Disposition: form-data; name=\"test_image\"; filename=\"IMG_3457.JPG\"\r\n");
            dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");

            //write image
            int bytesAvailable = fileInputStream.available();
            int bufferSize = Math.min(bytesAvailable, 4096);
            byte[] buffer = new byte[bufferSize];
            // read file and write it into form...
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0)
            {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, 4096);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            //finish writing image

            dos.writeBytes("\r\n" + boundary); //2nd boundary
            //write model selection parameters
            dos.writeBytes("Content-Disposition: form-data; name=\"model_selection_parameters\"\r\n\r\n");
            dos.writeBytes("{\"iterations\":\"default\",\"learning_rate\":\"default\",\"num_images\":\"default\"}\r\n");

            dos.writeBytes(lastBoundary);
            dos.flush();
            dos.close();
            fileInputStream.close();

            //READ RESPONSE
            DataInputStream dis = new DataInputStream(urlConnection.getInputStream());

            BufferedReader r = new BufferedReader(new InputStreamReader(dis));
            StringBuilder total = new StringBuilder();
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            response = total.toString();
            //Log.d(TAG, "Response: " + response);


            //////////////////JSON/////////////////
            JSONObject obj = new JSONObject(response);
            Log.d(TAG, "JSON Object: " + obj.toString());

            //outside labels array
            JSONArray labelsArray = obj.getJSONArray("labels");
            for (int i=0; i<labelsArray.length(); i++){

                JSONObject labelsObject = labelsArray.getJSONObject(i);

                //inside tag object
                String tag = labelsObject.getString("tag");

                //inside box array
                JSONArray boxArray = labelsObject.getJSONArray("box");

                //upper left coordinates for box
                JSONArray upperLeftCoordinates = boxArray.getJSONArray(0);

                //lower right coordinates for box
                JSONArray lowerRightCoordinates = boxArray.getJSONArray(1);

                //inside upper left coordinates array
                Integer upperLeftCoordinateX = upperLeftCoordinates.getInt(1);
                Integer upperLeftCoordinateY = upperLeftCoordinates.getInt(0);

                //inside lower right coordinates array
                Integer width = lowerRightCoordinates.getInt(0);
                Integer height = lowerRightCoordinates.getInt(1);

                Log.d(TAG, "JSON Label: " + labelsObject);
                Log.d(TAG, "upperLeftCoordinate: " + upperLeftCoordinates);
                //Log.d(TAG, "lowerRightCoordinate: " + lowerRightCoordinates);
                Log.d(TAG, "upperLeftCoordinateX: " + upperLeftCoordinateX);
                Log.d(TAG, "upperLeftCoordinateY: " + upperLeftCoordinateY);
                Log.d(TAG, "Width: " + width);
                Log.d(TAG, "Height: " + height);
                Log.d(TAG, "Tag: " + tag);


                //create instance of IdentifiedImageObject
                IdentifiedImageObject identifiedImageObject = new IdentifiedImageObject();
                identifiedImageObject.setUpperLeft(upperLeftCoordinateX, upperLeftCoordinateY);
                identifiedImageObject.setBoxWidthandHeight(width, height);

                identifiedImageObject.setTag(tag);

                boxes.add(identifiedImageObject);


            }

            Log.d(TAG, "Number of boxes: " + boxes.size());


            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(myContext, "Error contacting server " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(myContext, "Error parsing result from server " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            Log.e("TAG", "Could not parse malformed JSON: \"" + response + "\"");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(myContext, "Error contacting server " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        return boxes;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute( ArrayList<IdentifiedImageObject> identifiedImageObject) {
        super.onPostExecute(identifiedImageObject);
        delegate.taskCompletionResult(boxes);


    }


}
