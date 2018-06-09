package com.example.hackio.twentythreeml;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.GeoJSONLocation;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    String responseObject;
    LatLng[] locations;
    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HyperTrack.initialize(this, "pk_57e6c87f1728b831a320c84c66bbcc4b3cf10988");

        setContentView(R.layout.activity_main);
        responseObject = getIntent().getStringExtra("data");
        Log.i("Gautam", "before locations");
        //locations = parseResponseObject(responseObject);

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.trip_map);

        mapFragment.getMapAsync(this);
        tryHyper();
    }

    private LatLng[] parseResponseObject(String data){
        LatLng[] locs = null;   //////
        /*
        * Logic to extract location array from json object
        * */
        return locs;
    }

    private void addFence(LatLng[] locs, GoogleMap gMap){
        int nos = locs.length;

        /*
        Polygon polygon = gMap.addPolygon(new PolygonOptions()
                .add(locs)
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));
        polygon.setVisible(true);

        LatLng l = locs[0];
        */

        Polygon polygon = gMap.addPolygon(new PolygonOptions()
                .add(new LatLng(12.9940273,77.6612323),
                        new LatLng(12.9948984,77.6610944),
                        new LatLng(12.9944162,77.660412),
                        new LatLng(12.9935113,77.6608457),
                        new LatLng(12.9938952,77.6614162))
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));
        polygon.setVisible(true);

        LatLng l = new LatLng(12.9940273,77.6612323);
        /*
        new LatLng(12.9940273,77.6612323),
        new LatLng(12.9948984,77.6610944),
        new LatLng(12.9944162,77.660412),
        new LatLng(12.9935113,77.6608457),
        new LatLng(12.9938952,77.6614162),
        */

        gMap.addMarker(new MarkerOptions().position(l)
                .title("Marker"));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(l));
        Log.i("Gautam", "after map");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("Gautam", "before map");
        //addFence(locations, googleMap);
        Polygon polygon = googleMap.addPolygon(new PolygonOptions()
                .add(new LatLng(12.9940273,77.6612323),
                        new LatLng(12.9948984,77.6610944),
                        new LatLng(12.9944162,77.660412),
                        new LatLng(12.9935113,77.6608457),
                        new LatLng(12.9938952,77.6614162))
                .strokeColor(Color.RED)
                .fillColor(Color.BLUE));
        polygon.setVisible(true);

        LatLng l = new LatLng(12.9940273,77.6612323);
        googleMap.addMarker(new MarkerOptions().position(l)
                .title("Marker"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(l));
        Log.i("Gautam", "after map");
    }

    private void tryHyper(){
        Place expectedPlace = new Place();
        expectedPlace.setLocation(new GeoJSONLocation(12.9940273,77.6612323));

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
    }

    private void saveDeliveryAction(Action action){
        Log.i("Hyper", action.getId());
    }

}
