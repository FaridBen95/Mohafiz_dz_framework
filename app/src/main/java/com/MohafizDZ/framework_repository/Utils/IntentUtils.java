package com.MohafizDZ.framework_repository.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;

public class IntentUtils {

    public static void openURLInBrowser(Context context, String url) {
        if (!url.equals("false") && !url.equals("")) {
            if (!url.contains("http")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        }
    }

    public static void startActivity(Context context, Class<?> activity_class, Bundle data) {
        Intent intent = new Intent(context, activity_class);
        if (data != null)
            intent.putExtras(data);
        context.startActivity(intent);
        if(MyAppCompatActivity.onChangeView != null){
            MyAppCompatActivity.onChangeView.openedClass(activity_class);
        }
    }

    public static void startActivity(Context context, Intent intent){
        if(intent != null){
            context.startActivity(intent);
        }
        try {
            if(MyAppCompatActivity.onChangeView != null){
                MyAppCompatActivity.onChangeView.openedClass(Class.forName(intent.getComponent().getClassName()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void redirectToMap(Context context, String location) {
        if (!location.equals("false") && !location.equals("")) {
            String map = "geo:0,0?q=" + location;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
            context.startActivity(intent);
        }
    }

    public static void redirectToMap(Context context, String longitude, String latitude) {
        if (!longitude.equals("false") && !longitude.equals("") &&
                !latitude.equals("false") && !latitude.equals("")) {
            String map = "geo:"+longitude+","+latitude; //+"?q=" + location;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
            context.startActivity(intent);
        }
    }

    public static void navigationToMap(Context context, String longitude, String latitude) {
        if (!longitude.equals("false") && !longitude.equals("") &&
                !latitude.equals("false") && !latitude.equals("")) {
//            String map = "google.navigation:"+longitude+","+latitude; //+"?q=" + location;
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
//            context.startActivity(intent);

//            String uri = "http://maps.google.com/maps?saddr="+longitude+","+latitude;//+"&daddr="+"43.572665,3.871447";
//            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
//            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//            context.startActivity(intent);

            String packageName = "com.google.android.apps.maps";
            String query = "google.navigation:q="+longitude+","+latitude;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
            intent.setPackage(packageName);
            try {
                context.startActivity(intent);
            } catch (Exception e){
                Toast.makeText(context, "L'application google maps n'est pas installé ! ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void requestMessage(Context context, String email) {
        if (!email.equals("false") && !email.equals("")) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.setData(Uri.parse("mailto:" + email));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void requestCall(Context context, String number) {
        if (!number.equals("false") && !number.equals("")) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            context.startActivity(intent);
        }
    }
}
