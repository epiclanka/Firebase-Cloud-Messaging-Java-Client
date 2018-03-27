package com.epic.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.internal.FirebaseThreadManagers;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by thilina_h on 12/14/2017.
 */
public class FirebaseAppInitializer {

    private static FirebaseApp firebaseApp;

    public FirebaseAppInitializer(InputStream config_json_stream, String db_name) throws IOException {
        synchronized (this) {
            if (firebaseApp == null) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(config_json_stream))
                        .setStorageBucket(db_name + ".appspot.com")
                        .setDatabaseUrl("https://" + db_name + ".firebaseio.com/")
                        .build();
                firebaseApp = FirebaseApp.initializeApp(options);
            }
        }
    }

    public FirebaseApp getConfiguredFirebaseApp() {
        return firebaseApp;
    }



}
