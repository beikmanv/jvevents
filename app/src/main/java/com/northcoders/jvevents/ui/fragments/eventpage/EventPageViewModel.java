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

public class EventPageViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final MutableLiveData<EventDTO> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<EventDTO>> allEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateEventStatus = new MutableLiveData<>();
    private final MutableLiveData<EventDTO> selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> launchCalendarEvent = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public EventPageViewModel(@NonNull Application application) {
        super(application);
        this.repository = new EventRepository(application);
    }

    public LiveData<List<EventDTO>> getAllEvents() {
        return repository.getAllEventsLiveData();
    }

    public LiveData<EventDTO> getEvent() {
        return eventLiveData;
    }

    public void fetchEventById(long id) {
        eventLiveData.setValue(repository.getEventByIdLiveData(id).getValue());
    }

    public void fetchAllEvents() {
        allEventsLiveData.setValue(repository.getAllEventsLiveData().getValue());
    }

    public void checkIfUserIsStaff() {
        repository.checkIfUserIsStaff();
    }

    public void updateEvent(EventDTO event) {
        updateEventStatus.setValue(repository.updateEvent(event).getValue());
    }

    public LiveData<Boolean> isUserStaff() {
        return repository.getIsUserStaff();
    }

    public LiveData<Boolean> getUpdateEventStatus() {
        return updateEventStatus;
    }

    // ✅ Getter for selected event
    public LiveData<EventDTO> getSelectedEvent() {
        return selectedEvent;
    }

    // ✅ Getter for launch calendar event flag
    public LiveData<Boolean> getLaunchCalendarEvent() {
        return launchCalendarEvent;
    }

    public void signUpForEvent(EventDTO event) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    String email = user.getEmail();
                    ApiService apiService = RetrofitInstance.getApiServiceWithAuth(idToken);
                    repository.signUpForEvent(apiService, event.getId(), email, success -> {
                        if (success) {
                            selectedEvent.setValue(event);
                            launchCalendarEvent.setValue(true);  // Trigger calendar dialog
                            toastMessage.setValue("Signed up for \"" + event.getTitle() + "\"");
                        } else {
                            selectedEvent.setValue(event);
                            toastMessage.setValue("Already signed up!");
                            launchCalendarEvent.setValue(true);  // Trigger calendar dialog
                        }
                    });
                } else {
                    toastMessage.setValue("Failed to get ID token.");
                }
            });
        } else {
            toastMessage.setValue("User not authenticated.");
        }
    }

    // ✅ Method to reset the launch calendar event
    public void resetLaunchCalendarEvent() {
        launchCalendarEvent.setValue(false);
    }
}
