package com.northcoders.jvevents.ui.fragments.profilepage;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePageViewModel extends AndroidViewModel {

    private static final String TAG = "ProfilePageViewModel";
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authStatus = new MutableLiveData<>();

    public ProfilePageViewModel(@NonNull Application application) {
        super(application);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userLiveData.setValue(currentUser);
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getAuthStatus() {
        return authStatus;
    }

    public void signInWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        userLiveData.setValue(user);
                        authStatus.setValue(true);
                        sendIdTokenToBackend(idToken);
                    } else {
                        Log.e(TAG, "Firebase Auth failed: ", task.getException());
                        authStatus.setValue(false);
                    }
                });
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        authStatus.setValue(false);
        userLiveData.setValue(null);
    }

    void sendIdTokenToBackend(String idToken) {
        ApiService apiService = RetrofitInstance.getApiServiceWithAuth(idToken);

        apiService.loginWithGoogle(idToken).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Backend token verification success: " + response.body());
                    } else {
                        // Log the full raw response for debugging
                        String rawResponse = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "❌ Backend verification failed. Code: " + response.code() + " - Raw response: " + rawResponse);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error reading backend response", e);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "❌ Network error during backend verification", t);
            }
        });
    }
}
