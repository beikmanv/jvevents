package com.northcoders.jvevents.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.northcoders.jvevents.BR;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.EventItemLayoutBinding;
import com.northcoders.jvevents.model.EventDTO;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<EventDTO> eventList;
    private boolean isStaff;
    private final OnEventActionListener actionListener;
    private final int layoutId;

    public EventAdapter(List<EventDTO> eventList, boolean isStaff, OnEventActionListener actionListener, int layoutId) {
        this.eventList = eventList != null ? eventList : new ArrayList<>();
        this.isStaff = isStaff;
        this.actionListener = actionListener;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                layoutId,
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

    public void updateEvents(List<EventDTO> newEvents) {
        eventList.clear();
        eventList.addAll(newEvents);
        notifyDataSetChanged();
    }

    public void setIsStaff(boolean isStaff) {
        this.isStaff = isStaff;
        notifyDataSetChanged();
    }

    public interface OnEventActionListener {
        void onItemClick(EventDTO event);
        void onSeeAttendeesClick(EventDTO event);
        void onEditEventClick(EventDTO event);
    }

    public interface OnEventActionListenerExtended extends OnEventActionListener {
        void onDeleteEventClick(EventDTO event);
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        public EventViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(EventDTO event) {
            binding.setVariable(BR.event, event);
            binding.executePendingBindings();

            binding.getRoot().setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onItemClick(event);
                }
            });

            if (binding instanceof EventItemLayoutBinding) {
                EventItemLayoutBinding fullBinding = (EventItemLayoutBinding) binding;

                fullBinding.btnSeeAttendees.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onSeeAttendeesClick(event);
                    }
                });

                if (isStaff) {
                    fullBinding.btnSeeAttendees.setVisibility(View.VISIBLE);
                    fullBinding.btnEditEvent.setVisibility(View.VISIBLE);
                    fullBinding.btnDeleteEvent.setVisibility(View.VISIBLE);

                    fullBinding.btnSeeAttendees.setOnClickListener(v -> {
                        if (actionListener != null) {
                            actionListener.onSeeAttendeesClick(event);
                        }
                    });

                    fullBinding.btnEditEvent.setOnClickListener(v -> {
                        if (actionListener != null) {
                            actionListener.onEditEventClick(event);
                        }
                    });

                    fullBinding.btnDeleteEvent.setOnClickListener(v -> {
                        if (actionListener instanceof EventAdapter.OnEventActionListenerExtended) {
                            ((OnEventActionListenerExtended) actionListener).onDeleteEventClick(event);
                        }
                    });

                } else {
                    fullBinding.btnEditEvent.setVisibility(View.GONE);
                    fullBinding.btnDeleteEvent.setVisibility(View.GONE);
                }
            }

        }
    }

}

