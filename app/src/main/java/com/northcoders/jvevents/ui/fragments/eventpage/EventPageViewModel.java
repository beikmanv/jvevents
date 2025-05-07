package com.northcoders.jvevents.ui.fragments.eventpage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.repository.EventRepository;

import java.util.List;

public class EventPageViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final MutableLiveData<EventDTO> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<EventDTO>> allEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserStaffLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateEventStatus = new MutableLiveData<>();

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
}
