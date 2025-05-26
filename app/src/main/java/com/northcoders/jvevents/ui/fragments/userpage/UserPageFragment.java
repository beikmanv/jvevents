package com.northcoders.jvevents.ui.fragments.userpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.wallet.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserPageFragment extends Fragment {

    private static final String TAG = "UserPageFragment";
    private UserPageViewModel viewModel;
    private FragmentUserPageBinding binding;
    private GoogleSignInClient googleSignInClient;
    private List<AppUserDTO> userList = new ArrayList<>();
    private UserAdapter userAdapter;
    private EventAdapter eventAdapter;
    private PaymentsClient paymentsClient;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserPageViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST) // change to PRODUCTION later
                .build();

        paymentsClient = Wallet.getPaymentsClient(requireContext(), walletOptions);
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
        binding.searchViewUsers.setQueryHint("Click on a user for events he's attending");
        AutoCompleteTextView searchText = binding.searchViewUsers.findViewById(androidx.appcompat.R.id.search_src_text);
        int accessibleGray = ContextCompat.getColor(requireContext(), R.color.accessible_gray);
        searchText.setHintTextColor(accessibleGray);
        binding.searchViewUsers.setIconifiedByDefault(false);

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

        binding.recyclerViewEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventAdapter = new EventAdapter(
                new ArrayList<>(),
                true,
                new EventAdapter.OnEventActionListener() {
                    @Override
                    public void onItemClick(EventDTO event) {
                        Toast.makeText(requireContext(), "Clicked: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    }

                    public void onSeeAttendeesClick(EventDTO event) {}
                    public void onEditEventClick(EventDTO event) {}
                },
                R.layout.event_item_layout_simple // Use simplified layout
        );
        binding.recyclerViewEvents.setAdapter(eventAdapter);
    }

    private void loadUsers() {
        RetrofitInstance.getApiService().getAllUsers().enqueue(new Callback<List<AppUserDTO>>() {
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

        View nav = requireActivity().findViewById(R.id.bottomnavbar);
        if (nav != null) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) nav.getLayoutParams();
            if (params.getBehavior() instanceof com.google.android.material.behavior.HideBottomViewOnScrollBehavior) {
                params.setBehavior(null); // ⛔ Remove scroll behavior
                nav.setLayoutParams(params);
            }
            nav.setVisibility(View.VISIBLE);
        }

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

        viewModel.checkGooglePayAvailability(paymentsClient);

        viewModel.isGooglePayAvailable().observe(getViewLifecycleOwner(), isAvailable -> {
            binding.googlePayButton.setVisibility(isAvailable ? View.VISIBLE : View.GONE);
        });

        viewModel.getPaymentMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        binding.googlePayButton.setOnClickListener(v -> requestPayment());
    }

    public void handlePaymentSuccess(PaymentData paymentData) {
        try {
            JSONObject paymentMethodData = new JSONObject(paymentData.toJson())
                    .getJSONObject("paymentMethodData");
            String token = paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token");
            // Use token to process payment server-side if needed
            viewModel.setPaymentMessage("Thank you for your donation!");
        } catch (Exception e) {
            viewModel.setPaymentMessage("Payment processing failed.");
        }
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

        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    if (paymentData != null) {
                        try {
                            String rawTokenJson = new JSONObject(paymentData.toJson())
                                    .getJSONObject("paymentMethodData")
                                    .getJSONObject("tokenizationData")
                                    .getString("token");

                            Log.d("GooglePay", "📦 Raw token value: " + rawTokenJson);

                            String tokenId;
                            if (rawTokenJson.trim().startsWith("{")) {
                                // It's a JSON object
                                JSONObject tokenObject = new JSONObject(rawTokenJson);
                                tokenId = tokenObject.getString("id");
                            } else {
                                // It's a raw token string
                                tokenId = rawTokenJson;
                            }

                            Log.d("GooglePay", "✅ Final token ID sent to backend: " + tokenId);
                            viewModel.sendGooglePayToken(tokenId);
                            viewModel.handlePaymentSuccess(paymentData);


                        } catch (Exception e) {
                            Log.e("GooglePay", "❌ Token extraction failed", e);
                            viewModel.setPaymentMessage("Payment error occurred.");
                        }
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    Log.d("GooglePay", "Canceled");
                    break;
                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    Log.e("GooglePay", "Error: " + status);
                    break;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        View nav = requireActivity().findViewById(R.id.bottomnavbar);
        if (nav != null) nav.setVisibility(View.VISIBLE);
    }

    private void requestPayment() {
        try {
            JSONObject tokenizationSpec = new JSONObject()
                    .put("type", "PAYMENT_GATEWAY")
                    .put("parameters", new JSONObject()
                            .put("gateway", "stripe")
                            .put("stripe:version", "2022-11-15")
                            .put("stripe:publishableKey",
                                    "pk_test_51RQZtk4Tt7E1qBkG66karB0QHoFYLLyMiuDr2Cjw2YZoGSqfp76n0b7pvStTpXEbixKatZWqW6qZRqOpHG4iBgYH00BbgP3UMQ")
                    );

            JSONObject cardPaymentMethod = new JSONObject()
                    .put("type", "CARD")
                    .put("tokenizationSpecification", tokenizationSpec)
                    .put("parameters", new JSONObject()
                            .put("allowedCardNetworks", new JSONArray().put("VISA").put("MASTERCARD"))
                            .put("allowedAuthMethods", new JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
                            .put("billingAddressRequired", true)
                            .put("billingAddressParameters", new JSONObject().put("format", "FULL")));

            JSONObject transactionInfo = new JSONObject()
                    .put("totalPrice", "5.00")
                    .put("totalPriceStatus", "FINAL")
                    .put("currencyCode", "USD");

            JSONObject merchantInfo = new JSONObject()
                    .put("merchantName", "Your App Name")
                    .put("merchantOrigin", "http://10.0.2.2:8085/api/v1/");

            JSONObject paymentDataRequestJson = new JSONObject()
                    .put("apiVersion", 2)
                    .put("apiVersionMinor", 0)
                    .put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod))
                    .put("transactionInfo", transactionInfo)
                    .put("merchantInfo", merchantInfo);

            PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());

            Log.d("GooglePay", "🟡 Starting Google Pay payment...");

            AutoResolveHelper.resolveTask(
                    paymentsClient.loadPaymentData(request),
                    requireActivity(),
                    LOAD_PAYMENT_DATA_REQUEST_CODE
            );
        } catch (Exception e) {
            Log.e("GooglePay", "Payment JSON error", e);
        }
    }
}
