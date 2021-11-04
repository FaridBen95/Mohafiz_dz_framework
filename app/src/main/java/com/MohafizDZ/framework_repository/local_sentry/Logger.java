package com.MohafizDZ.framework_repository.local_sentry;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.MohafizDZ.empty_project.BuildConfig;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.framework_repository.Utils.MyUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

public class Logger {
    public static final String TAG = Logger.class.getSimpleName();
    private static long delay = 10000;
    private static long lastInsertInMilis = 0;
    private static boolean firstLaunch = true;
    private static Context mContext;
    private static String text;
    private static String fileName;
    private static Stack<String> textStack;
    private static int depth = 10;
    private static int currentDepth = 0;

    public Logger(Context mContext){
        Logger.mContext = mContext;
        if(fileName == null || fileName.equals("")){
            fileName = MConstants.LOG_FILE_NAME;
        }
        if(textStack == null){
            textStack = new Stack<>();
        }
    }

    private void startUsingLog(){
        synchronized (this){
            if(allowed()){
                firstLaunch = false;
                text = getLogText();
                createOrWriteToLog(mContext, fileName, text);
            }
        }
    }

    private boolean allowed(){
        return firstLaunch || System.currentTimeMillis() >= lastInsertInMilis + delay;
    }

    private void createOrWriteToLog(Context mContext,String fileName, String text) {
        try {
            String apkName = BuildConfig.APPLICATION_ID;
            apkName = apkName.substring(apkName.lastIndexOf(".")+1, apkName.length()-1);
            File root = new File(Environment.getExternalStorageDirectory(), apkName);
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, fileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(text);
            writer.flush();
            writer.close();
            Toast.makeText(mContext, "Saved", Toast.LENGTH_SHORT).show();
            lastInsertInMilis = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertToTextStack(String text){
        if(currentDepth > depth) {
            currentDepth = 0;
            String emptyLines = "----------------------------------" + depth +" deep of the log. \n \n \n \n \n";
            textStack.push(emptyLines);
        }
        textStack.push(text);
    }

    private String getLogText(){
        if(textStack.empty()){
            return "";
        }else{
            return textStack.pop() + getLogText();
        }
    }

    public void startWriting(String text){
        String currentDate = MyUtil.getCurrentDate();
        String textInput = currentDate + "\n";
        textInput += text;
        insertToTextStack(textInput);
        startUsingLog();
    }
}
