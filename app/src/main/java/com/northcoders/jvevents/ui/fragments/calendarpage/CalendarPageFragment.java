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
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.FragmentCalendarPageBinding;
import java.io.IOException;
import okhttp3.*;



public class CalendarPageFragment extends Fragment {

    private static final String TAG = "CalendarPageFragment";
    private static final String BACKEND_URL = "http://10.0.2.2:8085/api/v1/auth/google";

    private FragmentCalendarPageBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "Google sign-in successful: " + account.getEmail());
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.e(TAG, "Google sign-in failed: " + e.getStatusCode(), e);
                        Toast.makeText(getContext(), "Google sign-in failed.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.w(TAG, "Google sign-in canceled or failed.");
                    Toast.makeText(getContext(), "Sign-in canceled or failed.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
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

        // Set up sign-out button if present
        if (binding.signOutButton != null) {
            binding.signOutButton.setOnClickListener(v -> signOut());
        }

        // Trigger sign-in if not already authenticated
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No user signed in. Initiating sign-in...");
            signIn();
        } else {
            Log.d(TAG, "User already signed in: " + user.getEmail());
            sendUserIdTokenToBackend(user);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "User signed out");
            // Optionally refresh the UI
            requireActivity().recreate();
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Firebase auth successful: " + user.getEmail());
                        Toast.makeText(getContext(), "Welcome, " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        sendUserIdTokenToBackend(user);
                    } else {
                        Log.e(TAG, "Firebase auth failed", task.getException());
                        Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendUserIdTokenToBackend(FirebaseUser user) {
        user.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String idToken = task.getResult().getToken();
                sendIdTokenToBackend(idToken);
            } else {
                Log.e(TAG, "Failed to get ID token", task.getException());
            }
        });
    }

    private void sendIdTokenToBackend(String idToken) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("idToken", idToken)
                .build();

        Request request = new Request.Builder()
                .url(BACKEND_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Backend request failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Backend response: " + response.code());
                } else {
                    Log.e(TAG, "Backend error. Code: " + response.code());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
