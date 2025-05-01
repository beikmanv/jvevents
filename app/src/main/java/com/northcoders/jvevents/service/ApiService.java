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

    @GET("events/{eventId}")
    Call<EventDTO> getEventById(@Path("eventId") long eventId);

    @POST("events/create")
    Call<EventDTO> createEvent(@Body EventDTO event);

    @PUT("events/update/{eventId}")
    Call<EventDTO> updateEvent(@Path("eventId") long eventId, @Body EventDTO event);

    @DELETE("events/{eventId}")
    Call<Void> deleteEvent(@Path("eventId") long eventId);

    @POST("events/{eventId}/signup")
    Call<Void> signupForEvent(@Path("eventId") long eventId, @Query("email") String email);

    @GET("events/{eventId}/users")
    Call<List<AppUserDTO>> getUsersForEvent(@Path("eventId") long eventId);

    // ----------------- USERS ---------------------

    @GET("users")
    Call<List<AppUserDTO>> getAllUsers();

    @GET("users/{userId}")
    Call<AppUserDTO> getUserById(@Path("userId") long userId);

    @DELETE("users/{userId}/delete")
    Call<Void> deleteUserById(@Path("userId") long userId);

    @GET("users/{userId}/events")
    Call<List<EventDTO>> getEventsForUser(@Path("userId") long userId);

    @GET("users/user")
    Call<Map<String, String>> getCurrentUser(); // Expected map with key "email"

    // ----------------- ADMIN ---------------------

    @PUT("admin/set-staff/{userId}")
    Call<String> setStaffStatus(@Path("userId") long userId, @Query("isStaff") boolean isStaff);

    @GET("admin/check-staff")
    Call<String> checkIfStaff();

    @GET("admin/is-staff")
    Call<Boolean> isUserStaff(@Query("email") String email);

    // ----------------- AUTH ----------------------

    @POST("auth/firebase/verify-token")
    Call<Map<String, String>> loginWithGoogle(@Body Map<String, String> tokenMap);

}
