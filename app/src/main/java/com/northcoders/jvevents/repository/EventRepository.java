package com.northcoders.jvevents.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {
    private final ApiService service;
    private final MutableLiveData<List<EventDTO>> allEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventDTO> singleEventLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserStaffLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateEventStatus = new MutableLiveData<>();

    public EventRepository(Application application) {
        this.service = RetrofitInstance.getApiService();
    }

    public MutableLiveData<List<EventDTO>> getAllEventsLiveData() {
        service.getAllEvents().enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allEventsLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                Log.e("EventRepository", "Error fetching events: " + t.getMessage());
            }
        });
        return allEventsLiveData;
    }

    public MutableLiveData<EventDTO> getEventByIdLiveData(long id) {
        service.getEventById(id).enqueue(new Callback<EventDTO>() {
            @Override
            public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    singleEventLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<EventDTO> call, Throwable t) {
                Log.e("EventRepository", "Error fetching event by ID: " + t.getMessage());
            }
        });
        return singleEventLiveData;
    }

    public MutableLiveData<Boolean> updateEvent(EventDTO event) {
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    String idToken = result.getToken();
                    ApiService api = RetrofitInstance.getApiServiceWithAuth(idToken);
                    api.updateEvent(event.getId(), event).enqueue(new Callback<EventDTO>() {
                        @Override
                        public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                            updateEventStatus.setValue(response.isSuccessful());
                        }

                        @Override
                        public void onFailure(Call<EventDTO> call, Throwable t) {
                            updateEventStatus.setValue(false);
                        }
                    });
                });
        return updateEventStatus;
    }

    // ✅ Check if user is staff (RESTORED)
    public void checkIfUserIsStaff() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            isUserStaffLiveData.setValue(false);
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    String idToken = result.getToken();
                    ApiService authedService = RetrofitInstance.getApiServiceWithAuth(idToken);
                    authedService.isUserStaff(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                            .enqueue(new Callback<Boolean>() {
                                @Override
                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                    boolean isStaff = response.isSuccessful() && Boolean.TRUE.equals(response.body());
                                    isUserStaffLiveData.setValue(isStaff);
                                }

                                @Override
                                public void onFailure(Call<Boolean> call, Throwable t) {
                                    isUserStaffLiveData.setValue(false);
                                    Log.e("EventRepository", "Failed to check staff status: " + t.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    isUserStaffLiveData.setValue(false);
                    Log.e("EventRepository", "Failed to get ID token: " + e.getMessage());
                });
    }

    // ✅ Expose staff status
    public LiveData<Boolean> getIsUserStaff() {
        return isUserStaffLiveData;
    }
}
