package com.northcoders.jvevents.ui.fragments.eventpage;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
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
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.ui.adapters.EventAdapter;
import com.northcoders.jvevents.ui.adapters.UserAdapter;
import com.northcoders.jvevents.ui.mainactivity.RecyclerViewInterface;

import java.io.IOException;

import okhttp3.*;

public class EventPageFragment extends Fragment implements RecyclerViewInterface {

    private FragmentEventPageBinding fragmentEventPageBinding;
    private UserAdapter userAdapter;
    private EventPageViewModel eventPageViewModel;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;

    public EventPageFragment() {}

    private void displayInRecyclerView(EventDTO eventDTO) {
        recyclerView = fragmentEventPageBinding.eventListRecyclerView;

        userAdapter = new UserAdapter(requireContext(), eventDTO.getUsers(), this);
        recyclerView.setAdapter(userAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        userAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EventPageViewModel viewModel = new ViewModelProvider(this).get(EventPageViewModel.class);
        fragmentEventPageBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_page, container, false);
        View view = fragmentEventPageBinding.getRoot();

        // Observe and bind events
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                eventAdapter = new EventAdapter(requireContext(), events, this);
                fragmentEventPageBinding.eventListRecyclerView.setAdapter(eventAdapter);
                fragmentEventPageBinding.eventListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                fragmentEventPageBinding.eventListRecyclerView.setHasFixedSize(true);
            }
        });

        // Trigger API call
        viewModel.fetchAllEvents();

        return view;
    }

    @Override
    public void onItemClick(int position) {
        EventDTO clickedEvent = eventAdapter.getEvents().get(position); // Updated: getItems() instead of getEvents()
        String email = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : null;

        if (email == null) {
            Toast.makeText(getContext(), "Please sign in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        signUpForEvent(clickedEvent.getId(), email);
    }

    private void signUpForEvent(Long eventId, String email) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl url = HttpUrl.parse("http://10.0.2.2:8085/api/v1/events/" + eventId + "/signup")
                .newBuilder()
                .addQueryParameter("email", email)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(new byte[0]))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("EventPageFragment", "Sign-up request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Successfully signed up for event!");
                        Toast.makeText(getContext(), "Successfully signed up!", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 409) {
                        Log.w(TAG, "Already signed up for event.");
                        Toast.makeText(getContext(), "You are already signed up for this event!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to sign up. Response code: " + response.code());
                        Toast.makeText(getContext(), "Signup failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
