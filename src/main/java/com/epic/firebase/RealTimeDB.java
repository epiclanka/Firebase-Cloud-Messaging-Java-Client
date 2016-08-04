/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.epic.firebase;

import com.google.api.client.json.JsonObjectParser;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.utilities.Utilities;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.*;
import org.json.*;

/**
 *
 * @author thilina_h
 */
public class RealTimeDB {

    //private final FirebaseDatabase getDB();
    private DataSnapshot snapshot;
    private final Lock lock = new ReentrantLock();
    private final Condition isDone = lock.newCondition();
    private final CustomValueEventListner rootValueEventListner;
    private final DatabaseReference.CompletionListener LISTNER = (DatabaseError databaseError, DatabaseReference databaseReference) -> {
        if (databaseError != null) {
            System.out.println("Data could not be saved " + databaseError.getMessage());
        } else {
            System.out.println("Data saved successfully.");
        }

    };

    public RealTimeDB(InputStream config_json_stream, String db_name) {

        System.out.println("-----------initializing RealtimeDB-----------");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(config_json_stream)
                .setDatabaseUrl("https://" + db_name + ".firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(options);
        rootValueEventListner = new CustomValueEventListner();
        FirebaseDatabase.getInstance().getReference().addValueEventListener(rootValueEventListner);
        try {
            snapshot = rootValueEventListner.getSnapshot(60, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            System.out.println("Timeout exceeded");
            Logger.getLogger(RealTimeDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("initialized");
    }

    public DatabaseReference getDBRef(String path) {

        return getDB().getReference(path);
    }

    public FirebaseDatabase getDB() {
        return FirebaseDatabase.getInstance();
    }

    public void insert_object(String node, String child_id, Object data) {
        getDB().getReference(node).child(child_id).setValue(data, LISTNER);
    }

    public void insert(String node, String child_id, boolean data) {
        insert_object(node, child_id, data);
    }

    public void insert(String node, String child_id, long data) {
        insert_object(node, child_id, data);
    }

    public void insert(String node, String child_id, double data) {
        insert_object(node, child_id, data);
    }

    public void insert(String node, String child_id, List<Object> data) {
        insert_object(node, child_id, data);
    }

    public void insert(String node, Map<String, Object> data) {
        for (Map.Entry<String, Object> pair : data.entrySet()) {
            insert_object(node, pair.getKey(), pair.getValue());
        }
        System.out.println(data.size() + " records inserted");
    }

    public List<String> getChildKeys(String node) {
        List<String> key_list = new ArrayList<>();
        if (getSnapshot().child(node).exists()) {
            for (DataSnapshot child : getSnapshot().child(node).getChildren()) {
                if (child.exists()) {
                    key_list.add(child.getKey());
                }
            }
        }
        return key_list;
    }

    public DataSnapshot getSnapshot() {
        if(snapshot==null){
            try {
                snapshot=rootValueEventListner.getSnapshot(60, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                throw new NullPointerException("Failed to load DataSnapshot");
            }
        }
        return snapshot;
    }
    

//    /**
//     * This supports multi-path update
//     *
//     * @param node
//     * @param updates <br/>[keys should be multi paths.
//     * <br/>Ex:-updates.put("alanisawesome/nickname", "Alan The Machine");]
//     */
//    public void update(String node, String child_id, Map<String, Object> updates) {
//        getDB().getReference(node).child(child_id).updateChildren(updates);
//    }
//
//    public void update(String node, Map<String, Object> updates) {
//        getDB().getReference(node).updateChildren(updates);
//    }
    public void replace(String node, Map<String, Object> updates) {
        updates.entrySet().stream().forEach((pair) -> {
            insert_object(node, pair.getKey(), pair.getValue());
        });

    }

    public void delete(String node, String child_id) {
        getDB().getReference(node).child(child_id).removeValue(LISTNER);
    }

    private static class CustomValueEventListner implements ValueEventListener {

        private final Lock lock = new ReentrantLock();
        private final Condition isDone = lock.newCondition();
        private DataSnapshot snapshot;

        @Override
        public void onDataChange(DataSnapshot snap) {
            System.out.println("OnDataChange");
            try {
                boolean locked = lock.tryLock(3, TimeUnit.SECONDS);
                if (locked) {
                    snapshot = snap;
                    isDone.signal();
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(RealTimeDB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    lock.unlock();
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError arg0) {
            System.out.println("Error in DBChange registration");

        }

        public DataSnapshot getSnapshot() throws TimeoutException {
            return getSnapshot(0, TimeUnit.NANOSECONDS);
        }

        public DataSnapshot getSnapshot(long time, TimeUnit units) throws TimeoutException {
  
            lock.lock();
            try {
                if (snapshot == null) {
                    try {
                        System.out.println("waiting for snapshot");
                        isDone.await(time, units);//releases the lock
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RealTimeDB.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } finally {
                lock.unlock();
            }
            if (snapshot==null) {
                throw new TimeoutException();
            }
            return snapshot;
        }

//        public Lock getLock() {
//            return lock;
//        }
//
//        public Condition getIsDone() {
//            return isDone;
//        }
    }
}
