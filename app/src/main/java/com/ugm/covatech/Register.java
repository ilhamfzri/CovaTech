package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    EditText mName, mEmail, mPassword1, mPassword2;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    Double homeLatitude = 0.0;
    Double homeLongitude =0.0;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mName = findViewById(R.id.editName);
        mEmail = findViewById(R.id.editEmail);
        mPassword1 = findViewById(R.id.editPassword);
        mPassword2 = findViewById(R.id.editPassword2);
        getLastLocation();
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
                                    homeLatitude = location.getLatitude();
                                    homeLongitude = location.getLongitude();
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

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    public static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public void setRegister(View view) {
        final String vEmail = mEmail.getText().toString().trim();
        final String vName = mName.getText().toString().trim();
        final String vPassword = mPassword1.getText().toString().trim();
        final String vPassword2 = mPassword2.getText().toString().trim();

        //Check Email Validity
        if (isValid(vEmail) == false) {
            mEmail.setError("Masukan email yang benar!");

            return;
        }

        //Check Name
        if (TextUtils.isEmpty(vName)) {
            mName.setError("Masukan Nama anda!");
            return;
        }

        //Check Password length, if below 6 character return error
        if (vPassword.length() < 6 || vPassword2.length() < 6) {
            Toast.makeText(Register.this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Check Password
        if (vPassword.equals(vPassword2) == false) {
            Toast.makeText(Register.this, "Password yang anda masukan berbeda dengan password konfirmasi!", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.createUserWithEmailAndPassword(vEmail, vPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = fAuth.getCurrentUser();
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Register.this, "Email Verifikasi Telah Dikirim!", Toast.LENGTH_SHORT).show();
                            String userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);

                            // Save user data to firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("Name", vName);
                            userData.put("Email", vEmail);
                            userData.put("VaksinStatus", false);
                            userData.put("CovidStatus", true);
                            GeoPoint homeGeopoint = new GeoPoint(homeLatitude, homeLongitude);
                            userData.put("home_location", homeGeopoint);

                            documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    fAuth.signOut();
                                    Intent loginActivity = new Intent(Register.this, LoginActivity.class);
                                    startActivity(loginActivity, ActivityOptions.makeSceneTransitionAnimation(Register.this).toBundle());
                                }
                            });
                        }
                    });
                }
            }


        });
    }

    public void setLogin(View view) {
        Intent loginActivity = new Intent(Register.this, LoginActivity.class);
        startActivity(loginActivity, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}