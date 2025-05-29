package com.northcoders.jvevents.util;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.northcoders.jvevents.R;

import com.northcoders.jvevents.BuildConfig;

public class GoogleSignInHelper {

    private static final int RC_SIGN_IN = 9001;
    private final GoogleSignInClient googleSignInClient;

    public GoogleSignInHelper(Activity activity) {
        if (android.os.Build.FINGERPRINT.contains("generic")) {
            android.util.Log.w("GoogleSignInHelper", "⚠️ Emulator detected, skipping GoogleSignInClient init");
            googleSignInClient = null;
            return;
        }

        String clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID;
        if (clientId == null || clientId.isEmpty()) {
            android.util.Log.e("GoogleSignInHelper", "❌ GOOGLE_WEB_CLIENT_ID is not defined");
            googleSignInClient = null;
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void signIn(Activity activity) {
        if (googleSignInClient == null) {
            android.util.Log.e("GoogleSignInHelper", "🚫 signIn() called but client is null");
            return;
        }

        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static int getRequestCode() {
        return RC_SIGN_IN;
    }

    public static String getIdToken(Intent data) throws ApiException {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            return account != null ? account.getIdToken() : null;
        } catch (Exception e) {
            android.util.Log.e("GoogleSignInHelper", "❌ getIdToken() failed", e);
            return null;
        }
    }

    public void signOut() {
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        } else {
            android.util.Log.w("GoogleSignInHelper", "signOut() called but client is null");
        }
    }
}

