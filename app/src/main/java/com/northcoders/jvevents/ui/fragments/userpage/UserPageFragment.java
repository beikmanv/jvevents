package com.northcoders.jvevents.ui.fragments.userpage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.RetrofitInstance;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPageFragment extends Fragment {

    private static final String TAG = "UserPageFragment";
    private UserPageViewModel viewModel;
    private FragmentUserPageBinding binding;
    private GoogleSignInClient googleSignInClient;
    private List<AppUserDTO> userList = new ArrayList<>();

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
        setupUserDropdown();
        return binding.getRoot();
    }

    private void setupUserDropdown() {
        FirebaseUser currentUser = viewModel.getUserLiveData().getValue(); // Make sure the user is authenticated

        if (currentUser == null) {
            Log.e(TAG, "❌ User is not authenticated.");
            Toast.makeText(requireContext(), "You are not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch JWT Token
        currentUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult().getToken();
                if (token == null) {
                    Log.e(TAG, "❌ Failed to get Firebase ID Token.");
                    Toast.makeText(requireContext(), "Authentication error.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Make API Call with Token
                RetrofitInstance.getApiService().getAllUsers("Bearer " + token).enqueue(new Callback<List<AppUserDTO>>() {
                    @Override
                    public void onResponse(Call<List<AppUserDTO>> call, Response<List<AppUserDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            userList = response.body();
                            List<String> userNames = new ArrayList<>();
                            for (AppUserDTO user : userList) {
                                userNames.add(user.getUsername());
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_spinner_item, userNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            binding.spinnerUsers.setAdapter(adapter);

                            binding.spinnerUsers.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                    AppUserDTO selectedUser = userList.get(position);
                                    fetchUserEvents(selectedUser.getId());
                                }

                                @Override
                                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                            });

                            Log.d(TAG, "✅ User dropdown populated: " + userNames);
                        } else {
                            Log.e(TAG, "❌ Failed to fetch users: " + response.message());
                            Toast.makeText(requireContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AppUserDTO>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error fetching users", t);
                        Toast.makeText(requireContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Log.e(TAG, "❌ Failed to get Firebase ID Token: " + task.getException());
                Toast.makeText(requireContext(), "Authentication error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserEvents(Long userId) {
        RetrofitInstance.getApiService().getEventsForUser(userId).enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayEvents(response.body());
                } else {
                    displayEvents(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayEvents(List<EventDTO> events) {
        binding.eventListContainer.removeAllViews();
        for (EventDTO event : events) {
            TextView eventText = new TextView(requireContext());
            eventText.setText("- " + event.getTitle() + " (" + event.getEventDate() + ")");
            binding.eventListContainer.addView(eventText);
        }

        if (events.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No events assigned.");
            binding.eventListContainer.addView(emptyText);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe User Data for Sign-In State
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), this::updateUI);

        // Observe Authentication Status
        viewModel.getAuthStatus().observe(getViewLifecycleOwner(), isAuthenticated -> {
            if (!isAuthenticated) {
                Toast.makeText(getContext(), "You are logged out.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set Click Listeners for Sign In and Sign Out
        binding.signInButton.setOnClickListener(v -> initiateSignIn());
        binding.signOutButton.setOnClickListener(v -> {
            viewModel.signOut();
        });

        // Observe and Load Users
        viewModel.getUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                setupUserDropdown();
            } else {
                Toast.makeText(requireContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });

        // Observe and Display Events
        viewModel.getEventsLiveData().observe(getViewLifecycleOwner(), events -> {
            displayEvents(events);
        });

        // Automatically fetch users at start
        viewModel.fetchUsers();
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
