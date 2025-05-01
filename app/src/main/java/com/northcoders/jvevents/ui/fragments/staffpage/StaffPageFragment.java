package com.northcoders.jvevents.ui.fragments.staffpage;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.northcoders.jvevents.databinding.FragmentStaffPageBinding;
import com.northcoders.jvevents.service.ApiService;
import com.northcoders.jvevents.service.RetrofitInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StaffPageFragment extends Fragment {

    private FragmentStaffPageBinding binding;

    public StaffPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStaffPageBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}
