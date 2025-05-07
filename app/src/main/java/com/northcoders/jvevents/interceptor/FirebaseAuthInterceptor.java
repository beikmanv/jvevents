package com.northcoders.jvevents.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class FirebaseAuthInterceptor implements Interceptor {
    private final String idToken;

    public FirebaseAuthInterceptor(String idToken) {
        this.idToken = idToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + idToken)
                .build();
        return chain.proceed(authenticatedRequest);
    }
}
