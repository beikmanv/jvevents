package com.northcoders.jvevents.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authStatus = new MutableLiveData<>();

    /**
     * Constructor - Initializes user state with the currently signed-in Firebase user.
     */
    public UserRepository() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLiveData.setValue(currentUser);
            authStatus.setValue(true);
            Log.d(TAG, "✅ User already signed in: " + currentUser.getDisplayName());
        } else {
            userLiveData.setValue(null);
            authStatus.setValue(false);
            Log.d(TAG, "❌ No user signed in.");
        }
    }

    /**
     * Initiates Google Sign-In and retrieves a verified Firebase ID Token.
     */
    public void signInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true).addOnCompleteListener(idTokenTask -> {
                                if (idTokenTask.isSuccessful() && idTokenTask.getResult() != null) {
                                    String firebaseIdToken = idTokenTask.getResult().getToken();
                                    if (firebaseIdToken != null) {
                                        userLiveData.setValue(user);
                                        sendIdTokenToBackend(firebaseIdToken);
                                        authStatus.setValue(true);
                                    }
                                } else {
                                    Log.e(TAG, "❌ Failed to get Firebase ID Token", idTokenTask.getException());
                                    authStatus.setValue(false);
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "❌ Firebase Authentication failed", task.getException());
                        authStatus.setValue(false);
                    }
                });
    }

    /**
     * Sends the verified Firebase ID Token to the backend for further verification.
     */
    private void sendIdTokenToBackend(String firebaseIdToken) {
        ApiService apiService = RetrofitInstance.getApiServiceWithAuth(firebaseIdToken);

        apiService.loginWithGoogle(firebaseIdToken).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Backend verification successful: " + response.body());
                } else {
                    handleBackendError(response);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "❌ Network error during backend verification", t);
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

    /**
     * Logs out the user.
     */
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        userLiveData.setValue(null);
        authStatus.setValue(false);
        Log.d(TAG, "✅ User signed out.");
    }

    /**
     * Exposes user data and authentication status to the ViewModel.
     */
    public MutableLiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<Boolean> getAuthStatus() {
        return authStatus;
    }
}
