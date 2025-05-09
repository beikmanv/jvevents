package com.northcoders.jvevents.ui.fragments.userpage;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.*;
import com.northcoders.jvevents.repository.UserRepository;

public class UserPageViewModel extends AndroidViewModel {

    private static final String TAG = "UserPageViewModel";
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authStatus = new MutableLiveData<>();
    private final UserRepository userRepository;

    public UserPageViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userLiveData.setValue(currentUser);
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getAuthStatus() {
        return authStatus;
    }

    /**
     * This method signs in using a Google ID Token and sends a verified Firebase ID Token to the backend.
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
                                        userRepository.sendIdTokenToBackend(firebaseIdToken);
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
     * This method logs the user out and updates the UI state.
     */
    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        authStatus.setValue(false);
        userLiveData.setValue(null);
    }
}
