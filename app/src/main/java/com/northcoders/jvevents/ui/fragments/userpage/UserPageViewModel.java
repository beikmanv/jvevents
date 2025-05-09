package com.northcoders.jvevents.ui.fragments.userpage;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseUser;
import com.northcoders.jvevents.repository.UserRepository;

public class UserPageViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    public UserPageViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userRepository.getUserLiveData();
    }

    public LiveData<Boolean> getAuthStatus() {
        return userRepository.getAuthStatus();
    }

    /**
     * Initiates Google Sign-In through the Repository.
     */
    public void signInWithGoogle(String idToken) {
        userRepository.signInWithGoogle(idToken);
    }

    /**
     * Logs out the user through the Repository.
     */
    public void signOut() {
        userRepository.signOut();
    }
}
