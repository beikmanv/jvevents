package com.northcoders.jvevents.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.EventItemLayoutBinding;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.ui.mainactivity.RecyclerViewInterface;

import java.util.List;

public class EventAdapter extends BaseAdapter<EventDTO> {

    public EventAdapter(Context context, List<EventDTO> eventList, RecyclerViewInterface recyclerViewInterface) {
        super(context, eventList, recyclerViewInterface);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        EventItemLayoutBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.event_item_layout,
                parent,
                false
        );
        return new EventViewHolder(binding);
    }

    public static class EventViewHolder extends BaseViewHolder<EventDTO> {
        private final EventItemLayoutBinding binding;

        public EventViewHolder(EventItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bind(EventDTO event) {
            binding.setEvent(event);
        }
    }
}

