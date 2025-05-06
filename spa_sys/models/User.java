package com.example.spa_sys.models;

public class User {
    private static int loggedInUserId;

    // Setters and getters for the user ID
    public static void setLoggedInUserId(int userId) {
        loggedInUserId = userId;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    // Optionally add other fields for user info like name, email, etc.
}

