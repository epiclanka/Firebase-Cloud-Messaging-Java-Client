/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nwised.firebase;

import com.google.common.base.Stopwatch;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import com.google.firebase.database.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author thilina_h
 */
public class RealTimeDB {

    //private final FirebaseDatabase getDB();
    private final CustomValueEventListner rootValueEventListner;
    private final DatabaseReference.CompletionListener LISTNER = (DatabaseError databaseError, DatabaseReference databaseReference) -> {
        if (databaseError != null) {
            System.out.println("Data could not be saved " + databaseError.getMessage());
        } else {
//            System.out.println("Data saved successfully.");

        }

    };

    Logger logger = Logger.getLogger(RealTimeDB.class.getName());
    private FirebaseDatabase firebaseDatabase;

    private DatabaseReference currentReference;

    public RealTimeDB(FirebaseApp firebaseApp) throws IOException {

        System.out.println("-----------initializing RealtimeDB-----------");

        rootValueEventListner = new CustomValueEventListner();
        firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
//        firebaseDatabase.setPersistenceEnabled(true);
        firebaseDatabase.getReference().addValueEventListener(rootValueEventListner);

        currentReference = firebaseDatabase.getReference().getRef();

        System.out.println("RealtimeDB initialized");
    }

    public DatabaseReference getDBRef(@NotNull String path) {

        return getDB().getReference(path);
    }

    public void setCurrentReference(@NotNull DatabaseReference ref) {
        currentReference = ref;
    }

    public DatabaseReference getCurrentReference() {
        return currentReference;
    }

    public FirebaseDatabase getDB() {
        return firebaseDatabase;
    }

    public void insertObject(String node, String child_id, Object data) {
        getDB().getReference(node).child(child_id).setValue(data, LISTNER);

    }

    public void updateObject(String node, String child_id, Map<String, Object> data) {
//        ApiFuture<Void> voidApiFuture = getDB().getReference(node).child(child_id).removeValueAsync();
//        try {
//            voidApiFuture.get();
//            getDB().getReference(node).child(child_id).setValue(data, LISTNER);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        getDB().getReference(node).child(child_id).updateChildren(data, LISTNER);

    }

    private void insertObject(String node, String child_id, Object data, CustomCompletionListner completionListner) {
        //CustomCompletionListner completionListner=new CustomCompletionListner();
        getDB().getReference(node).child(child_id).setValue(data, completionListner);
        //completionListner.waitTillComplete();
    }

    public void insert(String node, String child_id, boolean data) {
        insertObject(node, child_id, data);
    }

    public void insert(String node, String child_id, long data) {
        insertObject(node, child_id, data);
    }

    public void insert(String node, String child_id, double data) {
        insertObject(node, child_id, data);
    }

    public void insert(String node, String child_id, List<Object> data) {
        insertObject(node, child_id, data);
    }

    public void insert(String node, Map<String, Object> data) {
        data.entrySet().stream().forEach((pair) -> {
            insertObject(node, pair.getKey(), pair.getValue());
        });
        System.out.println(data.size() + " records inserted");
    }

    public void insert_blocking(String node, Map<String, Object> data) {
        List<CustomCompletionListner> listner_list = new ArrayList<>();

        data.entrySet().stream().forEach((pair) -> {
            CustomCompletionListner completionListner = new CustomCompletionListner();
            listner_list.add(completionListner);
            insertObject(node, pair.getKey(), pair.getValue(), completionListner);
        });
        listner_list.stream().forEach((listner) -> {// wait till all the data get saved
            listner.waitTillComplete(20, TimeUnit.SECONDS);
        });
        System.out.println(data.size() + " records inserted");
    }

