package com.example.hackio.twentythreeml;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.GsonBuilder;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.models.UserParams;

public class LoginActivity extends AppCompatActivity {

    private String responseString;

    private EditText nameText, phoneNumberText;
    private LinearLayout loginBtnLoader;
    public static final String HT_QUICK_START_SHARED_PREFS_KEY = "com.hypertrack.quickstart:SharedPreference";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getData();

        // Check if user is logged in
        if (getUser() != null) {
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            mainActivityIntent.putExtra("data", responseString);
            startActivity(mainActivityIntent);
            finish();
            return;
        }

        initUIViews();
    }

    private void getData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://fb5d869f.ngrok.io/getLocs";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i("Volley Response", response);
                        responseString = response;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // Call this method to initialize UI views and handle listeners for these views
    private void initUIViews() {
        // Initialize UserName Views
        nameText = (EditText) findViewById(R.id.login_name);

        // Initialize Password Views
        phoneNumberText = (EditText) findViewById(R.id.login_phone_number);

        // Initialize Login Btn Loader
        loginBtnLoader = (LinearLayout) findViewById(R.id.login_btn_loader);
    }

    /**
     * Call this method when user login button has been tapped.
     * Note that this method is linked with the layout file (content_login.xml)
     * using this button's layout's onClick attribute. So no need to invoke this
     * method or handle login button's click listener explicitly.
     *
     * @param view
     */
    public void onLoginButtonClick(View view) {
        // Check if Location Settings are enabled, if yes then attempt
        // UserLogin
        checkForLocationSettings();
    }

    /**
     * Call this method to check Location Settings before proceeding with UserLogin
     */
    private void checkForLocationSettings() {
        // Check for Location permission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
            return;
        }

        // Check for Location settings
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
        }

        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here i.e User Login in this case
        attemptUserLogin();
    }

    /**
     * Handle on Grant Location Permissions request accepted/denied result
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Location Permission denied error
                Toast.makeText(this, "Location Permission denied.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handle on Enable Location Services request accepted/denied result
     *
     * @param requestCode
     * @param resultCode
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings();

            } else {
                // Handle Enable Location Services request denied error
                Toast.makeText(this, "Please enable location",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Call this method to attempt user login. This method will create a UserId
     * on HyperTrack Server and configure the SDK using this generated UserId.
     */
    private void attemptUserLogin() {
        if (TextUtils.isEmpty(phoneNumberText.getText().toString())) {
            Toast.makeText(this, "Invalid",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show Login Button loader
        loginBtnLoader.setVisibility(View.VISIBLE);

        // Get User details, if specified
        final String name = nameText.getText().toString();
        final String phoneNumber = phoneNumberText.getText().toString();
        String UUID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        final String uniqueId = HTTextUtils.isEmpty(UUID) ? phoneNumber : UUID;

        /**
         * Get or Create a User for given uniqueId on HyperTrack Server here to
         * login your user & configure HyperTrack SDK with this generated
         * HyperTrack UserId.
         * OR
         * Implement your API call for User Login and get back a HyperTrack
         * UserId from your API Server to be configured in the HyperTrack SDK.
         */
        UserParams userParams = new UserParams().setName(name).setPhone(phoneNumber).setUniqueId(uniqueId);
        HyperTrack.getOrCreateUser(userParams, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse successResponse) {
                User user = (User) successResponse.getResponseObject();

                // Hide Login Button loader
                loginBtnLoader.setVisibility(View.GONE);
                // Handle createUser success here, if required
                // HyperTrack SDK auto-configures UserId on createUser API call,
                saveUser(user);
                // On UserLogin success
                onUserLoginSuccess();
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                // Hide Login Button loader
                loginBtnLoader.setVisibility(View.GONE);

                Toast.makeText(LoginActivity.this, "Error"
                                + " " + errorResponse.getErrorMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Call this method when user has successfully logged in
     */
    private void onUserLoginSuccess() {

        Intent mainActivityIntent = new Intent(LoginActivity.this,
                MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivityIntent);
        finish();
    }

    private void saveUser(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences(HT_QUICK_START_SHARED_PREFS_KEY,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user", new GsonBuilder().create().toJson(user));
        editor.apply();
    }

    private User getUser() {
        SharedPreferences sharedPreferences = getSharedPreferences(HT_QUICK_START_SHARED_PREFS_KEY,
                Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString("user", "null");
        if (HTTextUtils.isEmpty(jsonString)) {
            return null;
        }
        User user = null;
        try {

            user = new GsonBuilder().create().fromJson(jsonString, User.class);
        } catch (Exception e) {
            return null;
        }

        return user;
    }
}
