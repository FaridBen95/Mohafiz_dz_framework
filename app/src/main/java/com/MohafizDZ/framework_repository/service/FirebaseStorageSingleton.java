package com.MohafizDZ.framework_repository.service;

import com.google.firebase.storage.FirebaseStorage;

public class FirebaseStorageSingleton {

    private static FirebaseStorage firestoreSingleton;

    private FirebaseStorageSingleton(){

    }

    public static FirebaseStorage get(){
        if(firestoreSingleton == null){
            firestoreSingleton = FirebaseStorage.getInstance();
        }
        return firestoreSingleton;
    }
}
