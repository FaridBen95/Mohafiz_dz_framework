package com.MohafizDZ.framework_repository.service;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirestoreSingleton {

    private static FirebaseFirestore firestoreSingleton;

    private FirestoreSingleton(){

    }

    public static FirebaseFirestore get(){
        Log.d("firebase_state", String.valueOf(firestoreSingleton == null));
        if(firestoreSingleton == null){
            try {
                firestoreSingleton = FirebaseFirestore.getInstance();
                FirebaseFirestore.setLoggingEnabled(false);
                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(false)
                        .build();
                firestoreSingleton.setFirestoreSettings(settings);
            }catch (Exception ignored){}
//            firestoreSingleton.clearPersistence();
        }
        return firestoreSingleton;
    }
}
