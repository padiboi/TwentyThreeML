package com.example.hackio.twentythreeml;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonObject;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.GeoJSONLocation;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener {

    //String responseObject;
    MapFragment mapFragment;

    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    ArrayList<LatLng> locs = new ArrayList<>();
    ArrayList<LatLng> marks = new ArrayList<>();
    ArrayList<String> questions = new ArrayList<>();
    ArrayList<String> answers = new ArrayList<>();
    ArrayList<String> clues = new ArrayList<>();
    String responseString;
    private float[] distance_travelled = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private float[] results = new float[]{0.0f, 0.0f, 0.0f};
    int qCounter = 0;

    TextView statusTV, questionTV, clueTV;
    EditText answerET;
    Button submitBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HyperTrack.initialize(this, "pk_57e6c87f1728b831a320c84c66bbcc4b3cf10988");

        setContentView(R.layout.activity_main);
        getData();
        //responseObject = getIntent().getStringExtra("data");
        Log.i("Gautam", "before locations");

        statusTV = findViewById(R.id.status);
        questionTV = findViewById(R.id.question);
        answerET = findViewById(R.id.answer);
        submitBTN = findViewById(R.id.submit);
        clueTV = findViewById(R.id.clue);

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.trip_map);
        mapFragment.getMapAsync(this);
        tryHyper();
    }

    private void getData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://13.232.33.75:3000/getLocs";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i("Volley Response", response);
                        responseString = response;
                        parseResponseObject(responseString);
                        addPolygon();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void parseResponseObject(String data) {
        /*
         * Logic to extract location array from json object
         * */

        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jArray = jsonObject.getJSONObject("geofence").getJSONArray("location");
            for (int i = 0; i < jArray.length(); i++) {
                Double a = jArray.getJSONObject(i).getDouble("lat");
                Double b = jArray.getJSONObject(i).getDouble("lng");
                locs.add(new LatLng(a, b));
            }
            jArray = jsonObject.getJSONArray("places");
            for(int i=0; i<jArray.length(); i++){
                Double a = jArray.getJSONObject(i).getJSONArray("coordinates").getDouble(0);
                Double b = jArray.getJSONObject(i).getJSONArray("coordinates").getDouble(1);
                String question = jArray.getJSONObject(i).getString("question");
                String answer = jArray.getJSONObject(i).getString("answer");
                String clue = jArray.getJSONObject(i).getString("answer");
                marks.add(new LatLng(a,b));
                questions.add(i, question);
                answers.add(i, answer);
                clues.add(i, clue);
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        Log.i("Gautam", "before map");
        //addFence(locations, googleMap);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void addPolygon() {

        LatLng[] ls = new LatLng[5];
        for(int i=0; i<5; i++){
            ls[i] = locs.get(i);
        }

        Polygon polygon = mGoogleMap.addPolygon(new PolygonOptions()
                .add(ls)
                .strokeColor(Color.GREEN)
                .fillColor(Color.parseColor("#ef9a9a")));
        polygon.setVisible(true);

        LatLng l = new LatLng(12.9940273, 77.6612323);
        MarkerOptions mar = new MarkerOptions().position(l)
                .title("Marker");

        mGoogleMap.addMarker(mar);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(l));
        Log.i("Gautam", "after map");
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        questionTV.setText(questions.get(qCounter%2));
        clueTV.setText(clues.get(qCounter%2));
        return true;
    }

    private void tryHyper() {
        Place expectedPlace = new Place();
        expectedPlace.setLocation(new GeoJSONLocation(12.9940273, 77.6612323));

        ActionParamsBuilder actionParamsBuilder = new ActionParamsBuilder();
        actionParamsBuilder.setType(Action.TYPE_DELIVERY);
        actionParamsBuilder.setExpectedPlace(expectedPlace);

        HyperTrack.createAction(actionParamsBuilder.build(), new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                Action action = (Action) response.getResponseObject();
                saveDeliveryAction(action);
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
            }
        });

        if(HyperTrack.isTracking()){
            Log.i("hyper", "True");
        }else{
            Log.i("hyper", "false");
        }

    }

    private void saveDeliveryAction(Action action) {
        Log.i("Hyper", action.getId());
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null)
            startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        Log.i("Updates", String.valueOf(location.getLatitude() + " " + location.getLongitude()));

        if(responseString != null) {
            for (int i = 0; i < 5; i++) {
                Location.distanceBetween(locs.get(i).latitude, locs.get(i).longitude, location.getLatitude(), location.getLongitude(), results);
                distance_travelled[i] += results[0];
                Log.i("Distance", String.valueOf(distance_travelled[i]));
            }
        }



    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}


// #ef9a9a, #e57373, #ef5350, #f44336, #e53935, #d32f2f, #c62828, #b71c1c