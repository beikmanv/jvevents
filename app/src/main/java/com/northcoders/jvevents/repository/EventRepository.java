package com.northcoders.jvevents.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;
import com.northcoders.jvevents.util.SignUpCallback;

import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventRepository {
    private final ApiService apiService;
    private final MutableLiveData<List<EventDTO>> allEventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<EventDTO> singleEventLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUserStaffLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateEventStatus = new MutableLiveData<>();

    public EventRepository(Application application) {
        this.apiService = RetrofitInstance.getApiService();
    }

    // ✅ Fetch All Events
    public LiveData<List<EventDTO>> getAllEventsLiveData() {
        apiService.getAllEvents().enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allEventsLiveData.setValue(response.body());
                } else {
                    Log.e("EventRepository", "Failed to fetch events: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                Log.e("EventRepository", "Error fetching events: " + t.getMessage());
            }
        });
        return allEventsLiveData;
    }

    // ✅ Fetch Single Event by ID
    public LiveData<EventDTO> getEventByIdLiveData(long id) {
        apiService.getEventById(id).enqueue(new Callback<EventDTO>() {
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

    // ✅ Update Event (For Staff)
    public LiveData<Boolean> updateEvent(EventDTO event) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(tokenResult -> {
                    String idToken = tokenResult.getToken();
                    ApiService api = RetrofitInstance.getApiServiceWithAuth(idToken);
                    api.updateEvent(event.getId(), event).enqueue(new Callback<EventDTO>() {
                        @Override
                        public void onResponse(Call<EventDTO> call, Response<EventDTO> response) {
                            result.setValue(response.isSuccessful());
                        }

                        @Override
                        public void onFailure(Call<EventDTO> call, Throwable t) {
                            result.setValue(false);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e("EventRepository", "Failed to get ID token: " + e.getMessage());
                });
        return result;
    }

    // ✅ Check if User is Staff
    public void checkIfUserIsStaff() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            isUserStaffLiveData.setValue(false);
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(tokenResult -> {
                    String idToken = tokenResult.getToken();
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

    public void signUpForEvent(Long eventId, String email, Consumer<Boolean> callback) {
        Call<Void> call = apiService.signupForEvent(eventId, email);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.accept(true);
                } else if (response.code() == 409) {
                    callback.accept(false); // Already signed up
                } else {
                    callback.accept(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.accept(false);
            }
        });
    }

}
