/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 16/1/15 3:36 PM
 */
package com.MohafizDZ.framework_repository.Utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.MohafizDZ.App;
import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.framework_repository.core.DevicePermissionHelper;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.core.Values;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class FileManager implements DialogInterface.OnClickListener {
    public static final String TAG = FileManager.class.getSimpleName();
    public static final int REQUEST_CAMERA = 111;
    public static final int REQUEST_HIGH_CAMERA = 112;
    public static final int REQUEST_IMAGE = 113;
    public static final int REQUEST_AUDIO = 114;
    public static final int REQUEST_FILE = 115;
    public static final int REQUEST_ALL_FILE = 116;
    private static final int SINGLE_ATTACHMENT_STREAM = 117;
    private static final long IMAGE_MAX_SIZE = 1000000; // 1 MB
    private final MyAppCompatActivity mActivity;
    private String[] mOptions = null;
    private RequestType requestType = null;
    private Uri newImageUri = null;
    private App mApp;
    private DevicePermissionHelper devicePermissionHelper;

    public enum RequestType {
        CAPTURE_HIGH_IMAGE,
        CAPTURE_IMAGE,
        IMAGE,
        IMAGE_OR_CAPTURE_IMAGE,
        IMAGE_OR_CAPTURE_HIGH_IMAGE,
        AUDIO,
        FILE,
        ALL_FILE_TYPE
    }

    public FileManager(MyAppCompatActivity activity) {
        mActivity = activity;
        mApp = (App) mActivity.getApplicationContext();
        devicePermissionHelper = new DevicePermissionHelper(activity);
    }

    private String createFile(String name, byte[] fileAsBytes, String file_type) {
        InputStream is = new ByteArrayInputStream(fileAsBytes);
        String filename = name.replaceAll("[-+^:=, ]", "_");
        String file_path = new File(mActivity.getApplicationContext().getCacheDir(), MyUtil.uniqid("img", true)+ ".jpg").getName();
        try {
            FileOutputStream fos = new FileOutputStream(file_path);
            byte data[] = new byte[1024];
            int count = 0;
            while ((count = is.read(data)) != -1) {
                fos.write(data, 0, count);
            }
            is.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file_path;
    }

    private void requestIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        FileNameMap mime = URLConnection.getFileNameMap();
        String mimeType = mime.getContentTypeFor(uri.getPath());
        intent.setDataAndType(uri, mimeType);
        try {
            mActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            makeText(mActivity, mActivity.getResources().getString(R.string.no_activity_found_to_handle_this_file),
                    Toast.LENGTH_LONG).show();
        }
    }


    private boolean fileExists(Uri uri) {
        return new File(uri.getPath()).exists();
    }

    public Bitmap getBitmapFromURI(Uri uri) {
        Bitmap bitmap;
        if (!fileExists(uri) && atLeastKitKat()) {
            String path = getDocPath(uri);
            bitmap = BitmapUtils.getBitmapImage(mActivity,
                    BitmapUtils.uriToBase64(Uri.fromFile(new File(path)), mActivity.getContentResolver()));
        } else {
            bitmap = BitmapUtils.getBitmapImage(mActivity,
                    BitmapUtils.uriToBase64(uri, mActivity.getContentResolver()));
        }
        return bitmap;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getDocPath(Uri uri) {
        String wholeID = DocumentsContract.getDocumentId(uri);
        String id = wholeID.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};
        String sel = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = mActivity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
                new String[]{id}, null);
        String filePath = null;
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public boolean atLeastKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public void requestForFile(final RequestType type) {
        if (devicePermissionHelper.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && devicePermissionHelper.hasPermission(Manifest.permission.CAMERA)) {
            _requestForFile(type);
        } else {
            Log.w(TAG, "No permission for CAMERA or WRITE_EXTERNAL_STORAGE");
            devicePermissionHelper.requestPermissions(new DevicePermissionHelper
                    .PermissionGrantListener() {
                @Override
                public void onPermissionGranted() {
                    _requestForFile(type);
                }

                @Override
                public void onPermissionDenied() {
                    makeText(mActivity, R.string.toast_permission_download_storage,
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionRationale() {
                }
            }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
        }
    }

    public void _requestForFile(RequestType type) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        switch (type) {
            case AUDIO:
                intent.setType("audio/*");
                requestIntent(intent, REQUEST_AUDIO);
                break;
            case IMAGE:
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                requestIntent(intent, REQUEST_IMAGE);
                break;
            case CAPTURE_IMAGE:
            case CAPTURE_HIGH_IMAGE:
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "MohafizDZ attachments");
                values.put(MediaStore.Images.Media.DESCRIPTION,
                        "Captured from MohafizDz Mobile App");
                newImageUri = mActivity.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);
                requestIntent(intent, type == RequestType.CAPTURE_IMAGE ?
                        REQUEST_CAMERA : REQUEST_HIGH_CAMERA);
                break;
            case IMAGE_OR_CAPTURE_IMAGE:
            case IMAGE_OR_CAPTURE_HIGH_IMAGE:
                requestDialog(type);
                break;
            case FILE:
                intent.setType("application/*");
                requestIntent(intent, REQUEST_FILE);
                break;
            case ALL_FILE_TYPE:
                intent.setType("*/*");
                requestIntent(intent, REQUEST_ALL_FILE);
                break;
        }
    }

    public Values getURIDetails(Uri uri) {
        Values values = new Values();
        ContentResolver mCR = mActivity.getContentResolver();
        if (uri.getScheme().equals("content")) {
            Cursor cr = mCR.query(uri, null, null, null, null);
            int nameIndex = cr.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int fileSize = cr.getColumnIndex(OpenableColumns.SIZE);
            if (cr.moveToFirst()) {
                values.put("name", cr.getString(nameIndex));
                values.put("datas_fname", values.get("name"));
                values.put("file_size", Long.toString(cr.getLong(fileSize)));
                String path = getPath(uri);
                if (path != null) {
                    values.put("file_size", new File(path).length() + "");
                }
            }
            cr.close();
        }
        if (uri.getScheme().equals("file")) {
            File file = new File(uri.toString());
            values.put("name", file.getName());
            values.put("datas_fname", values.get("name"));
            values.put("file_size", Long.toString(file.length()));
        }
        values.put("file_uri", uri.toString());
        values.put("scheme", uri.getScheme());
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getMimeTypeFromExtension(mime
                .getExtensionFromMimeType(mCR.getType(uri)));
        values.put("file_type", (type == null) ? uri.getScheme() : type);
        values.put("type", type);
        return values;
    }

    public String getPath(Uri uri) {
        ContentResolver mCR = mActivity.getContentResolver();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = mCR.query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public Values handleResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                case REQUEST_HIGH_CAMERA:
                    Values values = getURIDetails(newImageUri);
                    values.put("datas", BitmapUtils.uriToBase64(newImageUri,
                            mActivity.getContentResolver(), requestCode == REQUEST_CAMERA));
                    return values;
                case REQUEST_IMAGE:
                    values = getURIDetails(data.getData());
                    values.put("datas", BitmapUtils.uriToBase64(data.getData(),
                            mActivity.getContentResolver(), true));
                    return values;
                case REQUEST_ALL_FILE:
                default:
                    return getURIDetails(data.getData());
            }
        }
        return null;
    }

    private void requestIntent(Intent intent, int requestCode) {
        try {
            mActivity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            makeText(mActivity, "No Activity Found to handle request",
                    LENGTH_SHORT).show();
        }
    }

    private void requestDialog(RequestType type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        switch (type) {
            case IMAGE_OR_CAPTURE_IMAGE:
            case IMAGE_OR_CAPTURE_HIGH_IMAGE:
                requestType = type;
                mOptions = new String[]{"Select Image", "Capture Image"};
                break;
        }
        builder.setSingleChoiceItems(mOptions, -1, this);
        builder.create().show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (requestType) {
            case IMAGE_OR_CAPTURE_IMAGE:
            case IMAGE_OR_CAPTURE_HIGH_IMAGE:
                RequestType captureType = requestType == RequestType.IMAGE_OR_CAPTURE_IMAGE
                        ? RequestType.CAPTURE_IMAGE : RequestType.CAPTURE_HIGH_IMAGE;
                requestForFile((which == 0) ? RequestType.IMAGE : captureType);
                break;
        }
        dialog.dismiss();
    }

}
