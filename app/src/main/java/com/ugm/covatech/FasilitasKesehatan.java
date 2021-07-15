package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FasilitasKesehatan extends AppCompatActivity {
    final ArrayList<String> arrayName = new ArrayList<String>();
    final ArrayList<String> arrayAddress = new ArrayList<String>();
    final ArrayList<String> arrayDistance = new ArrayList<String>();
    final ArrayList<String> arrayContact = new ArrayList<String>();
    final ArrayList<String> arrayDirection = new ArrayList<String>();
    RecyclerView recyclerView;
    AdapterFasilitasKesehatan adapterFasilitasKesehatan;
    FirebaseFirestore db;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    double currentLatitude;
    double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fasilitas_kesehatan);
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        loadData();
    }

    public void loadData() {
        getLastLocation();
        db.collection("fasilitas_kesehatan").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //Check Current Latitude & Longitude
                    Log.d("Current Location", "Latitude :" + Double.toString(currentLatitude) +", Longitude : "+ Double.toString(currentLongitude));

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        double placeLat = document.getGeoPoint("Geopoint").getLatitude();
                        double placeLon = document.getGeoPoint("Geopoint").getLongitude();

                        float distance = distanceLocation(placeLat, placeLon, currentLatitude, currentLongitude);
                        Log.d("Current Location", "Latitude :" + Double.toString(placeLat) +", Longitude : "+ Double.toString(placeLon));
                        String roundedDistance = String.format("%.2f", distance);
                        Log.d("Distance", roundedDistance);

                        arrayDistance.add(roundedDistance);
                        arrayName.add(document.getString("Name"));
                        arrayAddress.add(document.getString("Address"));
                        arrayContact.add(document.getString("Contact"));
                        arrayDirection.add(document.getString("Direction"));

                    }

                    final String[] stringArrayName = arrayName.toArray(new String[0]);
                    final String[] stringArrayDistance = arrayDistance.toArray(new String[0]);
                    final String[] stringArrayAddress = arrayAddress.toArray(new String[0]);
                    final String[] stringArrayContact = arrayContact.toArray(new String[0]);
                    final String[] stringArrayDirection = arrayDirection.toArray(new String[0]);

                    adapterFasilitasKesehatan = new AdapterFasilitasKesehatan(FasilitasKesehatan.this, stringArrayName, stringArrayDistance, new AdapterFasilitasKesehatan.ClickListener() {
                        @Override
                        public void onPositionClicked(int position) {
                            showBottomSheet(stringArrayName[position], stringArrayAddress[position], stringArrayDistance[position], stringArrayContact[position], stringArrayDirection[position]);
                        }
                    });
                    recyclerView.setAdapter(adapterFasilitasKesehatan);
                    recyclerView.setLayoutManager(new LinearLayoutManager(FasilitasKesehatan.this));
                }
            }
        });
    }

    public void showBottomSheet(String sName, String sAddress, String sDistance, final String sContact, final String sDirection) {
        TextView namePlace, addressPlace, distancePlace;
        Button buttonDial;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FasilitasKesehatan.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet_fasilitas_kesehatan, (LinearLayout) findViewById(R.id.bottomSheetContainer));

        bottomSheetView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                String number = sContact.replaceAll("[\\D]", "");
                dialIntent.setData(Uri.parse("tel:" + number));
                startActivity(dialIntent);
            }
        });

        bottomSheetView.findViewById(R.id.directionPlaces).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + sDirection);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        namePlace = bottomSheetView.findViewById(R.id.namePlace);
        namePlace.setText(sName);
        addressPlace = bottomSheetView.findViewById(R.id.addressPlace);
        addressPlace.setText(sAddress);
        buttonDial = bottomSheetView.findViewById(R.id.button);
        buttonDial.setText(sContact);

        distancePlace = bottomSheetView.findViewById(R.id.distancePlaces);
        distancePlace.setText(sDistance + " km dari lokasimu");
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    public float distanceLocation(double srcLat, double srcLon, double desLat, double desLon){
        Location locationA = new Location("point A");

        locationA.setLatitude(srcLat);
        locationA.setLongitude(srcLon);

        Location locationB = new Location("point B");

        locationB.setLatitude(desLat);
        locationB.setLongitude(desLon);

        float distance = locationA.distanceTo(locationB);
        return distance/1000;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        Log.d("Test","test");
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
                                }
                            }
                        });
                }
                else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
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


}