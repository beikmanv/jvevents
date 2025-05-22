package com.northcoders.jvevents.util;

public class SignupResult {
    public final boolean success;
    public final String message;

    public SignupResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
