package com.northcoders.jvevents.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {

    private static final String TAG = "PaymentRepository";

    public void sendGooglePayToken(String token, MutableLiveData<String> paymentMessage) {
        Log.d(TAG, "📡 sendGooglePayToken() CALLED with token: " + token);

        if (token == null || token.trim().isEmpty()) {
            Log.e(TAG, "❌ Invalid token received");
            paymentMessage.postValue("Invalid payment token.");
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "❌ No authenticated user found.");
            paymentMessage.postValue("User not authenticated.");
            return;
        }

        auth.getCurrentUser().getIdToken(true).addOnSuccessListener(idTokenResult -> {
            String idToken = idTokenResult.getToken();
            ApiService apiService = RetrofitInstance.getApiServiceWithAuth(idToken);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("token", token);

            apiService.sendGooglePayToken(requestBody).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "📬 onResponse: " + response.code());
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "✅ Payment response: " + responseBody);
                            paymentMessage.postValue("Thanks! Payment successful.");
                        } else {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "❌ Payment failed - Code: " + response.code() + ", Error: " + errorBody);
                            paymentMessage.postValue("Payment failed. Please try again.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error reading payment response", e);
                        paymentMessage.postValue("Unexpected error. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "❌ Network error during payment", t);
                    paymentMessage.postValue("Network error. Please check your connection.");
                }
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "❌ Failed to retrieve Firebase ID token", e);
            paymentMessage.postValue("Authentication failed. Please try again.");
        });
    }
}
