package com.justinwei.auijustin;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by justinwei on 8/9/2016.
 */
public class ImageTask extends AsyncTask<Uri, Void, ArrayList<IdentifiedImageObject>> {


    private Context myContext;
    private String boundary = "------WebKitFormBoundaryChxiHEU3U8MFB0zg\r\n";
    private String lastBoundary = "------WebKitFormBoundaryChxiHEU3U8MFB0zg--\r\n";
    private String response, line;
    private static final String TAG = "ImageTask";
    private ArrayList<IdentifiedImageObject> boxes = new ArrayList<>();
    private ImageTaskDelegate delegate;

    public ImageTask(Context context, ImageTaskDelegate delegate) {
        myContext = context;
        this.delegate = delegate;
    }


    @Override
    protected ArrayList<IdentifiedImageObject> doInBackground(Uri... uris) {
        /*HttpPost httpPost = new HttpPost("http://130.245.169.183/IS_argus/modified_api.php?op=identify_objects");
        Uri image = uris[0];
        File imageFile = new File((image.getPath()));

        ContentType type = ContentType.create("image/jpeg");
        ContentBody cbFile = new FileBody(imageFile, type);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("image", cbFile);

        HttpEntity entity = builder.build();
        httpPost.setEntity(entity);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        try {
            HttpResponse response = clientBuilder.build().execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }*/



        try {
            Uri image = uris[0];
            String path = image.getEncodedPath();

            InputStream fileInputStream = myContext.getContentResolver().openInputStream(image);

            URL url = new URL("http://130.245.169.183/IS_argus/modified_api.php?op=identify_objects");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            //urlConnection.setChunkedStreamingMode(0);
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
                Integer upperLeftCoordinateX = upperLeftCoordinates.getInt(0);
                Integer upperLeftCoordinateY = upperLeftCoordinates.getInt(1);

                //inside lower right coordinates array
                Integer lowerRightCoordinateX = lowerRightCoordinates.getInt(0);
                Integer lowerRightCoordinateY = lowerRightCoordinates.getInt(1);

                Log.d(TAG, "JSON Label: " + labelsObject);
                Log.d(TAG, "upperLeftCoordinate: " + upperLeftCoordinates);
                Log.d(TAG, "lowerRightCoordinate: " + lowerRightCoordinates);
                Log.d(TAG, "upperLeftCoordinateX: " + upperLeftCoordinateX);
                Log.d(TAG, "upperLeftCoordinateY: " + upperLeftCoordinateY);
                Log.d(TAG, "lowerRightCoordinateX: " + lowerRightCoordinateX);
                Log.d(TAG, "lowerRightCoordinateY: " + lowerRightCoordinateY);
                Log.d(TAG, "Tag: " + tag);


                //create instance of IdentifiedImageObject
                IdentifiedImageObject identifiedImageObject = new IdentifiedImageObject();
                identifiedImageObject.setUpperLeft(upperLeftCoordinateX, upperLeftCoordinateY);
                identifiedImageObject.setLowerRight(lowerRightCoordinateX, lowerRightCoordinateY);
                identifiedImageObject.setTag(tag);

                boxes.add(identifiedImageObject);


            }

            Log.d(TAG, "Number of boxes: " + boxes.size());


            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("TAG", "Could not parse malformed JSON: \"" + response + "\"");
        }

        return boxes;

    }




    private void writeStream(File file, OutputStream out) {

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
