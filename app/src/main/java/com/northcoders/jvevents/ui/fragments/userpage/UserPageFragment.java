package com.northcoders.jvevents.ui.fragments.userpage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.FragmentUserPageBinding;

public class UserPageFragment extends Fragment {

    private static final String TAG = "UserPageFragment";
    private UserPageViewModel viewModel;
    private FragmentUserPageBinding binding;
    private GoogleSignInClient googleSignInClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserPageViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe User Data
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            updateUI(user);
        });

        // Observe Authentication Status
        viewModel.getAuthStatus().observe(getViewLifecycleOwner(), isAuthenticated -> {
            if (!isAuthenticated) {
                Toast.makeText(getContext(), "You are logged out.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set Click Listeners
        binding.signInButton.setOnClickListener(v -> initiateSignIn());
        binding.signOutButton.setOnClickListener(v -> viewModel.signOut());
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            binding.statusText.setText("Hello, " + user.getDisplayName());
            binding.signOutButton.setVisibility(View.VISIBLE);
            binding.signInButton.setVisibility(View.GONE);
        } else {
            binding.statusText.setText("Not signed in");
            binding.signOutButton.setVisibility(View.GONE);
            binding.signInButton.setVisibility(View.VISIBLE);
        }
    }

    private void initiateSignIn() {
        // Clear the cached Google Account and force Account Picker
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            googleSignInClient.revokeAccess().addOnCompleteListener(revokeTask -> {
                // Now always show Google Account Picker
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 9001);
            });
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001 && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    viewModel.signInWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e(TAG, "❌ Google sign-in failed", e);
                Toast.makeText(getContext(), "Google sign-in failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
