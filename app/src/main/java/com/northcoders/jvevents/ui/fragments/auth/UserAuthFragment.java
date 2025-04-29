package com.northcoders.jvevents.ui.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.api.ApiException;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.util.GoogleSignInHelper;
import com.northcoders.jvevents.repository.AuthRepository;
import com.northcoders.jvevents.databinding.FragmentUserAuthBinding;

public class UserAuthFragment extends Fragment {

    private GoogleSignInHelper googleSignInHelper;
    private FragmentUserAuthBinding binding;

    public UserAuthFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserAuthBinding.inflate(inflater, container, false);
        googleSignInHelper = new GoogleSignInHelper(requireActivity());

        binding.signInButton.setOnClickListener(view -> googleSignInHelper.signIn(requireActivity()));

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GoogleSignInHelper.getRequestCode() && data != null) {
            try {
                String idToken = GoogleSignInHelper.getIdToken(data);
                AuthRepository.authenticateWithBackend(idToken, success -> {
                    if (success) {
                        Log.d("Auth", "Login success!");
                        // Navigate to protected fragment
                    } else {
                        Log.e("Auth", "Backend login failed");
                    }
                });
            } catch (ApiException e) {
                Log.e("Auth", "Google sign in failed", e);
            }
        }
    }
}
