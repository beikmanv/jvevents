package com.northcoders.jvevents.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.UserItemLayoutBinding;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.ui.mainactivity.RecyclerViewInterface;

import java.util.List;

public class UserAdapter extends BaseAdapter<AppUserDTO> {

    public UserAdapter(Context context, List<AppUserDTO> userList, RecyclerViewInterface recyclerViewInterface) {
        super(context, userList, recyclerViewInterface);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserItemLayoutBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.user_item_layout,
                parent,
                false
        );
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    public static class UserViewHolder extends BaseViewHolder<AppUserDTO> {
        private final UserItemLayoutBinding binding;

        public UserViewHolder(UserItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void bind(AppUserDTO user) {
            binding.setUser(user);
            binding.executePendingBindings();
        }
    }
}
