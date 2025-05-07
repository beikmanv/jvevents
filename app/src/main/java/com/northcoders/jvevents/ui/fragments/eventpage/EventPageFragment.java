package com.northcoders.jvevents.ui.fragments.eventpage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.DialogEditEventBinding;
import com.northcoders.jvevents.databinding.FragmentEventPageBinding;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.ui.adapters.EventAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EventPageFragment extends Fragment {

    private FragmentEventPageBinding binding;
    private EventPageViewModel viewModel;
    private EventAdapter eventAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EventPageViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_page, container, false);

        setupRecyclerView();
        observeViewModel();

        // ✅ Fetch all events and check staff status
        viewModel.fetchAllEvents();
        viewModel.checkIfUserIsStaff();

        return binding.getRoot();
    }

    // ✅ Setting up the RecyclerView with Adapter
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(requireContext(), new ArrayList<>(), false, new EventAdapter.EventItemListener() {
            @Override
            public void onSeeAttendeesClick(EventDTO event) {
                Toast.makeText(requireContext(), "See Attendees clicked for " + event.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditEventClick(EventDTO event) {
                showEditEventDialog(event);
            }
        });

        binding.eventListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventListRecyclerView.setAdapter(eventAdapter);
    }

    // ✅ Observe ViewModel for data changes
    private void observeViewModel() {
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                eventAdapter.updateEvents(events);
            }
        });

        viewModel.isUserStaff().observe(getViewLifecycleOwner(), isStaff -> {
            if (isStaff != null) {
                eventAdapter.setIsStaff(isStaff);
            } else {
                eventAdapter.setIsStaff(false);
            }
        });

        viewModel.getUpdateEventStatus().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(requireContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                viewModel.fetchAllEvents(); // Refresh events list
            } else if (isSuccess != null) {
                Toast.makeText(requireContext(), "Failed to update event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Show Edit Event Dialog
    private void showEditEventDialog(EventDTO event) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        DialogEditEventBinding dialogBinding = DialogEditEventBinding.inflate(inflater);
        dialogBinding.setEvent(event);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Event")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    viewModel.updateEvent(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ✅ Launch Calendar Intent
    private void launchCalendarIntent(EventDTO event) {
        long startMillis;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(event.getEventDate());
            startMillis = date.getTime();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Invalid event date", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription());

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "No calendar app found.", Toast.LENGTH_SHORT).show();
        }
    }
}
