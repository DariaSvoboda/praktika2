package com.example.praktika2;

import com.google.firebase.auth.FirebaseAuth;

public class AuthHelper {

    private static FirebaseAuth instance;

    private AuthHelper() { }

    public static synchronized FirebaseAuth getInstance() {
        if (instance == null) {
            instance = FirebaseAuth.getInstance();
        }
        return instance;
    }
}