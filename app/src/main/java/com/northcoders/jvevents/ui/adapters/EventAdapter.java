package com.northcoders.jvevents.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.EventItemLayoutBinding;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.ui.mainactivity.RecyclerViewInterface;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final Context context;
    private final List<EventDTO> eventList;
    private final RecyclerViewInterface recyclerViewInterface;
    private final boolean isStaff;
    private final EventItemListener eventItemListener;

    public EventAdapter(Context context, List<EventDTO> eventList, RecyclerViewInterface recyclerViewInterface, boolean isStaff, EventItemListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.recyclerViewInterface = recyclerViewInterface;
        this.isStaff = isStaff;
        this.eventItemListener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EventItemLayoutBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.event_item_layout,
                parent,
                false
        );
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(eventList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public List<EventDTO> getEvents() {
        return eventList;
    }

    public interface EventItemListener {
        void onSeeAttendeesClick(EventDTO event);
        void onEditEventClick(EventDTO event);
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private final EventItemLayoutBinding binding;

        public EventViewHolder(EventItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EventDTO event) {
            binding.setEvent(event);

            if (isStaff) {
                binding.btnEditEvent.setVisibility(View.VISIBLE);
                binding.btnEditEvent.setOnClickListener(v -> {
                    if (eventItemListener != null) {
                        eventItemListener.onEditEventClick(event);
                    }
                });
            } else {
                binding.btnEditEvent.setVisibility(View.GONE);
            }

            binding.btnSeeAttendees.setOnClickListener(v -> {
                if (eventItemListener != null) {
                    eventItemListener.onSeeAttendeesClick(event);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (recyclerViewInterface != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    recyclerViewInterface.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
