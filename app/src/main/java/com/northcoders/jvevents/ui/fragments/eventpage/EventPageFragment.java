package com.northcoders.jvevents.ui.fragments.eventpage;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.FragmentEventPageBinding;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.ui.adapters.EventAdapter;
import com.northcoders.jvevents.ui.adapters.UserAdapter;
import com.northcoders.jvevents.ui.mainactivity.RecyclerViewInterface;

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
    public void onItemClick(int position) {}
}