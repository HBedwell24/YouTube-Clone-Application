package com.example.youtubeapiintegration.Activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.youtubeapiintegration.R;
import com.google.android.material.button.MaterialButton;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static com.example.youtubeapiintegration.ProfileApplication.LOG_TAG;

public class AuthorizationActivity extends AppCompatActivity {

    private static final String SHARED_PREFERENCES_NAME = "AuthStatePreference";
    private static final String AUTH_STATE = "AUTH_STATE";
    private static final String USED_INTENT = "USED_INTENT";

    // state
    AuthState mAuthState;

    // views
    MaterialButton mAuthorize;
    MaterialButton mGuest;

    String mLoginHint;
    private BroadcastReceiver mRestrictionsReceiver;
    private final static String LOGIN_HINT = "login_hint";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        mAuthorize = (MaterialButton) findViewById(R.id.bAuthorize);
        mAuthorize.setOnClickListener(new AuthorizeListener(this));

        mGuest = (MaterialButton) findViewById(R.id.bGuest);
        mGuest.setOnClickListener(new MaterialButton.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(AuthorizationActivity.this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });


        enablePostAuthorizationFlows();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
    }

    private void checkIntent(@Nullable Intent intent) {

        if (intent != null) {
            String action = intent.getAction();
            switch (action) {
                case "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE":
                    if (!intent.hasExtra(USED_INTENT)) {
                        handleAuthorizationResponse(intent);
                        intent.putExtra(USED_INTENT, true);
                    }
                    break;

                default:
            }
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        // Retrieve app restrictions and take appropriate action
        getAppRestrictions();

        // Register a receiver for app restrictions changed broadcast
        registerRestrictionsReceiver();
    }

    @Override
    protected void onStop() {

        super.onStop();

        // Unregister receiver for app restrictions changed broadcast
        unregisterReceiver(mRestrictionsReceiver);
    }

    @Override
    protected void onStart() {

        super.onStart();
        checkIntent(getIntent());
        registerRestrictionsReceiver();
    }

    private void enablePostAuthorizationFlows() {

        mAuthState = restoreAuthState();

        if (mAuthState != null && mAuthState.isAuthorized()) {

            Intent intent = new Intent(AuthorizationActivity.this, AuthenticationActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Exchanges the code, for the {@link TokenResponse}.
     *
     * @param intent represents the {@link Intent} from the Custom Tabs or the System Browser.
     */
    private void handleAuthorizationResponse(@NonNull Intent intent) {

        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);

        if (response != null) {
            Log.i(LOG_TAG, String.format("Handled Authorization Response %s ", authState.toJsonString()));
            AuthorizationService service = new AuthorizationService(this);
            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {

                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {

                    if (exception != null) {
                        Log.w(LOG_TAG, "Token Exchange failed", exception);
                    } else {

                        if (tokenResponse != null) {
                            authState.update(tokenResponse, exception);
                            persistAuthState(authState);
                            Log.i(LOG_TAG, String.format("Token Response [ Access Token: %s, ID Token: %s ]", tokenResponse.accessToken, tokenResponse.idToken));

                            Intent intent = new Intent(AuthorizationActivity.this, AuthenticationActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    private void persistAuthState(@NonNull AuthState authState) {

        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putString(AUTH_STATE, authState.toJsonString())
                .commit();
    }

    private void clearAuthState() {

        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(AUTH_STATE)
                .apply();
    }

    @Nullable
    private AuthState restoreAuthState() {

        String jsonString = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(AUTH_STATE, null);

        if (!TextUtils.isEmpty(jsonString)) {

            try {
                return AuthState.fromJson(jsonString);
            }

            catch (JSONException jsonException) {
                // should never happen
            }
        }
        return null;
    }

    /**
     * Kicks off the authorization flow.
     */
    public static class AuthorizeListener implements Button.OnClickListener {

        private final AuthorizationActivity mMainActivity;

        public AuthorizeListener(@NonNull AuthorizationActivity mainActivity) {
            mMainActivity = mainActivity;
        }

        @Override
        public void onClick(View view) {

            // code from the step 'Create the Authorization Request',
            // and the step 'Perform the Authorization Request' goes here.
            AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                    Uri.parse("https://accounts.google.com/o/oauth2/v2/auth") /* auth endpoint */,
                    Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token endpoint */
            );

            String clientId = "";
            Uri redirectUri = Uri.parse("com.google.codelabs.appauth:/oauth2callback");
            AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                    serviceConfiguration,
                    clientId,
                    AuthorizationRequest.RESPONSE_TYPE_CODE,
                    redirectUri
            );
            builder.setScopes("profile");

            if(mMainActivity.getLoginHint() != null) {

                Map loginHintMap = new HashMap<String, String>();
                loginHintMap.put(LOGIN_HINT,mMainActivity.getLoginHint());
                builder.setAdditionalParameters(loginHintMap);

                Log.i(LOG_TAG, String.format("login_hint: %s", mMainActivity.getLoginHint()));

                Log.i(LOG_TAG, String.format("login_hint: %s", mMainActivity.getLoginHint()));
            }

            AuthorizationRequest request = builder.build();

            AuthorizationService authorizationService = new AuthorizationService(view.getContext());
            String action = "com.google.codelabs.appauth.HANDLE_AUTHORIZATION_RESPONSE";
            Intent postAuthorizationIntent = new Intent(action);
            PendingIntent pendingIntent = PendingIntent.getActivity(view.getContext(), request.hashCode(), postAuthorizationIntent, 0);
            authorizationService.performAuthorizationRequest(request, pendingIntent);
        }
    }

    private void getAppRestrictions() {

        RestrictionsManager restrictionsManager = (RestrictionsManager) this.getSystemService(Context.RESTRICTIONS_SERVICE);
        Bundle appRestrictions = restrictionsManager.getApplicationRestrictions();

        if(!appRestrictions.isEmpty()) {

            if(appRestrictions.getBoolean(UserManager.KEY_RESTRICTIONS_PENDING) != true) {
                mLoginHint = appRestrictions.getString(LOGIN_HINT);
            }

            else {
                Toast.makeText(this,R.string.restrictions_pending_block_user, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void registerRestrictionsReceiver(){
        IntentFilter restrictionsFilter = new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);

        mRestrictionsReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                getAppRestrictions();
            }
        };
        registerReceiver(mRestrictionsReceiver, restrictionsFilter);
    }

    public String getLoginHint() {
        return mLoginHint;
    }
}



