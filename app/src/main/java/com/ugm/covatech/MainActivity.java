package com.ugm.covatech;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.View;

import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView currentLocation;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    double currentLatitude;
    double currentLongitude;
    String userFullName;

    LinearLayout linearLayoutDataCovidPerProvinsi;

    TextView textViewKasusPositif, textViewKasusSembuh, textViewKasusMeninggal;

    FirebaseAuth fAuth;
    FirebaseFirestore db;
    FloatingActionButton scanButton;
    ImageView buttonNotification;
    TextView textCartBadge;
    TextView textTopTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textTopTitle = findViewById(R.id.title_top_bar);
        linearLayoutDataCovidPerProvinsi = findViewById(R.id.linearLayout_data_covid_per_provinsi);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }, 5);
        }

        textViewKasusPositif = findViewById(R.id.text_jumlah_positif);
        textViewKasusSembuh = findViewById(R.id.text_jumlah_sembuh);
        textViewKasusMeninggal = findViewById(R.id.text_jumlah_meninggal);
        scanButton = findViewById(R.id.addFab);
        textCartBadge = findViewById(R.id.cart_badge);

        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setBackgroundService();
        setupBottomNavigationView();
        getLastLocation();
        getUserProfile();
        volleyGet();
        setNotification();

        buttonNotification = findViewById(R.id.button_notification);

        buttonNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivity = new Intent(MainActivity.this, NotificationActivity.class);
                nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(nextActivity);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivity = new Intent(MainActivity.this, ScannerActivity.class);
                nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(nextActivity);
            }
        });

        linearLayoutDataCovidPerProvinsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivity = new Intent(MainActivity.this, DataCovidProvinsiActivity.class);
                nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(nextActivity);
            }
        });
    }

    public void setReviewList(View view){
        Intent nextActivity = new Intent(MainActivity.this, ReviewListActivity.class);
        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(nextActivity);
    }

    public void setCovaTribute(View view) {
        Intent nextActivity = new Intent(MainActivity.this, CovaTributeMain.class);
        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(nextActivity);
    }

    public void setFasilitas(View view) {
        Intent nextActivity = new Intent(MainActivity.this, FasilitasKesehatanActivity.class);
        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(nextActivity);
    }

    public void setCovaTrace(View view) {
        Intent nextActivity = new Intent(MainActivity.this, CovatraceActivity.class);
        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(nextActivity);
    }

    public void setCovaMaps(View view) {
        Intent nextActivity = new Intent(MainActivity.this, CovaMapsActivity.class);
        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(nextActivity);
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profil:
                        Intent nextActivity = new Intent(MainActivity.this, ProfileActivity.class);
                        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(nextActivity);
                }
                return true;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        Log.d("Test", "test");
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
                                    Log.d("Current Location", "Latitude :" + Double.toString(currentLatitude) + ", Longitude : " + Double.toString(currentLongitude));
                                    Geocoder geoCoder = new Geocoder(MainActivity.this);
                                    try {
                                        List<Address> address = geoCoder.getFromLocation(currentLatitude, currentLongitude, 5);
                                        Log.d("Country", address.get(0).getCountryCode());
                                        Log.d("Localy", address.get(0).getLocality());
                                        Log.d("Feature Name", address.get(0).getFeatureName());
                                        Log.d("Admin Area", address.get(0).getAdminArea());

                                        currentLocation = findViewById(R.id.currentLocation);
                                        currentLocation.setText(address.get(0).getLocality() + ", " + address.get(0).getAdminArea());

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
            // if permissions aren't available,
            // request for permissions
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

    public void getUserProfile() {
        final String userID = fAuth.getUid();
        DocumentReference docRef = db.collection("users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    userFullName = document.getString("Name");
                }
            }
        });
    }


    public void volleyGet() {
        String urlAPI = "https://api.kawalcorona.com/indonesia/";
        final List<String> jsonResponses = new ArrayList<>();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlAPI,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObjectCovid19Indo = response.getJSONObject(i);
                                Log.d("Data", jsonObjectCovid19Indo.toString());
                                Log.d("Kasus Positif", jsonObjectCovid19Indo.getString("positif"));
                                Log.d("Sembuh", jsonObjectCovid19Indo.getString("sembuh"));
                                Log.d("Meninggal", jsonObjectCovid19Indo.getString("meninggal"));

                                textViewKasusPositif.setText(jsonObjectCovid19Indo.getString("positif"));
                                textViewKasusSembuh.setText(jsonObjectCovid19Indo.getString("sembuh"));
                                textViewKasusMeninggal.setText(jsonObjectCovid19Indo.getString("meninggal"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        requestQueue.add(jsonArrayRequest);

    }

    public void setBackgroundService(){
        String input = "Test";
        Intent serviceIntent = new Intent(this, BackgroundTrackerServices.class);
        serviceIntent.putExtra("inputExtra", input);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void setNotification(){
        db.collection("users").document(fAuth.getUid()).collection("notification").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                textCartBadge.setText(Integer.toString(task.getResult().size()));
            }
        });

        db.collection("users").document(fAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = documentSnapshot.getString("Name");
                textTopTitle.setText("Halo , "+name+"\nCovatech-19");
            }
        });
    }


}