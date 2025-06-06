package com.northcoders.jvevents.ui.fragments.eventpage;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.repository.EventRepository;


import java.util.List;

public class EventPageViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final MutableLiveData<EventDTO> selectedEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> launchCalendarEvent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showCalendarThankYou = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<List<AppUserDTO>> attendeesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showAttendeesDialog = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    private final MutableLiveData<Boolean> updateEventSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getUpdateEventSuccess() { return updateEventSuccess; }
    private final MutableLiveData<Boolean> createEventSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getCreateEventSuccess() { return createEventSuccess; }
    private final MutableLiveData<Boolean> deleteEventSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getDeleteEventSuccess() { return deleteEventSuccess; }

    public LiveData<List<EventDTO>> getAllEvents() {
        return repository.getAllEventsLiveData(); // the version WITHOUT callback
    }

    public EventPageViewModel(@NonNull Application application) {
        super(application);
        this.repository = new EventRepository(application);
    }

    public LiveData<EventDTO> getSelectedEvent() {
        return selectedEvent;
    }

    public void updateEvent(EventDTO event) {
        repository.updateEvent(event, updateEventSuccess);
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
        isLoading.setValue(true);
        repository.getAllEventsLiveData(new EventRepository.LoadCallback() {
            @Override
            public void onLoaded() {
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                toastMessage.setValue(error);
            }
        });
    }

    public void checkIfUserIsStaff() {
        repository.checkIfUserIsStaff();
    }

    public LiveData<Boolean> isUserStaff() {
        return repository.getIsUserStaff();
    }

    public void signUpForEvent(EventDTO event) {
        repository.signUpForEvent(event.getId(), result -> {
            if (result.success) {
                selectedEvent.setValue(event);
                triggerCalendarEvent(event);
            } else {
                toastMessage.setValue(result.message);
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

    public void deleteEvent(EventDTO event) {
        repository.deleteEvent(event.getId(), deleteEventSuccess);
    }

    public LiveData<List<AppUserDTO>> getAttendeesLiveData() {
        return attendeesLiveData;
    }

    public LiveData<Boolean> getShowAttendeesDialog() {
        return showAttendeesDialog;
    }

    public void fetchAttendeesForEvent(EventDTO event) {
        repository.getAttendeesForEvent(event.getId(), attendees -> {
            if (attendees != null) {
                attendeesLiveData.postValue(attendees);
                showAttendeesDialog.postValue(true);
            } else {
                toastMessage.postValue("Failed to load attendees.");
            }
        });
    }

    public void resetShowAttendeesDialog() {
        showAttendeesDialog.setValue(false);
    }

    public void createEvent(EventDTO event) {
        repository.createEvent(event, createEventSuccess);
    }
}


