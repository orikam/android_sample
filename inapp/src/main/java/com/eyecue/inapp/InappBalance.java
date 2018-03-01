package com.eyecue.inapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;

/**
 * Created by orikam on 27/01/2018.
 */

public class InappBalance {
    private static InappBalance instance = null;
    private GoogleSignInClient client;
    private Context ctx;
    Task<GoogleSignInAccount> signInTask;

    protected InappBalance() {
        client = buildGoogleSignInClient();
        Intent signin = client.getSignInIntent();
        signInTask = GoogleSignIn.getSignedInAccountFromIntent(signin);
        Log.d("oooOri","oooOri inapp balance " + signInTask);
    }

    public static InappBalance getInstance(Context context) {
        if(instance == null) {
            instance = new InappBalance();
            instance.ctx = context;


        }
        return instance;
    }

    private GoogleSignInClient buildGoogleSignInClient() {
//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(Drive.SCOPE_FILE)
//                        .build();
//        return GoogleSignIn.getClient(ctx, signInOptions);
        return  null;
    }

}
