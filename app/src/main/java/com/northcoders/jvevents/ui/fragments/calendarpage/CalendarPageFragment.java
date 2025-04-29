package com.northcoders.jvevents.ui.fragments.calendarpage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.FragmentCalendarPageBinding;

public class CalendarPageFragment extends Fragment {

    private static final String TAG = "CalendarPageFragment";

    private FragmentCalendarPageBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "Google sign-in successful. Account: " + account.getEmail());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.e(TAG, "Google sign in failed, statusCode=" + e.getStatusCode(), e);
                        Toast.makeText(getContext(), "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "Google sign in canceled or failed with resultCode=" + result.getResultCode());
                    Toast.makeText(getContext(), "Sign in canceled or failed", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Must be web client ID
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "No user signed in. Initiating sign-in...");
            signIn();
        } else {
            Log.d(TAG, "User already signed in: " + mAuth.getCurrentUser().getEmail());
        }

        if (binding.signInButton != null) {
            binding.signInButton.setOnClickListener(v -> {
                Log.d(TAG, "Sign-in button clicked.");
                signIn();
            });
        } else {
            Log.e(TAG, "signInButton is null. Check your XML layout.");
        }
    }

    private void signIn() {
        Log.d(TAG, "Launching Google Sign-In intent...");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Authenticating with Firebase using ID token...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Firebase auth success. User: " + user.getEmail());
                        Toast.makeText(getContext(), "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Firebase auth failed", task.getException());
                        Toast.makeText(getContext(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
