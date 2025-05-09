package com.northcoders.jvevents.repository;

import android.util.Log;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

public class AuthRepository {

    public interface AuthCallback {
        void onComplete(boolean success);
    }

    public static void authenticateWithBackend(String idToken, AuthCallback callback) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.get("application/json");
        String json = "{\"token\":\"" + idToken + "\"}";

        Log.d("AuthRepository", "Sending JSON: " + json); // 👈 LOG IT!

        RequestBody body = RequestBody.create(json, mediaType);

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8085/api/auth/google")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("AuthRepository", "Network error", e);
                callback.onComplete(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    Log.d("AuthRepository", "Backend response: " + response.code());
                    callback.onComplete(response.isSuccessful());
                } catch (Exception e) {
                    Log.e("AuthRepository", "Error reading response body", e);
                    callback.onComplete(false);
                }
            }
        });
    }
}