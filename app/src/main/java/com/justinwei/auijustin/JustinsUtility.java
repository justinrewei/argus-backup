package com.justinwei.auijustin;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.Context;
/**
 * Created by justinwei on 12/5/2016.
 */

public class JustinsUtility {

    // Convert the image URI to the direct file system path of the image file
    public static String getRealPathFromURI(final Context context, final Uri ac_Uri )
    {
        String result = "";
        boolean isok = false;

        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(ac_Uri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            isok = true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isok ? result : "";
    }
}
