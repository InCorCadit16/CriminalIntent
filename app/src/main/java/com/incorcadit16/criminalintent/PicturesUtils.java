package com.incorcadit16.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class PicturesUtils {
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeigth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int sampleSize = 1;
        if (srcWidth > destWidth || srcHeight > destHeigth) {
            float scaleWidth = srcWidth/destWidth;
            float scaleHeight = srcHeight/destHeigth;

            sampleSize = Math.round(scaleWidth > scaleHeight? scaleWidth : scaleHeight);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(path,options);


    }


    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        return getScaledBitmap(path,size.x, size.y);
    }
}
