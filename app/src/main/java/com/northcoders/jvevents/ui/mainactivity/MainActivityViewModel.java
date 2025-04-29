package com.northcoders.jvevents.ui.mainactivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.repository.EventRepository;

import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {

    private EventRepository eventRepository;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        this.eventRepository = new EventRepository(application);
    }

    public LiveData<List<EventDTO>> fetchAllEvents() {
        return eventRepository.getAllEventsLiveData();
    }
}
