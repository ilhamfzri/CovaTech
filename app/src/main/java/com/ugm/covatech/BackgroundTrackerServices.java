package com.ugm.covatech;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.L;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;;import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.sql.Time;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class BackgroundTrackerServices extends Service {
    private Handler mHandler = new Handler();
    private GeofencingClient geofencingClient;
    Double lastLatitude = 0.0;
    Double lastLongitude = 0.0;
    Double lastWriteLatitude = 0.0;
    Double lastWriteLongitude = 0.0;
    Double homeLatitude = 0.0;
    Double homeLongitude = 0.0;
    float minimum_distance = 1000;
    String placeID_writer;
    String placeName_writer;
    LatLng placeLocation_writer;

    int count_static = 0;

    String lastDocumentTracking, userUID, lastDocumentLocationTracking, lastPlaceID;
    FirebaseFirestore db;
    FirebaseAuth fAuth;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userUID = fAuth.getUid();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CovaTech", "CovaTech", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        startBackground();
        mToastRunnable.run();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startBackground() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "CovaTech");
        notificationBuilder.setContentIntent(resultPendingIntent);

        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_sembuhicon)
                .setContentTitle("CovaTech Running In Background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            startReadLocation();
            mHandler.postDelayed(mToastRunnable, 5000);
        }
    };

    private void startReadLocation() {
        FusedLocationProviderClient fusedLocationClient;
        geofencingClient = LocationServices.getGeofencingClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(BackgroundTrackerServices.this);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Berikan Izin Untuk Mengakses GPS!", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Double currentLatitude = location.getLatitude();
                Double currentLongitude = location.getLongitude();
                Log.d("Current Latitude =", Double.toString(currentLatitude));
                Log.d("Current Longitude = ", Double.toString(currentLongitude));
                Log.d("LOG", "Count Static : " + Integer.toString(count_static));
                float distance = distanceLocation(currentLatitude, currentLongitude, lastLatitude, lastLongitude);
                float distance_with_last_write_location = distanceLocation(currentLatitude, currentLongitude, lastWriteLatitude, lastLongitude);

                if (distance > 15) {
                    lastLatitude = currentLatitude;
                    lastLongitude = currentLongitude;
                    count_static = 0;
                } else {
                    count_static = count_static + 1;
                }
                if (count_static % 5 == 0) {
                    read_last_location_db();
                }

                if (count_static % 12 == 0) {
                    if (distance_with_last_write_location < 15
                            && TextUtils.equals(lastDocumentTracking, "non_id") == false
                            && TextUtils.equals(lastDocumentLocationTracking, "non_id") == false) {
                        update_end_time();
                    }
                }

                if (count_static == 15) {
                    if (distance_with_last_write_location > 15) {
                        read_current_place(currentLatitude, currentLongitude);
                    }
                    count_static = 0;
                }
            }
        });
    }

    public void update_last_location_document(String lastDocument, LatLng lastLocation) {
        DocumentReference docRef = db.collection("users").document(userUID);
        GeoPoint locationGeopoint = new GeoPoint(lastLocation.latitude, lastLocation.longitude);
        Map<String, Object> dataUpdate = new HashMap<>();
        dataUpdate.put("last_document_tracking", lastDocument);
        dataUpdate.put("geopoint_last_location", locationGeopoint);
        docRef.set(dataUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("LOG", "Update Last Location Document!");
            }
        });
    }

    public void update_last_location_document_tracking(String lastDocument, String placeUID) {
        DocumentReference docRef = db.collection("users").document(userUID);
        Map<String, Object> dataUpdate = new HashMap<>();
        dataUpdate.put("last_document_location_tracking", lastDocument);
        dataUpdate.put("last_place_id", placeUID);
        docRef.set(dataUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("LOG", "Update Last Location Tracking Document!");
            }
        });
    }

    public void update_end_time() {
        if (TextUtils.isEmpty(lastDocumentTracking) == false) {
            DocumentReference docRef = db.collection("users").document(userUID).collection("tracking_data").document(lastDocumentTracking);
            Map<String, Object> dataUpdate = new HashMap<>();
            dataUpdate.put("end_time", Timestamp.now());
            docRef.set(dataUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("TAG", "Last End Time on Document Tracking" + lastDocumentTracking + "has been updated!");
                }
            });
        }
        if (TextUtils.isEmpty(lastDocumentLocationTracking) == false && TextUtils.equals(lastDocumentLocationTracking, "home")==false) {
            DocumentReference docRef = db.collection("location").document(lastPlaceID).collection("tracking_data").document(lastDocumentLocationTracking);
            Map<String, Object> dataUpdate = new HashMap<>();
            dataUpdate.put("end_time", Timestamp.now());
            docRef.set(dataUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("TAG", "Last End Time on Document Location Tracking" + lastDocumentLocationTracking + "has been updated!");
                }
            });
        }
    }

    public void read_current_place(final double latitude, final double longitude) {
        final float distance_from_home = distanceLocation(latitude, longitude, homeLatitude, homeLongitude);
        if (distance_from_home < 15) {
            LatLng latLngHome = new LatLng(homeLatitude, homeLongitude);
            write_document_tracking("home", "Rumah", latLngHome);
            lastWriteLatitude = latLngHome.latitude;
            lastWriteLongitude = latLngHome.longitude;

        } else {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
            final PlacesClient placesClient = Places.createClient(this);
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG);
            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<FindCurrentPlaceResponse> task) {
                        if (task.isSuccessful()) {
                            FindCurrentPlaceResponse response = task.getResult();
                            float minimum_distance = 1000;
                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Double placeLikelihoodLatitude = placeLikelihood.getPlace().getLatLng().latitude;
                                Double placeLikelihoodLongitude = placeLikelihood.getPlace().getLatLng().longitude;
                                float distance_with_place_likelihood = distanceLocation(latitude, longitude, placeLikelihoodLatitude, placeLikelihoodLongitude);
                                if (distance_with_place_likelihood < minimum_distance) {
                                    minimum_distance = distance_with_place_likelihood;
                                    placeID_writer = placeLikelihood.getPlace().getId();
                                    placeName_writer = placeLikelihood.getPlace().getName();
                                    placeLocation_writer = placeLikelihood.getPlace().getLatLng();
                                }
                                Log.d("TAG", String.format("Place '%s' has likelihood: %f and distance %f",
                                        placeLikelihood.getPlace().getName(),
                                        placeLikelihood.getLikelihood(), distance_with_place_likelihood));
                            }
                            if (minimum_distance <= 15) {
                                write_document_tracking(placeID_writer, placeName_writer, placeLocation_writer);
                            } else {
                                LatLng lastLocation = new LatLng(latitude, longitude);
                                update_last_location_document("non_id", lastLocation);
                                update_last_location_document_tracking("non_id", "non_id");
                                lastWriteLatitude = lastLocation.latitude;
                                lastWriteLongitude = lastLocation.longitude;
                                lastDocumentTracking = "non_id";
                                lastDocumentLocationTracking = "non_id";
                            }

                        } else {
                            Log.d("TAG", "Not Detected Nearby Places!");
                        }
                    }
                });
            }
        }
    }

    public void write_document_tracking(final String placeUID, String placeName, final LatLng placeLocation) {
        final String document_id = Long.toString(Timestamp.now().getSeconds());
        Map<String, Object> dataUpdate = new HashMap<>();
        GeoPoint locationGeopoint = new GeoPoint(placeLocation.latitude, placeLocation.longitude);
        dataUpdate.put("place_name", placeName);
        dataUpdate.put("place_id", placeUID);
        dataUpdate.put("start_time", Timestamp.now());
        dataUpdate.put("end_time", Timestamp.now());
        dataUpdate.put("review_state", false);
        dataUpdate.put("place_location", locationGeopoint);
        DocumentReference docRef = db.collection("users").document(userUID).collection("tracking_data").document(document_id);
        docRef.set(dataUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("TAG", "Last End Time on Document Tracking" + lastDocumentTracking + "has been updated!");
                lastDocumentTracking = document_id;
                lastWriteLatitude = placeLocation.latitude;
                lastWriteLongitude = placeLocation.longitude;
                update_last_location_document(document_id, placeLocation);
            }
        });
        Log.d("StateMent", Boolean.toString(TextUtils.equals(placeUID, "home") ));
        if (TextUtils.equals(placeUID, "home") == false) {
            final String document_location_id = userUID + document_id;
            Map<String, Object> dataUpdateLocation = new HashMap<>();
            dataUpdateLocation.put("user_id", userUID);
            dataUpdateLocation.put("start_time", Timestamp.now());
            dataUpdateLocation.put("end_time", Timestamp.now());
            DocumentReference docRefLocation = db.collection("location").document(placeUID).collection("tracking_data").document(document_location_id);
            docRefLocation.set(dataUpdateLocation, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    lastDocumentLocationTracking = document_location_id;
                    update_last_location_document_tracking(document_location_id, placeUID);
                }
            });
        }
        else{
            update_last_location_document_tracking("home", "home");
        }
    }

    public void read_last_location_db() {
        //This function for reading last location on the database when application first starting
        DocumentReference docRef = db.collection("users").document(userUID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        GeoPoint geoPointLastLocation = document.getGeoPoint("geopoint_last_location");
                        GeoPoint homeLocation = document.getGeoPoint("home_location");
                        String lastDocument = document.getString("last_document_tracking");
                        String lastLocationDocument = document.getString("last_document_location_tracking");
                        String lastPlace = document.getString("last_place_id");

                        if (geoPointLastLocation == null || TextUtils.isEmpty(lastDocument) || TextUtils.isEmpty(lastLocationDocument) || TextUtils.isEmpty(lastPlace)) {
                            Log.d("TAG", "Geopoint Last Location Not Found!");
                        } else {
                            Log.d("TAG", "Geopoint Last Location Found!");
                            lastWriteLatitude = geoPointLastLocation.getLatitude();
                            lastWriteLongitude = geoPointLastLocation.getLongitude();
                            lastDocumentTracking = lastDocument;
                            lastDocumentLocationTracking = lastLocationDocument;
                            lastPlaceID = lastPlace;
                        }
                        homeLatitude = homeLocation.getLatitude();
                        homeLongitude = homeLocation.getLongitude();
                    }
                }
            }
        });
    }
    //This functions is to calculate distance (in meters) between two cordinates in lat & lon format.
    public float distanceLocation(double srcLat, double srcLon, double desLat, double desLon) {
        Location locationA = new Location("point A");

        locationA.setLatitude(srcLat);
        locationA.setLongitude(srcLon);

        Location locationB = new Location("point B");

        locationB.setLatitude(desLat);
        locationB.setLongitude(desLon);

        float distance = locationA.distanceTo(locationB);
        return distance;
    }

}
