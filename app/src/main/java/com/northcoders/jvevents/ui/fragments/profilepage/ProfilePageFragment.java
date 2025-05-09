package com.northcoders.jvevents.ui.fragments.profilepage;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.FragmentProfilePageBinding;

public class ProfilePageFragment extends Fragment {

    private static final String TAG = "ProfilePageFragment";
    private ProfilePageViewModel viewModel;
    private FragmentProfilePageBinding binding;
    private GoogleSignInClient googleSignInClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfilePageViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfilePageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe User Data
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.statusText.setText("Hello, " + user.getDisplayName());
                binding.signOutButton.setVisibility(View.VISIBLE);
                binding.signInButton.setVisibility(View.GONE);
            } else {
                binding.statusText.setText("Not signed in");
                binding.signOutButton.setVisibility(View.GONE);
                binding.signInButton.setVisibility(View.VISIBLE);
            }
        });

        // Set Click Listeners
        binding.signInButton.setOnClickListener(v -> initiateSignIn());
        binding.signOutButton.setOnClickListener(v -> viewModel.signOut());
    }

    private void initiateSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9001 && data != null) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e(TAG, "❌ Google sign-in failed", e);
                Toast.makeText(getContext(), "Google sign-in failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Use Firebase Auth directly to get an ID Token
    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Log.e(TAG, "❌ Google ID Token is null");
            Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true).addOnCompleteListener(idTokenTask -> {
                                if (idTokenTask.isSuccessful()) {
                                    String firebaseIdToken = idTokenTask.getResult().getToken();
                                    if (firebaseIdToken != null) {
                                        viewModel.sendIdTokenToBackend(firebaseIdToken);
                                    } else {
                                        Log.e(TAG, "❌ Firebase ID Token is null");
                                    }
                                } else {
                                    Log.e(TAG, "❌ Failed to get Firebase ID Token", idTokenTask.getException());
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "❌ Firebase Authentication failed", task.getException());
                        Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
