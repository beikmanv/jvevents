package com.northcoders.jvevents.ui.fragments.userpage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.northcoders.jvevents.R;
import com.northcoders.jvevents.databinding.FragmentUserPageBinding;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.service.RetrofitInstance;
import com.northcoders.jvevents.ui.adapters.EventAdapter;
import com.northcoders.jvevents.ui.adapters.UserAdapter;

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
    private UserAdapter userAdapter;
    private EventAdapter eventAdapter;

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
        setupRecyclerViews();
        loadUsers();
        return binding.getRoot();
    }

    private void setupRecyclerViews() {
        // Users
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        userAdapter = new UserAdapter(new ArrayList<>(), user -> fetchUserEvents(user.getId()));
        binding.recyclerViewUsers.setAdapter(userAdapter);

        // Search
        binding.searchViewUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.filter(newText);
                return true;
            }
        });

        // Events
        binding.recyclerViewEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventAdapter = new EventAdapter(new ArrayList<>(), true, new EventAdapter.OnEventActionListener() {
            @Override
            public void onItemClick(EventDTO event) {
                Toast.makeText(requireContext(), "Clicked: " + event.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSeeAttendeesClick(EventDTO event) {}
            @Override
            public void onEditEventClick(EventDTO event) {}
        });
        binding.recyclerViewEvents.setAdapter(eventAdapter);
    }

    private void loadUsers() {
        RetrofitInstance.getApiService().getAllUsers("").enqueue(new Callback<List<AppUserDTO>>() {
            @Override
            public void onResponse(Call<List<AppUserDTO>> call, Response<List<AppUserDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList = response.body();
                    userAdapter.updateUsers(userList);
                } else {
                    Toast.makeText(requireContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AppUserDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Failed to load users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserEvents(Long userId) {
        RetrofitInstance.getApiService().getEventsForUser(userId).enqueue(new Callback<List<EventDTO>>() {
            @Override
            public void onResponse(Call<List<EventDTO>> call, Response<List<EventDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventAdapter.updateEvents(response.body());
                } else {
                    eventAdapter.updateEvents(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<EventDTO>> call, Throwable t) {
                Toast.makeText(requireContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set Click Listeners for Sign In and Sign Out
        binding.signInButton.setOnClickListener(v -> initiateSignIn());
        binding.signOutButton.setOnClickListener(v -> {
            viewModel.signOut();
            binding.statusText.setText("Not signed in");
        });

        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            Log.d(TAG, "🔄 FirebaseUser changed: " + (user != null ? user.getEmail() : "null"));
            updateUI(user);
        });

        // Automatically fetch users at start
        loadUsers();
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
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            googleSignInClient.revokeAccess().addOnCompleteListener(revokeTask -> {
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
