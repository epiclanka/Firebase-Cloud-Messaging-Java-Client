package com.epic.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by thilina_h on 11/10/2017.
 */
public class CloudStorage {
    private Bucket bucket;

    public CloudStorage(FirebaseApp firebaseApp){

        bucket = StorageClient.getInstance(firebaseApp).bucket();
        Blob blob = bucket.create("test/", new byte[]{});



// 'bucket' is an object defined in the google-cloud-storage Java library.
// See http://googlecloudplatform.github.io/google-cloud-java/latest/apidocs/com/google/cloud/storage/Bucket.html
// for more details.
    }

    public void insert(){

    }

//    public static void main(String[] args) {
//        BiMap<String, Integer> userId = HashBiMap.create();
//        userId.put("mama",1);
//        userId.put("dada",2);
//        userId.put("lola",1);
//        String s = userId.inverse().get(1);
//        System.out.println(s);
//    }
}
