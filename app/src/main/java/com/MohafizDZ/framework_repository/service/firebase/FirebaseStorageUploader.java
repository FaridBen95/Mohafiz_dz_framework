package com.MohafizDZ.framework_repository.service.firebase;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.MohafizDZ.framework_repository.core.Col;
import com.MohafizDZ.framework_repository.datas.MConstants;
import com.MohafizDZ.framework_repository.service.FirebaseStorageSingleton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;

public class FirebaseStorageUploader {

    private static final String TAG = "FirebaseStorageUploader";

    // Reference to Firebase Storage
    private final FirebaseStorage storage;
    private final StorageListener storageListener;
    private int uploading;
    private Exception hasFailure;

    public FirebaseStorageUploader(Context context, StorageListener storageListener) {
        // Initialize FirebaseStorage instance
        storage = FirebaseStorageSingleton.get();
        this.storageListener = storageListener;
    }

    public void uploadFiles(String userId, List<Uri> uris) {
        // Get a reference to the root directory in Firebase Storage
        StorageReference storageRef = storage.getReference(MConstants.FIREBASE_STORAGE_LINK);
        final StorageReference userReference = storageRef.child(userId);
        hasFailure = null;
        uploading = 0;

        for (Uri uri : uris) {
            // Create a reference to the desired location in Firebase Storage
            // Here, we are using the URI's last path segment as the file name
            String fileName = uri.getPath();
            if (fileName != null) {
                StorageReference fileRef = userReference.child(fileName);

                // Upload the file to Firebase Storage
                fileRef.putFile(uri)
                        .addOnSuccessListener(taskSnapshot -> {
                            if(++uploading >= uris.size()) {
                                if(hasFailure == null) {
                                    storageListener.onSuccess(taskSnapshot);
                                }else{
                                    storageListener.onFailure(hasFailure);
                                }
                            }
                            // File successfully uploaded
                            Log.d(TAG, "File uploaded: " + fileName);
                        })
                        .addOnFailureListener(e -> {
                            hasFailure = e;
                            // Handle unsuccessful uploads
                            Log.e(TAG, "Failed to upload file: " + fileName, e);
                        });
            }
        }
    }

    public interface StorageListener{
        void onSuccess(UploadTask.TaskSnapshot taskSnapshot);
        void onFailure(Exception e);
    }
}