    public Set<String> getChildKeys(String node, int timeout) {
        Set<String> key_list = new HashSet<>();
        try {
            if (getSnapshot(timeout).child(node).exists()) {
                for (DataSnapshot child : getSnapshot(timeout).child(node).getChildren()) {
                    if (child.exists()) {
                        key_list.add(child.getKey());
                    }
                }
            }
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        return key_list;
    }

    public void deleteByValue(String ref_path, String table, String field, String value) {
        DatabaseReference ref = getDBRef(ref_path);

        Query applesQuery = ref.child(table).orderByChild(field).equalTo(value);

        applesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child_snapshot : dataSnapshot.getChildren()) {
                    System.out.println("deleting " + child_snapshot.getKey());
                    child_snapshot.getRef().removeValue(LISTNER);
//                    <Void> voidApiFuture = child_snapshot.getRef().removeValueAsync();
//                    try {
//                        voidApiFuture.get();
//                        System.out.println(table+" "+field+""+value+" deleted");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                logger.log(Level.WARNING, " deleteByValue() onCancelled", databaseError);
            }
        });
    }

    /**
     * throws a null pointer exception
     *
     * @return
     */
    public DataSnapshot getSnapshot(int timeout) {

        try {
            return rootValueEventListner.getSnapshot(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            throw new NullPointerException("Failed to load DataSnapshot");
        }
    }

    public void onShutDown() {
        Logger.getLogger(RealTimeDB.class.getSimpleName()).log(Level.INFO, "Unregistering ValueEventListners");
        firebaseDatabase.getReference().removeEventListener(rootValueEventListner);
        Logger.getLogger(RealTimeDB.class.getSimpleName()).log(Level.INFO, "ValueEventListners unregistered");
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
            insertObject(node, pair.getKey(), pair.getValue());
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

            try {
                boolean locked = lock.tryLock(3, TimeUnit.SECONDS);
                if (locked) {
                    snapshot = snap;
                    System.out.println("Snapshot Fetched at :" + LocalDateTime.now());
                    isDone.signalAll();
                } else {
                    Logger.getLogger(RealTimeDB.CustomValueEventListner.class.getName()).log(Level.WARNING, "could not acquire lock");
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(RealTimeDB.CustomValueEventListner.class.getName()).log(Level.SEVERE, null, ex);
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
            System.out.println("Error in Fetching snapshot " + arg0.getMessage());
            arg0.toException().printStackTrace();

        }

        public DataSnapshot getSnapshot() throws TimeoutException {
            return getSnapshot(0, TimeUnit.NANOSECONDS);
        }

        /**
         * @param time  timeout value. set to zero to wait for ever
         * @param units
         * @return
         * @throws TimeoutException
         */
        public DataSnapshot getSnapshot(long time, TimeUnit units) throws TimeoutException {

            lock.lock();
            try {
                if (snapshot == null) {
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    System.out.println("waiting for snapshot");
                    try {
                        if (time > 0) {
                            isDone.await(time, units);
                        } else {
                            isDone.await();
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(RealTimeDB.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
                    System.out.println();
                    System.out.println(elapsed + " seconds elapsed for fetching the snapshot");
                }
            } finally {
                lock.unlock();
            }
            if (snapshot == null) {
                throw new TimeoutException("loading snopshot failed : timeout");
            }
            return snapshot;
        }

    }

    private static class CustomCompletionListner implements DatabaseReference.CompletionListener {

        private final Lock lock = new ReentrantLock();
        private final Condition completed = lock.newCondition();
        private boolean isCompleted = false;

        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference arg1) {
            if (databaseError != null) {
                System.out.println("Data could not be saved " + databaseError.getMessage());
            } else {
                System.out.println("Data saved successfully.");
            }
            lock.lock();
            try {
                isCompleted = true;
                completed.signal();
            } finally {
                lock.unlock();
            }
        }

        public void waitTillComplete() {
            lock.lock();
            try {
                if (!isCompleted) {
                    completed.await();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(RealTimeDB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                lock.unlock();
            }
        }

        public boolean waitTillComplete(long time, TimeUnit units) {
            lock.lock();
            boolean signaled = false;
            try {
                if (!isCompleted) {
                    signaled = completed.await(time, units);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(RealTimeDB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                lock.unlock();
            }
            return signaled;
        }
    }
}
