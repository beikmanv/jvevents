package com.northcoders.jvevents.util;

@FunctionalInterface
public interface SignUpCallback {
    void onComplete(boolean success);
}
