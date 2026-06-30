package com.example.praktika2;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DebugHelper {

    private static final String TAG = "DEBUG_HELPER";
    private static boolean isDebugMode = true;
    private static FirebaseFirestore db;

    public static void init() {
        db = DatabaseHelper.getInstance();
        logInfo("DebugHelper инициализирован");
    }

    public static void logInfo(String message) {
        if (isDebugMode) {
            Log.i(TAG, getTimestamp() + " INFO: " + message);
        }
    }

    public static void logWarning(String message) {
        if (isDebugMode) {
            Log.w(TAG, getTimestamp() + " WARN: " + message);
        }
    }

    public static void logError(String message, Exception e) {
        if (isDebugMode) {
            Log.e(TAG, getTimestamp() + " ERROR: " + message, e);
        }
    }

    public static void logError(String message) {
        if (isDebugMode) {
            Log.e(TAG, getTimestamp() + " ERROR: " + message);
        }
    }

    public static void logScreenOpen(String screenName) {
        logInfo("Открыт экран: " + screenName);
    }

    public static void logButtonClick(String buttonName) {
        logInfo("Нажата кнопка: " + buttonName);
    }

    public static void logFirebaseError(String operation, Exception e) {
        logError("Firebase ошибка при " + operation + ": " + e.getMessage(), e);
    }

    public static void logFirebaseSuccess(String operation) {
        logInfo("Firebase успех: " + operation);
    }

    public static void saveLog(String event, String details) {
        if (db == null) {
            db = DatabaseHelper.getInstance();
        }

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("event", event);
        logEntry.put("details", details);
        logEntry.put("timestamp", getTimestamp());

        db.collection("debug_logs")
                .add(logEntry)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Лог сохранён в Firestore: " + event))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Не удалось сохранить лог в Firestore: " + e.getMessage()));
    }

    public static void saveErrorLog(String operation, String errorMessage) {
        saveLog("ERROR", operation + " | " + errorMessage);
    }

    public static void setDebugMode(boolean enabled) {
        isDebugMode = enabled;
        Log.i(TAG, "Режим отладки: " + (enabled ? "включён" : "выключен"));
    }

    public static boolean isDebugMode() {
        return isDebugMode;
    }

    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        return sdf.format(new Date());
    }
}