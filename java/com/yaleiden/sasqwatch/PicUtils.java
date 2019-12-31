package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * Created by Yale on 7/5/2015.
 */
public class PicUtils {

    public PicUtils() {

    }

    Bitmap byteToPic(String inImage) {
        byte[] pic = Base64.decode(inImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length);
        return bitmap;
    }

    String bitmapToByte(Bitmap bm) {
        String picArray = null;
        if (null != bm) {
            byte[] picByteArray;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
            picByteArray = stream.toByteArray();
            picArray = Base64.encodeToString(picByteArray, Base64.DEFAULT);
        }
        return picArray;
    }

    public Bitmap decodeUri(Uri selectedImage, Activity activity) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 150;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(selectedImage), null, o2);
    }
}
