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

        // 🔥 Automatically check cookies when the fragment opens
        ApiService service = RetrofitInstance.getService();
        service.debugCookies().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("CookiesDebug", "🍪 Cookies from Android: \n" + response.body());
                } else {
                    Log.e("CookiesDebug", "❌ Failed to fetch cookies. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("CookiesDebug", "💥 Cookie debug failed", t);
            }
        });

        return binding.getRoot();
    }
}
