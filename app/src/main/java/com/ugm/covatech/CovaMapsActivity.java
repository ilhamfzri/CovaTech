package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class CovaMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    String stringMapType = "street";

    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    private GoogleMap mMap;
    double currentLatitude;
    double currentLongitude;
    FirebaseAuth fAuth;
    LatLng currentLocation;
    LatLng homeLocation;
    MaterialToolbar topBar;
    FloatingActionButton floatingSetMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cova_maps);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        topBar = findViewById(R.id.topAppBar);
        floatingSetMap = findViewById(R.id.floating_set_map);

        floatingSetMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMapType();
            }
        });

        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backActivity = new Intent(CovaMapsActivity.this, MainActivity.class);
                backActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(backActivity);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getLastLocation();

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    currentLatitude = location.getLatitude();
                                    currentLongitude = location.getLongitude();

                                    currentLocation = new LatLng(currentLatitude, currentLongitude);

//                                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Lokasi Saat Ini"));
                                    mMap.setMyLocationEnabled(true);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));

                                    Log.d("Current Location", "Latitude :" + Double.toString(currentLatitude) + ", Longitude : " + Double.toString(currentLongitude));
                                    Geocoder geoCoder = new Geocoder(CovaMapsActivity.this);
                                    try {
                                        List<Address> address = geoCoder.getFromLocation(currentLatitude, currentLongitude, 5);
                                        Log.d("Country", address.get(0).getCountryCode());
                                        Log.d("Localy", address.get(0).getLocality());
                                        Log.d("Feature Name", address.get(0).getFeatureName());
                                        Log.d("Admin Area", address.get(0).getAdminArea());

                                    } catch (IOException e) {
                                    } catch (NullPointerException e) {
                                    }
                                }
                            }
                        });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    public void setMapType(){
        ImageView imageViewSatellite, imageViewTerrain;
        TextView textViewSatellite, textViewTerrain;
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CovaMapsActivity.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).
                inflate(R.layout.layout_bottom_sheet_fasilitas_map_type, (LinearLayout) findViewById(R.id.bottomSheetContainer));

        imageViewSatellite = bottomSheetView.findViewById(R.id.image_satellite);
        imageViewTerrain = bottomSheetView.findViewById(R.id.image_street);
        textViewSatellite = bottomSheetView.findViewById(R.id.textview_satellite);
        textViewTerrain = bottomSheetView.findViewById(R.id.textview_street);

        if(stringMapType=="street"){
            imageViewTerrain.setBackgroundColor(Color.parseColor("#502BA7"));
            textViewTerrain.setTextColor(Color.parseColor("#502BA7"));
            imageViewSatellite.setBackgroundColor(Color.parseColor("#FFFFFF"));
            textViewSatellite.setTextColor(Color.parseColor("#121212"));
        }
        else{
            imageViewSatellite.setBackgroundColor(Color.parseColor("#502BA7"));
            textViewSatellite.setTextColor(Color.parseColor("#502BA7"));
            imageViewTerrain.setBackgroundColor(Color.parseColor("#FFFFFF"));
            textViewTerrain.setTextColor(Color.parseColor("#121212"));

        }

        imageViewSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stringMapType="satellite";
                bottomSheetDialog.dismiss();
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        imageViewTerrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stringMapType="street";
                bottomSheetDialog.dismiss();
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}