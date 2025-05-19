package com.northcoders.jvevents.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.northcoders.jvevents.databinding.ItemAttendeeBinding;
import com.northcoders.jvevents.model.AppUserDTO;
import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeViewHolder> {

    private final List<AppUserDTO> attendees;

    public AttendeeAdapter(List<AppUserDTO> attendees) {
        this.attendees = attendees;
    }

    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAttendeeBinding binding = ItemAttendeeBinding.inflate(inflater, parent, false);
        return new AttendeeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        AppUserDTO user = attendees.get(position);
        holder.binding.attendeeName.setText(user.getUsername());
        holder.binding.attendeeEmail.setText(user.getEmail());
    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }

    static class AttendeeViewHolder extends RecyclerView.ViewHolder {
        ItemAttendeeBinding binding;

        public AttendeeViewHolder(@NonNull ItemAttendeeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
