package com.MohafizDZ.framework_repository.Utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.framework_repository.core.Account.LauncherActivity;
import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.core.DataRow;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.framework_repository.service.receiver.NotificationPublisher;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

public class MyUtil {
    public static final String DEFAULT_LANGUAGE = "not_provided";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_SHARE_DATE_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String CUSTOM_DATE_SHOWING_FORMAT = "EEEE, dd MMMM";

    public static String caller(){
        try{
            throw new Exception("the caller is");
        }
        catch (Exception e){
            return e.getStackTrace()[0].getFileName().replace(".java","");
        }
    }

    public static String getCurrentDate(){
        SimpleDateFormat gmtFormat = new SimpleDateFormat();
        gmtFormat.applyPattern(DEFAULT_DATE_TIME_FORMAT);
        TimeZone gmtTime =  TimeZone.getDefault();
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(new Date());
    }

    public static String getStringFromDate(Date date, String dateFormat){
        SimpleDateFormat gmtFormat = new SimpleDateFormat();
        gmtFormat.applyPattern(dateFormat);
        TimeZone gmtTime =  TimeZone.getDefault();
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(date);
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static Map<String, Object> rowsToMap(List<DataRow> rows){
        Map<String, Object> map = new HashMap<>();
        for(DataRow row : rows){
            map.put(row.get(Col.SERVER_ID).toString(), row);
        }
        return map;
    }

    /**
     * Create Date instance from given date string.
     *
     * @param date               date in string
     * @param dateFormat,        original date format
     * @param hasDefaultTimezone if date is in default timezone than true, otherwise false
     * @return Date, returns Date object with given date
     */
    public static Date createDateObject(String date, String dateFormat, Boolean hasDefaultTimezone) {
        Date dateObj = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            if (!hasDefaultTimezone) {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
            dateObj = simpleDateFormat.parse(date);
        } catch (Exception e) {
            Log.e("Create_date_object", e.getMessage());
        }
        return dateObj;
    }

    public static String repeat(String string, int repeat) {
        StringBuffer str = new StringBuffer();
        if(repeat >= 0) {
            for (int i = 0; i < repeat; i++)
                str.append(string);
        }
        return str.toString();
    }

    public static long dateToMilliSec(String date){
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
        long timeInMilliseconds = 0;
        try {
            Date mDate = sdf.parse(date);
            timeInMilliseconds = mDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeInMilliseconds;
    }

    public static String milliSecToDate(long milliSec){
        return milliSecToDate(milliSec, DEFAULT_DATE_TIME_FORMAT);
    }

    public static String milliSecToDate(long milliSec, String dateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSec);
        return sdf.format(calendar.getTime()).toString();
    }

    public static File saveImage(final Context context, final String imageData) {
        final byte[] imgBytesData = android.util.Base64.decode(imageData,
                android.util.Base64.DEFAULT);

        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            file = File.createTempFile("image", ".jpg", context.getCacheDir());
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert fileOutputStream != null;
        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                fileOutputStream);
        try {
            bufferedOutputStream.write(imgBytesData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static byte[] bytesFromFile(File file){
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    public static String uniqid(String prefix, boolean more_entropy) {
        long time = System.currentTimeMillis();
        //String uniqid = String.format("%fd%05f", Math.floor(time),(time-Math.floor(time))*1000000);
        //uniqid = uniqid.substring(0, 13);
        String uniqid = "";
        if(!more_entropy)
        {
            uniqid = String.format("%s%08x%05x", prefix, time/1000, time);
        }else
        {
            SecureRandom sec = new SecureRandom();
            byte[] sbuf = sec.generateSeed(8);
            ByteBuffer bb = ByteBuffer.wrap(sbuf);

            uniqid = String.format("%s%08x%05x", prefix, time/1000, time);
            uniqid += "." + String.format("%.8s", ""+bb.getLong()*-1);
        }


        return uniqid ;
    }

    public static File saveToFile(Context context, String urlLink){
        File outputFile = new File(context.getCacheDir(), MyUtil.uniqid("img", true)+ ".jpg");
        InputStream input = null;
        try {
            URL url = new URL (urlLink);
            input = url.openStream();

            OutputStream output = new FileOutputStream (outputFile);
            try {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                    output.write(buffer, 0, bytesRead);
                }
            }catch(Exception e){
                e.printStackTrace();
                outputFile = null;
            }
            finally {
                output.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
            outputFile = null;
        } finally {
            try {
                if(input != null){
                    input.close();
                }
            } catch (IOException e) {
                outputFile = null;
                e.printStackTrace();
            }
        }
        return outputFile;
    }

    public static File downloadPicture(Context context, final String url) {
        return downloadPicture(context, url, null);
    }

    public static File downloadPicture(Context context, final String url, final DownloadListener downloadListener){
        final File destination = new File(context.getCacheDir(), MyUtil.uniqid("img", true)+ ".jpg");
//        final final destination = MyUtil.uniqid("img", true)+ ".jpg";
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(destination));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                return destination;
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                if(downloadListener != null){
                    downloadListener.onDownloadFinished(destination);
                }
            }
        }.execute();
        return destination;
    }

    public static Map<Character, Character> getAccentsMap(){
        Map<Character, Character> accentsMap = new HashMap<>();
        accentsMap.put('À', 'A');
        accentsMap.put('Á', 'A');
        accentsMap.put('Â', 'A');
        accentsMap.put('Ã', 'A');
        accentsMap.put('Ä', 'A');
        accentsMap.put('È', 'E');
        accentsMap.put('É', 'E');
        accentsMap.put('Ê', 'E');
        accentsMap.put('Ë', 'E');
        accentsMap.put('Í', 'I');
        accentsMap.put('Ì', 'I');
        accentsMap.put('Î', 'I');
        accentsMap.put('Ï', 'I');
        accentsMap.put('Ù', 'U');
        accentsMap.put('Ú', 'U');
        accentsMap.put('Û', 'U');
        accentsMap.put('Ü', 'U');
        accentsMap.put('Ò', 'O');
        accentsMap.put('Ó', 'O');
        accentsMap.put('Ô', 'O');
        accentsMap.put('Õ', 'O');
        accentsMap.put('Ö', 'O');
        accentsMap.put('Ñ', 'N');
        accentsMap.put('Ç', 'C');
        accentsMap.put('ª', 'A');
        accentsMap.put('º', 'O');
        accentsMap.put('§', 'S');
        accentsMap.put('³', '3');
        accentsMap.put('²', '2');
        accentsMap.put('¹', '1');
        accentsMap.put('à', 'a');
        accentsMap.put('á', 'a');
        accentsMap.put('â', 'a');
        accentsMap.put('ã', 'a');
        accentsMap.put('ä', 'a');
        accentsMap.put('è', 'e');
        accentsMap.put('é', 'e');
        accentsMap.put('ê', 'e');
        accentsMap.put('ë', 'e');
        accentsMap.put('í', 'i');
        accentsMap.put('ì', 'i');
        accentsMap.put('î', 'i');
        accentsMap.put('ï', 'i');
        accentsMap.put('ù', 'u');
        accentsMap.put('ú', 'u');
        accentsMap.put('û', 'u');
        accentsMap.put('ü', 'u');
        accentsMap.put('ò', 'o');
        accentsMap.put('ó', 'o');
        accentsMap.put('ô', 'o');
        accentsMap.put('õ', 'o');
        accentsMap.put('ö', 'o');
        accentsMap.put('ñ', 'n');
        accentsMap.put('ç', 'c');
        return accentsMap;
    }

    public static Map<Character, Character> getForbiddenCharMap(){
        Map<Character, Character> accentsMap = new HashMap<>();
        accentsMap.put('"', ' ');
        accentsMap.put('\'', ' ');
        accentsMap.put('<', ' ');
        accentsMap.put('>', ' ');
        return accentsMap;
    }

    public static String getNormalizedText(String text) {
        StringBuilder sb = new StringBuilder(text);
        Map<Character, Character> accentsMap = getAccentsMap();

        for(int i = 0; i < text.length(); i++) {
            Character c = accentsMap.get(sb.charAt(i));
            if(c != null) {
                sb.setCharAt(i, c.charValue());
            }
        }
        return sb.toString();
    }

//    public static String getNormalizedText(String title) {
//        try {
//            String s = Normalizer.normalize(title, Normalizer.Form.NFD);
//            s = s.replaceAll("[^\\p{ASCII}]", "");
//            return s.replaceAll("\\p{M}", "");
//        }catch (Exception ignored){}
//        return title;
//    }

    public static String getPriceText(Number value) {
        return value + " DA";
    }

    public static String formatPrice(Float price) {
        String priceStr = String.format("%.0f", price);
        return formatPrice(priceStr);
    }

    public static String formatPrice(String price) {
        String formatedPrice = formatPrice(price, Locale.FRENCH, "DZD", "DA", "", 0);
        char separator = formatedPrice.length() > 3?formatedPrice.charAt(formatedPrice.length()-3): ' ';
        return formatedPrice.replace(separator, ' ' ) ;
    }

    public static String formatPrice(String price, Locale locale, String currencyCode, String displayCode, String spacer, Integer decimalDigits) {

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        if(decimalDigits != null) {
            currencyFormat.setMinimumFractionDigits(decimalDigits);
            currencyFormat.setMaximumFractionDigits(decimalDigits);
        }
        Currency currency = Currency.getInstance(currencyCode);
        currencyFormat.setCurrency(currency);

        try {
            String formatted = currencyFormat.format(NumberFormat.getNumberInstance().parse(price));
            currencyFormat.getCurrency().getSymbol(locale);
            return formatted.replace(currencyCode, displayCode);
        } catch (ParseException ignored) {
        }
        return "";
    }

    public static String getCurrency(){
        return "DA";
    }

    public static String getCurrency(Locale locale){
        Currency currency = Currency.getInstance(locale);
        return currency.getSymbol();
    }

    public static List<String> getArrayFromField(String field) {
        List<String> stringArray = new ArrayList<>();
        String text = "";
        for(int i = 0; i< field.length(); i++){
            char c = field.charAt(i);
            if(c == ' '){
                text = "";
            }else{
                text = text.concat(c + "");
                stringArray.add(text);
            }
        }
        return stringArray;
    }

    public static String getLinkFromString(String how_to_link) {
        List<String> links = new ArrayList<String>();
        Matcher m = Patterns.WEB_URL.matcher(how_to_link);
        while (m.find()) {
            String url = m.group();
            return url;
        }
        return null;
    }

    public static void preventDoubleClick(View view) {
        view.setEnabled(false);
        new Handler().postDelayed(() -> view.setEnabled(true), 300);
    }

    public static void animateProgress(final FrameLayout lyt_progress) {
        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewAnimation.fadeOut(lyt_progress);
            }
        }, 1500);
    }

    public static void showSimpleDialog(Context context, String title, String msg) {
        final androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(context);
        if(title != null){
            builder.setTitle(title);
        }
        if(msg != null){
            builder.setMessage(msg);
        }
        builder.setPositiveButton(context.getString(R.string.dialog_ok), null);
        builder.create().show();
    }

    private String getTimeDiff(String time, String currentTime,String dateFormat) throws ParseException
    {
        DateFormat formatter = new SimpleDateFormat(dateFormat);
        Date currentDate = (Date)formatter.parse(currentTime);
        Date oldDate = (Date)formatter.parse(time);
        long oldMillis = oldDate.getTime();
        long currentMillis = currentDate.getTime();
        return DateUtils.getRelativeTimeSpanString(oldMillis, currentMillis, 0).toString();
    }

    public interface DownloadListener{
        void onDownloadFinished(File file);
    }

    public static AlertDialog getProgressDialog(Context context){

        int llPadding = 30;
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(context);
        tvText.setText(R.string.loading_dialog_text);
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(ll);

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
        return dialog;
    }

    public static String[] addArgs(String[] args, String value) {
        String[] newArgs;
        if (args == null)
            newArgs = new String[] { value };
        else {
            newArgs = Arrays.copyOf(args, args.length + 1);
            newArgs[newArgs.length - 1] = value;
        }
        return newArgs;
    }

    public static String[] addArgs(String[] args, String[] args2) {
        for(String arg : args2){
            args = addArgs(args, arg);
        }
        return args;
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static File fileFrom(Context context, String fileName){
        return new File(context.getCacheDir(), fileName);
    }

    public static String getDateBeforeMins(int mins) {
        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.MINUTE, mins * -1);
        Date date = cal.getTime();
        SimpleDateFormat gmtFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.ENGLISH);
        gmtFormat.applyPattern(DEFAULT_DATE_TIME_FORMAT);
        TimeZone gmtTime = TimeZone.getDefault();
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(date);
    }

    public static String getDateBeforeHours(int hours) {
        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.HOUR, hours * -1);
        Date date = cal.getTime();
        SimpleDateFormat gmtFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.ENGLISH);
        gmtFormat.applyPattern(DEFAULT_DATE_TIME_FORMAT);
        TimeZone gmtTime = TimeZone.getDefault();
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(date);
    }

    public static void scheduleNotification(Context context, Notification notification, int delay) {

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    public static Notification getNotification(Activity activity, String title, String content) {
        Notification.Builder builder = new Notification.Builder(activity);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground);
        return builder.build();
    }

    public enum Language {english, french, arabic}

    public static Language getCurrentLanguage(Context context){
        String locale = new MySharedPreferences(context).getString(LauncherActivity.LANGUAGE_KEY, DEFAULT_LANGUAGE);
        if(locale.equals("fr")){
            return Language.french;
        }else if (locale.equals("ar")){
            return Language.arabic;
        }else if(locale.equals("not_provided")){
            return getSystemCurrentLanguage(context);
        }
        return Language.english;
    }

    public static String getCurrentLanguageLocale(Context context){
        String locale = new MySharedPreferences(context).getString(LauncherActivity.LANGUAGE_KEY, DEFAULT_LANGUAGE);
        if(locale.equals("not_provided")){
            return context.getResources().getConfiguration().locale.getLanguage();
        }
        return locale;
    }

    public static Language getSystemCurrentLanguage(Context context){
        String locale = context.getResources().getConfiguration().locale.getLanguage();
        if(locale.equals("fr")){
            return Language.french;
        }else if (locale.equals("ar")){
            return Language.arabic;
        }
        return Language.english;
    }

    public static String saveImage(Context mContext, Bitmap image, String fileName) {
        String savedImagePath = null;
        File storageDir = Environment.getExternalStoragePublicDirectory(MConstants.applicationImagesFolder);
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, fileName);
            try {
                imageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Add the image to the system gallery
            galleryAddPic(mContext, savedImagePath);
            Toast.makeText(mContext, mContext.getResources().getString(R.string.image_saved), Toast.LENGTH_LONG).show();
        }
        return savedImagePath;
    }

    private static void galleryAddPic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static String durationFromNow(Resources resources, String dateFrom, String currentDate) {
        long currentTimeMillis = currentDate == null? System.currentTimeMillis(): convertStringDateToMillis(currentDate);
        Date startDate = createDateObject(dateFrom, DEFAULT_DATE_TIME_FORMAT, false);
        long different = currentTimeMillis - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long weeksInMilli = daysInMilli * 7;
        long monthsInMilli = weeksInMilli * 4;
        long yearsInMilli = monthsInMilli * 12;

        long elapsedYears = different / yearsInMilli;
        different = different % yearsInMilli;

        long elapsedMonths = different / monthsInMilli;
        different = different % monthsInMilli;

        long elapsedWeeks = different / weeksInMilli;
        different = different % weeksInMilli;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        String output = "";
//        if (elapsedDays > 7) {
//            Date date = createDateObject(dateFrom, DEFAULT_SHARE_DATE_FORMAT, false);
//            output += getStringFromDate(date, DEFAULT_SHARE_DATE_FORMAT);
//            return output;
//        }
        if (elapsedYears > 1) {
            output += elapsedYears + " " + resources.getString(R.string.years);
            return output;
        }
        if (elapsedYears == 1) {
            output += elapsedYears + " " + resources.getString(R.string.year);
            return output;
        }
        if (elapsedMonths > 1) {
            output += elapsedMonths + " " + resources.getString(R.string.months);
            return output;
        }
        if (elapsedMonths == 1) {
            output += elapsedMonths + " " + resources.getString(R.string.month);
            return output;
        }
        if (elapsedWeeks > 1) {
            output += elapsedWeeks + " " + resources.getString(R.string.weeks);
            return output;
        }
        if (elapsedWeeks == 1) {
            output += elapsedWeeks + " " + resources.getString(R.string.week);
            return output;
        }
        if (elapsedDays == 1) {
            output += elapsedDays + " " + resources.getString(R.string.day);
            return output;
        }
        if (elapsedDays > 1) {
            output += elapsedDays + " " + resources.getString(R.string.days);
            return output;
        }
        if (elapsedHours == 1){
            output += elapsedHours + " " + resources.getString(R.string.hour);
            return output;
        }
        else if (elapsedHours > 1 ){
            output += elapsedHours + " " + resources.getString(R.string.hours);
            return output;
        }
        if (elapsedMinutes == 1){
            output += elapsedMinutes + " " + resources.getString(R.string.minute);
            return output;
        }
        if (elapsedMinutes > 1){
            output += elapsedMinutes + " " + resources.getString(R.string.minutes);
            return output;
        }
        if (elapsedSeconds > 0){
            output += elapsedSeconds + " " + resources.getString(R.string.seconds);
        }
        if(output.equals("")){
            output += resources.getString(R.string.now);
        }
        return output;
    }

    private static long convertStringDateToMillis(String currentDate) {
        return createDateObject(currentDate, DEFAULT_DATE_TIME_FORMAT, false).getTime();
    }

    public static Date getDateFromStringDate(String currentDate) {
        return createDateObject(currentDate, DEFAULT_DATE_TIME_FORMAT, false);
    }

    public static void toastIconInfo(Activity activity, String message) {
        toastIconInfo(activity, message, Toast.LENGTH_LONG);
    }

    public static Toast toastIconInfo(Activity activity, String message, int toastLength) {
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(toastLength);

        //inflate view
        View custom_view = activity.getLayoutInflater().inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(message);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_info);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(activity.getResources().getColor(R.color.blue_500));

        toast.setView(custom_view);
        toast.show();
        return toast;
    }

    public static void toastIconWarning(Activity activity, String message) {
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);

        //inflate view
        View custom_view = activity.getLayoutInflater().inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(message);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_info);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(activity.getResources().getColor(R.color.android_orange_dark));

        toast.setView(custom_view);
        toast.show();
    }

    public static void toastIconSuccess(Activity activity, String message) {
        Toast toast = new Toast(activity.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);

        //inflate view
        View custom_view = activity.getLayoutInflater().inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(message);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_check);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(activity.getResources().getColor(R.color.green_500));

        toast.setView(custom_view);
        toast.show();
    }


    public static Bundle getDataFromMap(Map<String, Object> bundleKeyValueMap) {
        Bundle data = new Bundle();
        if(bundleKeyValueMap != null) {
            for (String key : bundleKeyValueMap.keySet()) {
                Object value = bundleKeyValueMap.get(key);
                if(value instanceof Boolean){
                    data.putBoolean(key, (Boolean) bundleKeyValueMap.get(key));
                } else if (value instanceof Integer) {
                    data.putInt(key, (Integer) bundleKeyValueMap.get(key));
                } else if (value instanceof String) {
                    data.putString(key, (String) bundleKeyValueMap.get(key));
                } else if (value instanceof List) {
                    data.putStringArrayList(key, (ArrayList<String>) value);
                }
            }
        }
        return data;
    }

    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public enum ScreenSizeType{
        small, normal, large, xlarge
    }

    public static ScreenSizeType getScreenSizeType(Context context){
        int screenSize = context.getResources().getConfiguration().screenLayout;
        return (screenSize & Configuration.SCREENLAYOUT_SIZE_SMALL)==Configuration.SCREENLAYOUT_SIZE_SMALL? ScreenSizeType.small :
                (screenSize & Configuration.SCREENLAYOUT_SIZE_NORMAL)==Configuration.SCREENLAYOUT_SIZE_NORMAL? ScreenSizeType.normal :
                        (screenSize & Configuration.SCREENLAYOUT_SIZE_LARGE)==Configuration.SCREENLAYOUT_SIZE_LARGE? ScreenSizeType.large :
                                (screenSize & Configuration.SCREENLAYOUT_SIZE_XLARGE)==Configuration.SCREENLAYOUT_SIZE_XLARGE? ScreenSizeType.xlarge : null;
    }

    public static String formatTime(Date time, Locale locale){
        String timeFormat = DEFAULT_DATE_TIME_FORMAT;

        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat(timeFormat, locale);

//        try {
//        } catch(Exception e) {
////            formatter = new SimpleDateFormat(DEFAULT_TIME_FORMAT, locale);
//        }
        return formatter.format(time);
    }

    public static int getPhoneCode(Context context, String countryCode){
        return PhoneNumberUtil.createInstance(context).getCountryCodeForRegion(countryCode.toUpperCase());
    }

    public static String getCountryCodeFromSettings(Context context){
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkCountryIso();
    }

    public static int generateRandomColor() {
        Random rnd = new Random();
        int randomValue;
        do{
            randomValue = rnd.nextInt();
        }while (randomValue % 255 < 120);
        return randomValue % 255;
    }

    public static int generateRandomLightColor() {
        Random rnd = new Random();
        int randomValue;
        do{
            randomValue = rnd.nextInt();
        }while (randomValue % 240 < 120);
        return randomValue % 240;
    }

    public static String getAllowedText(String text) {
        StringBuilder sb = new StringBuilder(text);
        Map<Character, Character> forbiddenCharMap = getForbiddenCharMap();

        for(int i = 0; i < text.length(); i++) {
            Character c = forbiddenCharMap.get(sb.charAt(i));
            if(c != null) {
                sb.setCharAt(i, c.charValue());
            }
        }
        return sb.toString();
    }

    public static int countCommonChars(String phrase, String word) {
        int count = 0;
        int maxCount = 0;
        for (int i = 0; i < phrase.length(); i++) {
            char c = phrase.charAt(i);
            if (word.indexOf(c) != -1) {
                count++;
            }else if(count != 0){
                maxCount = Math.max(count, maxCount);
                count = 0;
            }
        }

        return maxCount;
    }

    public static SweetAlertDialog showLogInRequiredDialog(Activity activity, int activityKey){
        return showLogInRequiredDialog(activity, activityKey, true);
    }

    public static SweetAlertDialog showLogInRequiredDialog(Activity activity, int activityKey, boolean show){
        String title = activity.getString(R.string.log_in);
        String message = activity.getString(R.string.login_required_msg);
        String positiveButtonTitle = activity.getString(R.string.log_in);
        SweetAlertDialog dialog = new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText(title);
        dialog.setContentText(message);
        dialog.setConfirmText(positiveButtonTitle);
        dialog.setConfirmClickListener(sweetAlertDialog -> {
            Bundle data = new Bundle();
            data.putBoolean(LauncherActivity.FIRST_RUN_KEY, false);
            Intent intent = new Intent(activity, LauncherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtras(data);
            activity.startActivityForResult(intent, activityKey, intent.getExtras());
            activity.finish();
            sweetAlertDialog.dismiss();
        });
        dialog.setCancelButton(activity.getString(R.string.cancel), null);
        dialog.setCancelClickListener(null);
        dialog.setCancelable(false);
        if(show) {
            dialog.show();
        }
        return dialog;
    }

    public String getLocation(double lat, double lng){
        return GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lng));
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void performImeOption(EditText editText){
        BaseInputConnection inputConnection = new BaseInputConnection(editText, true);
        inputConnection.performEditorAction(EditorInfo.IME_ACTION_UNSPECIFIED);
    }

    public static String getGeoHash(double latitude, double longitude) {
        return GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));
    }

    public static double distance(double latitude1, double longitude1, double latitude2, double longitude2) {
        final int RADIUS_EARTH = 6371;

        double dLat = getRad(latitude2 - latitude1);
        double dLong = getRad(longitude2 - longitude1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(getRad(latitude1)) * Math.cos(getRad(latitude2)) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (RADIUS_EARTH * c) * 1000;
    }

    private static Double getRad(Double x) {
        return x * Math.PI / 180;
    }

    public static long getTimeInMillis(String date){
        return createDateObject(date, DEFAULT_DATE_TIME_FORMAT, false)
                .getTime();
    }

    public static float calculateDuration(String endDate, String startDate){
        float duration = 0;
        try {
            duration = (getTimeInMillis(endDate) -
                    getTimeInMillis(startDate));
        }catch (Exception ignored){}
        return duration;
    }


}
