package com.northcoders.jvevents.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.northcoders.jvevents.databinding.UserItemLayoutBinding;
import com.northcoders.jvevents.model.AppUserDTO;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<AppUserDTO> originalList;
    private List<AppUserDTO> filteredList;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClicked(AppUserDTO user);
    }

    public UserAdapter(List<AppUserDTO> userList, OnUserClickListener listener) {
        this.originalList = new ArrayList<>(userList);
        this.filteredList = new ArrayList<>(userList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UserItemLayoutBinding binding = UserItemLayoutBinding.inflate(inflater, parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(filteredList.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateUsers(List<AppUserDTO> newUsers) {
        if (newUsers == null) {
            newUsers = new ArrayList<>(); // fallback to empty list
        }
        this.originalList = new ArrayList<>(newUsers);
        this.filteredList = new ArrayList<>(newUsers);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredList = new ArrayList<>(originalList);
        } else {
            filteredList = new ArrayList<>();
            for (AppUserDTO user : originalList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final UserItemLayoutBinding binding;

        public UserViewHolder(UserItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AppUserDTO user) {
            binding.setUser(user);
            binding.executePendingBindings();
            binding.getRoot().setOnClickListener(v -> listener.onUserClicked(user));
        }
    }
}
