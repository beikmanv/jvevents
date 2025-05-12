package com.northcoders.jvevents.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authStatus = new MutableLiveData<>();

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
                                        Log.d(TAG, "✅ Signed in with Google: " + user.getDisplayName());
                                    } else {
                                        Log.e(TAG, "❌ Firebase ID Token is null.");
                                    }
                                } else {
                                    Log.e(TAG, "❌ Failed to get Firebase ID Token", idTokenTask.getException());
                                    authStatus.setValue(false);
                                }
                            });
                        } else {
                            Log.e(TAG, "❌ User is null after successful sign-in.");
                        }
                    } else {
                        Log.e(TAG, "❌ Firebase Authentication failed", task.getException());
                        authStatus.setValue(false);
                    }
                });
    }

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

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        userLiveData.setValue(null);
        authStatus.setValue(false);
        Log.d(TAG, "✅ User signed out.");
    }

    public MutableLiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<Boolean> getAuthStatus() {
        return authStatus;
    }

    public void fetchUsers(MutableLiveData<List<AppUserDTO>> usersLiveData) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            usersLiveData.postValue(null);
            Log.d(TAG, "❌ No current user while fetching users.");
            return;
        }

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult().getToken();
                ApiService apiService = RetrofitInstance.getApiServiceWithAuth(token);
                apiService.getAllUsers("Bearer " + token).enqueue(new Callback<List<AppUserDTO>>() {
                    @Override
                    public void onResponse(Call<List<AppUserDTO>> call, Response<List<AppUserDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            usersLiveData.postValue(response.body());
                        } else {
                            Log.e(TAG, "❌ Failed to fetch users: " + response.message());
                            usersLiveData.postValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AppUserDTO>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error fetching users", t);
                        usersLiveData.postValue(null);
                    }
                });
            } else {
                Log.e(TAG, "❌ Token retrieval failed when fetching users.");
                usersLiveData.postValue(null);
            }
        });
    }

    public void fetchUserEvents(long userId, MutableLiveData<List<EventDTO>> eventsLiveData) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            eventsLiveData.postValue(null);
            Log.d(TAG, "❌ No current user while fetching events.");
            return;
        }

        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult().getToken();
                ApiService apiService = RetrofitInstance.getApiServiceWithAuth(token);
                apiService.getEventsForUser(userId).enqueue(new Callback<List<EventDTO>>() {
                    @Override
                    public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            eventsLiveData.postValue(response.body());
                        } else {
                            Log.e(TAG, "❌ Failed to fetch events: " + response.message());
                            eventsLiveData.postValue(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error fetching events", t);
                        eventsLiveData.postValue(null);
                    }
                });
            } else {
                Log.e(TAG, "❌ Token retrieval failed when fetching events.");
                eventsLiveData.postValue(null);
            }
        });
    }
}
