package com.example.googlemaps2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    Location currentLocation;
    private static int REQUEST_CODE = 101;


    private String TrainStationName;
    private String STOPLAT;
    private String STOPLON;
    private ArrayList<PointD> latlngs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest req = new LocationRequest();
        req.setInterval(5000); // 2 seconds
        req.setFastestInterval(500); // 500 milliseconds
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        getDeviceLocation();


        latlngs = new ArrayList<PointD>();
        getTrainstationLocation();
    }

    private void getDeviceLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        Task<Location> task = mFusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null ){
                    currentLocation=location;
                    Toast.makeText(getApplicationContext(),currentLocation.getLongitude()
                            +""+currentLocation.getLatitude(),Toast.LENGTH_LONG).show();
                    System.out.println("Latitude:"+currentLocation.getLatitude()+"  Longitude: "+currentLocation.getLongitude());
                    SupportMapFragment supportMapFragment =(SupportMapFragment)getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    supportMapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

        System.out.println("onMap: "+latLng);
        MarkerOptions markerOptions=new MarkerOptions().position(latLng).title("I am here!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.addMarker(markerOptions);

        for(int i = 0; i < latlngs.size(); i++) {
            setLocation(googleMap, latlngs.get(i));
        }

    }

    private void setLocation(GoogleMap googleMap, PointD point) {
        final LatLng trainstation = new LatLng(point.lat, point.lng);

        final Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(trainstation)
                .title(point.name));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(trainstation));
    }


    public void  getTrainstationLocation(){
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url ="http://data-atgis.opendata.arcgis.com/datasets/c82756c875ff4e9fad0bc7a9f97ef7a8_0.geojson";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                StringBuilder localities = new StringBuilder();

                try {
                    JSONArray data = response.getJSONArray("features");

                    for (int index = 0; index < data.length(); index++) {
                        JSONObject station = data.getJSONObject(index);

                        //JSONObject geometry = station.getJSONObject("geometry");
                        JSONObject properties = station.getJSONObject("properties");

                        TrainStationName = properties.getString("STOPNAME");
                        STOPLAT = properties.getString("STOPLAT");
                        STOPLON = properties.getString("STOPLON");

                        localities.append(properties.getString("STOPNAME") + "\n");
                        localities.append(properties.getString("STOPLAT") + "\n");
                        localities.append(properties.getString("STOPLON") + "\n");
                        double lat = Double.valueOf(STOPLAT);
                        double lng = Double.valueOf(STOPLON);
                        latlngs.add(new PointD(TrainStationName, lat, lng));
                    }

                    System.err.println(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //output.setText(localities.toString());
                //System.out.println(TrainStationName + " , " + STOPLAT +","+ STOPLON);
                Log.i("info",localities.toString());

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(MapsActivity.this);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.getLocalizedMessage());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 101:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getDeviceLocation();
                }
                break;
        }
    }
}
