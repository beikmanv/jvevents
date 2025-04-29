package com.northcoders.jvevents.util;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.northcoders.jvevents.R;

public class GoogleSignInHelper {

    private static final int RC_SIGN_IN = 9001;
    private final GoogleSignInClient googleSignInClient;

    public GoogleSignInHelper(Activity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id)) // 🔑 Google Client ID
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void signIn(Activity activity) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static int getRequestCode() {
        return RC_SIGN_IN;
    }

    public static String getIdToken(Intent data) throws ApiException {
        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
        return account.getIdToken();
    }

    public void signOut() {
        googleSignInClient.signOut();
    }
}
