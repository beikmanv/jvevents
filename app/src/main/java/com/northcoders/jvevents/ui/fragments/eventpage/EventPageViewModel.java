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

import java.util.List;

public class EventPageViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final MutableLiveData<EventDTO> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<EventDTO>> allEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserStaffLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateEventStatus = new MutableLiveData<>();
    private final MutableLiveData<EventDTO> selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> launchCalendarEvent = new MutableLiveData<>();

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
            String email = user.getEmail();
            repository.signUpForEvent(event.getId(), email, success -> {
                if (success) {
                    selectedEvent.setValue(event);
                    launchCalendarEvent.setValue(true);
                } else {
                    launchCalendarEvent.setValue(false);
                }
            });
        } else {
            launchCalendarEvent.setValue(false);
            Log.e("EventPageViewModel", "User is not authenticated.");
        }
    }

    // ✅ Method to reset the launch calendar event
    public void resetLaunchCalendarEvent() {
        launchCalendarEvent.setValue(false);
    }
}
