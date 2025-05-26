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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.DialogEditEventBinding;
import com.northcoders.jvevents.databinding.FragmentEventPageBinding;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;
import com.northcoders.jvevents.ui.adapters.AttendeeAdapter;
import com.northcoders.jvevents.ui.adapters.EventAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventPageFragment extends Fragment implements EventAdapter.OnEventActionListenerExtended {

    private FragmentEventPageBinding binding; // Connects the layout (UI) to this code.
    private EventPageViewModel viewModel; // Manages all the data and logic for this fragment.
    private EventAdapter eventAdapter; // Manages the list of events (RecyclerView).
    private boolean calendarOpened = false;
    private EventDTO pendingEvent = null;

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
        binding.createEventButton.setOnClickListener(v -> showCreateEventDialog());

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
            binding.createEventButton.setVisibility(
                    Boolean.TRUE.equals(isStaff) ? View.VISIBLE : View.GONE
            );
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getShowCalendarThankYou().observe(getViewLifecycleOwner(), shouldShow -> {
            if (Boolean.TRUE.equals(shouldShow)) {
                showCalendarThankYouDialog();
                viewModel.resetShowCalendarThankYou();
            }
        });

        viewModel.getAttendeesLiveData().observe(getViewLifecycleOwner(), attendees -> {
            // Just a LiveData update, real display logic is triggered by showAttendeesDialog observer
        });

        viewModel.getShowAttendeesDialog().observe(getViewLifecycleOwner(), show -> {
            if (Boolean.TRUE.equals(show)) {
                List<AppUserDTO> attendees = viewModel.getAttendeesLiveData().getValue();
                if (attendees != null) showAttendeesDialog(attendees);
                viewModel.resetShowAttendeesDialog();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onItemClick(EventDTO event) {
        showSignUpAndCalendarConfirmationDialog(event);
    }

    @Override
    public void onSeeAttendeesClick(EventDTO event) {
        viewModel.fetchAttendeesForEvent(event);
    }

    @Override
    public void onEditEventClick(EventDTO event) {
        showEditEventDialog(event);
    }

    private void showSignUpAndCalendarConfirmationDialog(EventDTO event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Event to Calendar")
                .setMessage("Would you like to add \"" + event.getTitle() + "\" to your calendar?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    pendingEvent = event;
                    calendarOpened = true;
                    launchCalendarIntent(event); // only opens calendar now
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

    @Override
    public void onDeleteEventClick(EventDTO event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteEvent(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAttendeesDialog(List<AppUserDTO> attendees) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_attendees, null);
        RecyclerView recyclerView = view.findViewById(R.id.attendeesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new AttendeeAdapter(attendees));

        new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (calendarOpened && pendingEvent != null) {
            calendarOpened = false;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Sign-Up")
                    .setMessage("Did you successfully add \"" + pendingEvent.getTitle() + "\" to your calendar?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        viewModel.signUpForEvent(pendingEvent);
                        viewModel.triggerCalendarThankYou(); // optional thank-you
                        pendingEvent = null;
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        Toast.makeText(requireContext(), "Sign-up cancelled.", Toast.LENGTH_SHORT).show();
                        pendingEvent = null;
                    })
                    .show();
        }
    }

    private void showCreateEventDialog() {
        DialogEditEventBinding dialogBinding = DialogEditEventBinding.inflate(LayoutInflater.from(requireContext()));
        dialogBinding.setEvent(new EventDTO()); // Empty event for creation

        new AlertDialog.Builder(requireContext())
                .setTitle("Create New Event")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Create", (dialog, which) -> {
                    EventDTO newEvent = dialogBinding.getEvent();
                    viewModel.createEvent(newEvent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
