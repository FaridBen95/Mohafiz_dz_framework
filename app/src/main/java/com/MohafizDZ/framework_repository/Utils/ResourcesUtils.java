package com.MohafizDZ.framework_repository.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class ResourcesUtils {
    public static String string(Context context, int res_id) {
        return context.getResources().getString(res_id);
    }

    public static Integer dimen(Context context, int res_id) {
        return (int) context.getResources().getDimension(res_id);
    }

    public static int color(Context context, int res_id) {
        return context.getResources().getColor(res_id);
    }

    public static Drawable drawable(Context context, int res_id){
        return context.getResources().getDrawable(res_id);
    }
}
