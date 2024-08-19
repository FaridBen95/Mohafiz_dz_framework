package com.MohafizDZ.framework_repository.core;

import android.content.Context;
import android.graphics.Bitmap;

import com.MohafizDZ.framework_repository.Utils.BitmapUtils;

public class BitmapDataRow extends DataRow{
    private Bitmap bitmap = null;

    public Bitmap getBitmap(String imageKey, Context context){
        if(bitmap == null) {
            try {
                bitmap = BitmapUtils.getBitmapImage(getString(imageKey));
            }catch (Exception ignored){}
            try{
                bitmap = bitmap == null? BitmapUtils.getAlphabetImage(context, getString("name")) : bitmap;
            }catch (Exception ignored){}
        }
        return bitmap;
    }
}
