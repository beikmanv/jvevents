package com.northcoders.jvevents.ui.fragments.eventpage;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.repository.EventRepository;

import java.util.List;

public class EventPageViewModel extends AndroidViewModel {
    private final MutableLiveData<EventDTO> eventLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<EventDTO>> allEventsLiveData = new MutableLiveData<>(); // ✅ correct type
    private final EventRepository repository;

    public EventPageViewModel(@NonNull Application application) {
        super(application);
        this.repository = new EventRepository(application);
    }

    public LiveData<EventDTO> getEvent() {
        return eventLiveData;
    }

    public void setEvent(EventDTO event) {
        eventLiveData.setValue(event);
    }

    public void fetchEventById(long id) {
        LiveData<EventDTO> repositoryLiveData = repository.getEventByIdLiveData(id);
        repositoryLiveData.observeForever(event -> {
            Log.d("EventPageViewModel", "Fetched single event: " + event);
            eventLiveData.setValue(event); // ✅ update the correct LiveData
        });
    }

    public void fetchAllEvents() {
        Log.d("EventPageViewModel", "Fetching all events...");
        LiveData<List<EventDTO>> repoLiveData = repository.getAllEventsLiveData();
        repoLiveData.observeForever(events -> {
            if (events != null) {
                Log.d("EventPageViewModel", "Received " + events.size() + " events");
            } else {
                Log.e("EventPageViewModel", "No events received (null)");
            }
            allEventsLiveData.setValue(events);
        });
    }

    public LiveData<List<EventDTO>> getAllEvents() {
        return allEventsLiveData;
    }
}
