package com.example.praktika2;

import com.google.firebase.firestore.FirebaseFirestore;

public class DatabaseHelper {

    private static FirebaseFirestore instance;

    private DatabaseHelper() { }

    public static synchronized FirebaseFirestore getInstance() {
        if (instance == null) {
            instance = FirebaseFirestore.getInstance();
        }
        return instance;
    }
}