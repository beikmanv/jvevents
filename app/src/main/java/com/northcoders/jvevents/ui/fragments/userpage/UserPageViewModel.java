package com.northcoders.jvevents.ui.fragments.userpage;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseUser;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.repository.UserRepository;

import java.util.List;

public class UserPageViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<AppUserDTO>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<EventDTO>> eventsLiveData = new MutableLiveData<>();

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

    public void signInWithGoogle(String idToken) {
        userRepository.signInWithGoogle(idToken);
    }

    public void signOut() {
        userRepository.signOut();
    }

    public void fetchUsers() {
        userRepository.fetchUsers(usersLiveData);
    }

    public void fetchUserEvents(long userId) {
        userRepository.fetchUserEvents(userId, eventsLiveData);
    }

    public LiveData<List<AppUserDTO>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<List<EventDTO>> getEventsLiveData() {
        return eventsLiveData;
    }

}
