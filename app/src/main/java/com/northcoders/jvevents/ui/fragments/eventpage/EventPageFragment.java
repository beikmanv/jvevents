package com.northcoders.jvevents.ui.fragments.eventpage;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class EventPageFragment extends Fragment implements EventAdapter.OnEventActionListener {

    private FragmentEventPageBinding binding;
    private EventPageViewModel viewModel;
    private EventAdapter eventAdapter;
    private boolean showCalendarThankYou = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EventPageViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_page, container, false);

        setupRecyclerView();
        observeViewModel();

        viewModel.fetchAllEvents();
        viewModel.checkIfUserIsStaff();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(requireContext(), new ArrayList<>(), false, this);
        binding.eventListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventListRecyclerView.setAdapter(eventAdapter);
    }

    private void observeViewModel() {
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                eventAdapter.updateEvents(events);
            }
        });

        viewModel.isUserStaff().observe(getViewLifecycleOwner(), isStaff -> {
            eventAdapter.setIsStaff(isStaff != null && isStaff);
        });

        viewModel.getUpdateEventStatus().observe(getViewLifecycleOwner(), isSuccess -> {
            if (Boolean.TRUE.equals(isSuccess)) {
                Toast.makeText(requireContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                viewModel.fetchAllEvents();
            } else if (Boolean.FALSE.equals(isSuccess)) {
                Toast.makeText(requireContext(), "Failed to update event", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLaunchCalendarEvent().observe(getViewLifecycleOwner(), shouldLaunch -> {
            if (Boolean.TRUE.equals(shouldLaunch)) {
                EventDTO event = viewModel.getSelectedEvent().getValue();
                if (event != null) {
                    launchCalendarIntent(event);
                    viewModel.resetLaunchCalendarEvent();
                }
            }
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(EventDTO event) {
        showSignUpConfirmationDialog(event);
        viewModel.signUpForEvent(event);
    }

    @Override
    public void onSeeAttendeesClick(EventDTO event) {
        Toast.makeText(requireContext(), "See Attendees clicked for " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditEventClick(EventDTO event) {
        showEditEventDialog(event);
    }

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

    private void showSignUpConfirmationDialog(EventDTO event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Up for Event")
                .setMessage("Do you want to sign up for \"" + event.getTitle() + "\"?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.signUpForEvent(event); // Accessing the method from ViewModel
                })
                .setNegativeButton("No", null)
                .show();
    }
}
