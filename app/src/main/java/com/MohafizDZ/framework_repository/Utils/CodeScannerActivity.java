package com.MohafizDZ.framework_repository.Utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.MohafizDZ.own_distributor.R;
import com.MohafizDZ.framework_repository.core.MyAppCompatActivity;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;

import gun0912.tedimagepicker.builder.TedImagePicker;

public class CodeScannerActivity extends MyAppCompatActivity implements View.OnClickListener {
    private static final String TAG = CodeScannerActivity.class.getSimpleName();
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 4056;
    private CodeScanner mCodeScanner;
    private ScanListener scanListener = null;
    private FloatingActionButton scanFromImageFab;
    private boolean tedImagePickerIsShown;
    private TedImagePicker.Builder tedImagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);
        initConfig();
        scanListener = setScanListener();
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        scanFromImageFab = findViewById(R.id.scanFromImageFab);
        scanFromImageFab.setOnClickListener(this);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(result -> {
            if(scanListener == null) {
                Intent data = new Intent();
                data.putExtra("result", result.getText());
                setResult(RESULT_OK, data);
                CodeScannerActivity.this.finish();
            }else{
                Log.d(TAG, result.getText());
                scanListener.onScanCompleted(result.getText());
            }
        });
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());
    }

    private void initConfig() {
        tedImagePicker = TedImagePicker.with(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission is not granted, request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission is granted, perform necessary operations
                // ...
                mCodeScanner.startPreview();
            } else {
                finish();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    public void setTitleBar(ActionBar actionBar) {

    }

    @Override
    public Toolbar setToolBar() {
        return null;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.scanFromImageFab){
            loadImage();
        }
    }

    private void loadImage() {
        if(!tedImagePickerIsShown) {//todo remove the next line
            app().createApplicationFolder();
            tedImagePicker.savedDirectoryName(MConstants.applicationImagesFolder).
                    start(uri -> {
                        String decodedText = decodeQRCodeFromUri(CodeScannerActivity.this, uri);
                        if (decodedText != null) {
                            if(scanListener == null) {
                                Intent data = new Intent();
                                data.putExtra("result", decodedText);
                                setResult(RESULT_OK, data);
                                CodeScannerActivity.this.finish();
                            }else{
                                scanListener.onScanCompleted(decodedText);
                            }
                            // QR code successfully decoded, use the decoded text
                            Log.d(TAG, "Decoded QR code: " + decodedText);
                        } else {
                            Toast.makeText(this, getString(R.string.scan_qr_code_failed_msg), Toast.LENGTH_SHORT).show();
                            // Failed to decode the QR code from the image
                            Log.d(TAG, "Failed to decode QR code from image");
                        }
                    });
        }
        tedImagePickerIsShown = !tedImagePickerIsShown;
    }

    public String decodeQRCodeFromUri(Context context, Uri imageUri) {
        try {
            // Step 1: Retrieve InputStream from the URI
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            // Step 2: Create BitmapFactory.Options and decodeStream to get image dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Step 3: Calculate sample size
            int targetSize = 1024; // specify your desired target size
            int width = options.outWidth;
            int height = options.outHeight;

            int sampleSize = 1;
            if (width > targetSize || height > targetSize) {
                final int halfWidth = width / 2;
                final int halfHeight = height / 2;
                while ((halfWidth / sampleSize) >= targetSize && (halfHeight / sampleSize) >= targetSize) {
                    sampleSize *= 2;
                }
            }

            // Step 4: Reset InputStream and update BitmapFactory.Options with sample size
            inputStream = contentResolver.openInputStream(imageUri);
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            // Step 5: Decode the input stream into a Bitmap with the desired sample size
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Step 6: Decode the QR code from the Bitmap
            return decodeQRCode(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error decoding QR code from URI", e);
            return null;
        }
    }

    public String decodeQRCode(Bitmap bitmap) {
        try {
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            MultiFormatReader reader = new MultiFormatReader();
            reader.setHints(hints);

            Result result = reader.decode(binaryBitmap);
            return result.getText();
        } catch (Exception e) {
            Log.e(TAG, "Error decoding QR code", e);
            return null;
        }
    }

    public interface ScanListener{
        void onScanCompleted(String result);
    }

    public ScanListener setScanListener(){
        return null;
    }
}