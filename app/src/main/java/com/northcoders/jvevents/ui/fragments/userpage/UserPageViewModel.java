package com.northcoders.jvevents.ui.fragments.userpage;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.firebase.auth.FirebaseUser;
import com.northcoders.jvevents.model.AppUserDTO;
import com.northcoders.jvevents.model.EventDTO;
import com.northcoders.jvevents.repository.PaymentRepository;
import com.northcoders.jvevents.repository.UserRepository;

import org.json.JSONObject;

import java.util.List;

public class UserPageViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<AppUserDTO>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<EventDTO>> eventsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> googlePayAvailable = new MutableLiveData<>();
    private final MutableLiveData<String> paymentMessage = new MutableLiveData<>();
    public LiveData<Boolean> isGooglePayAvailable() { return googlePayAvailable; }
    public LiveData<String> getPaymentMessage() { return paymentMessage; }
    private final PaymentRepository paymentRepository = new PaymentRepository();

    public UserPageViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userRepository.getUserLiveData();
    }

    public LiveData<Boolean> getAuthStatus() {
        return userRepository.getAuthStatus();
    }

    public void signInWithGoogle(String idToken) {
        userRepository.signInWithGoogle(idToken);
    }

    public void signOut() {
        userRepository.signOut();
    }

    public void fetchUsers() {
        userRepository.fetchUsers(usersLiveData);
    }

    public void fetchUserEvents(long userId) {
        userRepository.fetchUserEvents(userId, eventsLiveData);
    }

    public LiveData<List<AppUserDTO>> getUsersLiveData() {
        return usersLiveData;
    }

    public LiveData<List<EventDTO>> getEventsLiveData() {
        return eventsLiveData;
    }

    public void checkGooglePayAvailability(PaymentsClient client) {
        userRepository.checkGooglePayAvailability(client, googlePayAvailable);
    }

    public void handlePaymentSuccess(PaymentData paymentData) {
        try {
            JSONObject paymentMethodData = new JSONObject(paymentData.toJson())
                    .getJSONObject("paymentMethodData");
            String token = paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token");
            Log.d("UserVM", "Received token: " + token.replaceAll("\\s+", " "));
            paymentMessage.setValue("Thank you for your donation!");
        } catch (Exception e) {
            Log.e("UserVM", "Payment parse error", e);
            paymentMessage.setValue("Payment failed.");
        }
    }

    public void setPaymentMessage(String message) {
        paymentMessage.setValue(message);
    }

    public void sendGooglePayToken(String token) {
        Log.d(TAG, "📡 sendGooglePayToken() CALLED with token: " + token);
        paymentRepository.sendGooglePayToken(token, paymentMessage);
    }
}
