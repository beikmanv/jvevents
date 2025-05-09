package com.northcoders.jvevents.ui.fragments.eventpage;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.repository.EventRepository;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;

import java.util.List;

// It is the brain of the EventPageFragment. It handles all the logic and data that the fragment needs.
// It is designed to store and manage UI-related data in a way that survives configuration changes (like screen rotations).
// It separates UI logic (handled in the Fragment) from business logic and data (handled here). It connects your EventPageFragment with your EventRepository.
public class EventPageViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final MutableLiveData<EventDTO> selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> launchCalendarEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showCalendarThankYou = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public EventPageViewModel(@NonNull Application application) {
        super(application);
        this.repository = new EventRepository(application);
    }

    public LiveData<List<EventDTO>> getAllEvents() {
        return repository.getAllEventsLiveData();
    }

    public LiveData<EventDTO> getSelectedEvent() {
        return selectedEvent;
    }

    public void updateEvent(EventDTO event) {
        repository.updateEvent(event).observeForever(success -> {
            if (Boolean.TRUE.equals(success)) {
                fetchAllEvents(); // Refresh the event list
                toastMessage.setValue("Event updated successfully.");
            } else {
                toastMessage.setValue("Failed to update event.");
            }
        });
    }

    public LiveData<Boolean> getLaunchCalendarEvent() {
        return launchCalendarEvent;
    }

    public LiveData<Boolean> getShowCalendarThankYou() {
        return showCalendarThankYou;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void fetchAllEvents() {
        repository.getAllEventsLiveData();
    }

    public void checkIfUserIsStaff() {
        repository.checkIfUserIsStaff();
    }

    public LiveData<Boolean> isUserStaff() {
        return repository.getIsUserStaff();
    }

    public void signUpForEvent(EventDTO event) {
        repository.signUpForEvent(event.getId(), success -> {
            if (success) {
                selectedEvent.setValue(event);
                launchCalendarEvent.setValue(true);
            } else {
                toastMessage.setValue("Already signed up!");
            }
        });
    }

    public void triggerCalendarThankYou() {
        showCalendarThankYou.setValue(true);
    }

    public void resetLaunchCalendarEvent() {
        launchCalendarEvent.setValue(false);
    }

    public void resetShowCalendarThankYou() {
        showCalendarThankYou.setValue(false);
    }

    public void triggerCalendarEvent(EventDTO event) {
        selectedEvent.setValue(event);
        launchCalendarEvent.setValue(true);
    }
}


