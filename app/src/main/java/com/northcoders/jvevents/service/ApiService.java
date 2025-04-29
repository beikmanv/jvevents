package com.northcoders.jvevents.service;

import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ----------------- EVENTS --------------------

    @GET("events")
    Call<List<EventDTO>> getAllEvents();

    @GET("events/{id}")
    Call<EventDTO> getEventById(@Path("id") long id);

    @POST("events/create")
    Call<EventDTO> createEvent(@Body EventDTO event);

    @PUT("events/update/{id}")
    Call<EventDTO> updateEvent(@Path("id") long id, @Body EventDTO event);

    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Path("id") long id);

    @POST("events/{id}/signup")
    Call<Void> signupForEvent(@Path("id") long id, @Query("email") String email);

    @GET("events/{id}/users")
    Call<List<AppUserDTO>> getUsersForEvent(@Path("id") long id);

    // ----------------- USERS ---------------------

    @GET("users")
    Call<List<AppUserDTO>> getAllUsers();

    @GET("users/{id}")
    Call<AppUserDTO> getUserById(@Path("id") long id);

    @DELETE("users/{id}/delete")
    Call<Void> deleteUserById(@Path("id") long id);

    @GET("users/{id}/events")
    Call<List<EventDTO>> getEventsForUser(@Path("id") long userId);

    @GET("users/user")
    Call<Map<String, String>> getCurrentUser(); // Expected map with key "email"

    // ----------------- ADMIN ---------------------

    @PUT("admin/set-staff/{userId}")
    Call<String> setStaffStatus(@Path("userId") long id, @Query("isStaff") boolean isStaff);

    @GET("admin/check-staff")
    Call<String> checkIfStaff();

    // ----------------- AUTH ----------------------

    @POST("auth/google")
    Call<Map<String, String>> loginWithGoogle(@Body Map<String, String> tokenMap);

    @GET("debug/cookies")
    Call<String> debugCookies();

}
