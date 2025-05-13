package com.northcoders.jvevents.ui.fragments.eventpage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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

    private FragmentEventPageBinding binding; // Connects the layout (UI) to this code.
    private EventPageViewModel viewModel; // Manages all the data and logic for this fragment.
    private EventAdapter eventAdapter; // Manages the list of events (RecyclerView).

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View nav = requireActivity().findViewById(R.id.bottomnavbar);
        if (nav != null) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) nav.getLayoutParams();
            params.setBehavior(new com.google.android.material.behavior.HideBottomViewOnScrollBehavior<>());
            nav.setLayoutParams(params);
        }
    }

    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(new ArrayList<>(), false, this, R.layout.event_item_layout);
        binding.eventListRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventListRecyclerView.setAdapter(eventAdapter);
    }

    // This listens for changes in the data (from the ViewModel)
    private void observeViewModel() {
        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) eventAdapter.updateEvents(events);
        });

        viewModel.isUserStaff().observe(getViewLifecycleOwner(), isStaff -> {
            eventAdapter.setIsStaff(isStaff != null && isStaff);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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

        viewModel.getShowCalendarThankYou().observe(getViewLifecycleOwner(), shouldShow -> {
            if (Boolean.TRUE.equals(shouldShow)) {
                showCalendarThankYouDialog();
                viewModel.resetShowCalendarThankYou();
            }
        });
    }

    @Override
    public void onItemClick(EventDTO event) {
        showSignUpAndCalendarConfirmationDialog(event);
    }

    @Override
    public void onSeeAttendeesClick(EventDTO event) {
        Toast.makeText(requireContext(), "See Attendees clicked for " + event.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditEventClick(EventDTO event) {
        showEditEventDialog(event);
    }

    private void showSignUpAndCalendarConfirmationDialog(EventDTO event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Up and Add to Calendar")
                .setMessage("Do you want to sign up for \"" + event.getTitle() + "\" and add it to your calendar?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.signUpForEvent(event);
                    viewModel.triggerCalendarEvent(event);  // Trigger calendar launch
                })
                .setNegativeButton("No", null)
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

        context.startActivity(insertIntent);
        viewModel.triggerCalendarThankYou();
    }

    private void showCalendarThankYouDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Event Added")
                .setMessage("Thanks for adding the event!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showEditEventDialog(EventDTO event) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        DialogEditEventBinding dialogBinding = DialogEditEventBinding.inflate(inflater);
        dialogBinding.setEvent(event);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Event")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Save", (dialog, which) -> {
                    EventDTO updatedEvent = dialogBinding.getEvent(); // Get the updated event
                    viewModel.updateEvent(updatedEvent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
