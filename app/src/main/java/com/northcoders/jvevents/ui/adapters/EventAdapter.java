package com.northcoders.jvevents.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.EventItemLayoutBinding;
import com.northcoders.jvevents.model.EventDTO;

import java.util.ArrayList;
import java.util.List;

// We only need adapters for RecyclerView
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<EventDTO> eventList;
    private boolean isStaff;
    private final OnEventActionListener actionListener;

    public EventAdapter(List<EventDTO> eventList, boolean isStaff, OnEventActionListener actionListener) {
        this.eventList = eventList != null ? eventList : new ArrayList<>();
        this.isStaff = isStaff;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    // Uses DataBindingUtil to bind the event_item_layout XML file
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EventItemLayoutBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.event_item_layout,
                parent,
                false
        );
        return new EventViewHolder(binding);
    }

    // Called to display the data at the specified position
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(eventList.get(position));
    }

    @Override
    // Tells the RecyclerView how many items it should display
    public int getItemCount() {
        return eventList.size();
    }

    // This is used when your list of events changes
    public void updateEvents(List<EventDTO> events) {
        this.eventList.clear();
        this.eventList.addAll(events);
        notifyDataSetChanged();
    }

    public void setIsStaff(boolean isStaff) {
        this.isStaff = isStaff;
        notifyDataSetChanged();
    }

    // This interface allows the Fragment to handle click events on each list item
    public interface OnEventActionListener {
        void onItemClick(EventDTO event);
        void onSeeAttendeesClick(EventDTO event);
        void onEditEventClick(EventDTO event);
    }

    // A ViewHolder is a design pattern used in RecyclerView to make scrolling smooth and efficient.
    // It "holds" or "caches" the views for each list item, so they don't have to be created every time.
    class EventViewHolder extends RecyclerView.ViewHolder {
        private final EventItemLayoutBinding binding;

        public EventViewHolder(EventItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EventDTO event) {
            binding.setEvent(event);
            binding.executePendingBindings();

            binding.getRoot().setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onItemClick(event);
                }
            });

            binding.btnSeeAttendees.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onSeeAttendeesClick(event);
                }
            });

            if (isStaff) {
                binding.btnEditEvent.setVisibility(View.VISIBLE);
                binding.btnEditEvent.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onEditEventClick(event);
                    }
                });
            } else {
                binding.btnEditEvent.setVisibility(View.GONE);
            }
        }
    }
}
