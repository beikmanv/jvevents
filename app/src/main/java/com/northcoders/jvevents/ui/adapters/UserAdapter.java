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
    private final List<AppUserDTO> userList;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClicked(AppUserDTO user);
    }

    public UserAdapter(List<AppUserDTO> userList, OnUserClickListener listener) {
        this.userList = userList != null ? userList : new ArrayList<>();
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
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUsers(List<AppUserDTO> newUsers) {
        userList.clear();
        userList.addAll(newUsers);
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
            binding.getRoot().setOnClickListener(v -> {
                listener.onUserClicked(user);
            });
        }
    }
}
