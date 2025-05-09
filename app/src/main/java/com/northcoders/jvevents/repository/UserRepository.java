package com.northcoders.jvevents.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private final MutableLiveData<Boolean> backendAuthStatus = new MutableLiveData<>();

    /**
     * Sends the verified Firebase ID Token to the backend for further verification.
     * This method uses the verified Firebase ID Token obtained from Firebase SDK.
     */
    public void sendIdTokenToBackend(String firebaseIdToken) {
        ApiService apiService = RetrofitInstance.getApiServiceWithAuth(firebaseIdToken);

        apiService.loginWithGoogle(firebaseIdToken).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Backend verification successful: " + response.body());
                    backendAuthStatus.setValue(true);
                } else {
                    handleBackendError(response);
                    backendAuthStatus.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "❌ Network error during backend verification", t);
                backendAuthStatus.setValue(false);
            }
        });
    }

    private void handleBackendError(Response<Map<String, String>> response) {
        try {
            String rawResponse = response.errorBody() != null ? response.errorBody().string() : "No error body";
            Log.e(TAG, "❌ Backend verification failed. Code: " + response.code() + " - Raw response: " + rawResponse);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading backend response (Malformed JSON)", e);
        }
    }

    public MutableLiveData<Boolean> getBackendAuthStatus() {
        return backendAuthStatus;
    }
}
