package com.northcoders.jvevents.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {
    private List<EventDTO> cities = new ArrayList<>();
    private MutableLiveData<List<EventDTO>> allEventsLiveData = new MutableLiveData<>();
    private MutableLiveData<EventDTO> singleEventLiveData = new MutableLiveData<>();
    private ApiService service;
    private Application application;

    public EventRepository(Application application) {
        this.application = application;
        service = RetrofitInstance.getService();
    }

    public MutableLiveData<List<EventDTO>> getAllEventsLiveData() {

        Log.d("EventRepository", "Starting GET /events call...");

        Call<List<EventDTO>> call = service.getAllEvents();

        call.enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                Log.d("EventRepository", "onResponse called");
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("EventRepository", "Received " + response.body().size() + " events from backend.");
                    allEventsLiveData.setValue(response.body());
                } else {
                    Log.e("EventRepository", "Response unsuccessful or empty: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                Log.e("EventRepository", "API call failed: " + t.getMessage());
            }
        });

        return allEventsLiveData;
    }

    public MutableLiveData<EventDTO> getEventByIdLiveData(long id) {
        Call<EventDTO> call = service.getEventById(id);

        call.enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    singleEventLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                Log.i("GET /events/{id}", t.getMessage());
            }
        });

        return singleEventLiveData;
    }
}
