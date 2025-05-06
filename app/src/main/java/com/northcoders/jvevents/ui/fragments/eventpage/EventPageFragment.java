package com.northcoders.jvevents.ui.fragments.eventpage;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.FragmentEventPageBinding;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;
import com.northcoders.jvevents.ui.adapters.EventAdapter;
import com.northcoders.jvevents.ui.adapters.UserAdapter;
import com.northcoders.jvevents.ui.mainactivity.RecyclerViewInterface;
import com.northcoders.jvevents.util.OnStaffCheckCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventPageFragment extends Fragment implements RecyclerViewInterface {

    private FragmentEventPageBinding fragmentEventPageBinding;
    private UserAdapter userAdapter;
    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;
    private boolean showCalendarThankYou = false;

    public EventPageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EventPageViewModel viewModel = new ViewModelProvider(this).get(EventPageViewModel.class);
        fragmentEventPageBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_page, container, false);

        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                checkIfUserIsStaff(isStaff -> {
                    eventAdapter = new EventAdapter(requireContext(), events, this, isStaff, new EventAdapter.EventItemListener() {
                        @Override
                        public void onSeeAttendeesClick(EventDTO event) {
                            Toast.makeText(requireContext(), "See Attendees clicked for " + event.getTitle(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onEditEventClick(EventDTO event) {
                            Toast.makeText(requireContext(), "Edit Event clicked for " + event.getTitle(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    fragmentEventPageBinding.eventListRecyclerView.setAdapter(eventAdapter);
                });
                fragmentEventPageBinding.eventListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                fragmentEventPageBinding.eventListRecyclerView.setHasFixedSize(true);
            }
        });

        viewModel.fetchAllEvents();
        return fragmentEventPageBinding.getRoot();
    }

    private void checkIfUserIsStaff(OnStaffCheckCallback callback) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            callback.onResult(false);
            return;
        }

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    String idToken = result.getToken();
                    if (idToken == null) {
                        callback.onResult(false);
                        return;
                    }

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(chain -> {
                                return chain.proceed(chain.request().newBuilder()
                                        .addHeader("Authorization", "Bearer " + idToken)
                                        .build());
                            })
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://10.0.2.2:8085/api/v1/")
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService authedService = retrofit.create(ApiService.class);

                    authedService.isUserStaff(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                            .enqueue(new Callback<Boolean>() {
                                @Override
                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                    Log.d("DEBUG", "isStaff API raw response: " + response.body());
                                    boolean isStaff = response.body() != null && response.body();
                                    callback.onResult(isStaff);
                                }

                                @Override
                                public void onFailure(Call<Boolean> call, Throwable t) {
                                    Log.e("DEBUG", "isStaff check failed", t);
                                    callback.onResult(false);
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("AUTH", "Failed to get ID token", e);
                    callback.onResult(false);
                });
    }

    @Override
    public void onItemClick(int position) {
        EventDTO event = eventAdapter.getEvents().get(position);
        String email = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : null;

        if (email == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        signUpForEvent(event.getId(), email, event);
    }

    private void signUpForEvent(Long eventId, String email, EventDTO event) {
        FirebaseAuth.getInstance().getCurrentUser()
                .getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();
                        OkHttpClient client = new OkHttpClient();

                        HttpUrl url = HttpUrl.parse("http://10.0.2.2:8085/api/v1/events/" + eventId + "/signup")
                                .newBuilder()
                                .addQueryParameter("email", email)
                                .build();

                        Request request = new Request.Builder()
                                .url(url)
                                .header("Authorization", "Bearer " + idToken)
                                .post(RequestBody.create(new byte[0]))
                                .build();

                        okhttp3.Call call = client.newCall(request);
                        call.enqueue(new okhttp3.Callback() {
                            @Override
                            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                                Log.e(TAG, "Sign-up request failed", e);
                            }

                            @Override
                            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                                requireActivity().runOnUiThread(() -> {
                                    if (response.isSuccessful()) {
                                        Log.d(TAG, "Successfully signed up for event!");
                                        launchCalendarIntent(event);
                                    } else if (response.code() == 409) {
                                        Toast.makeText(getContext(), "You are already signed up for this event!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Signup failed. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Authentication error. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchCalendarIntent(EventDTO event) {
        Context context = requireContext();
        PackageManager pm = context.getPackageManager();

        long startMillis;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(event.getEventDate());
            startMillis = date.getTime();
        } catch (Exception e) {
            Toast.makeText(context, "Invalid event date", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent insertIntent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startMillis + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation());

        if (insertIntent.resolveActivity(pm) != null) {
            showCalendarThankYou = true;
            context.startActivity(insertIntent);
        } else {
            new AlertDialog.Builder(context)
                    .setTitle("Open Google Calendar")
                    .setMessage("We'll open your Calendar now. Please add the event manually.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        Intent fallbackIntent = new Intent();
                        fallbackIntent.setComponent(new ComponentName(
                                "com.google.android.calendar",
                                "com.android.calendar.AllInOneActivity"
                        ));
                        if (fallbackIntent.resolveActivity(pm) != null) {
                            showCalendarThankYou = true;
                            context.startActivity(fallbackIntent);
                        } else {
                            Toast.makeText(context, "No calendar app found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showCalendarThankYou) {
            showCalendarThankYou = false;
            new AlertDialog.Builder(requireContext())
                    .setTitle("Event Added")
                    .setMessage("Thanks for adding the event!")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
