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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;;import pub.devrel.easypermissions.EasyPermissions;


public class BackgroundTrackerServices extends Service {
    private Handler mHandler = new Handler();
    private GeofencingClient geofencingClient;
    Double lastLatitude = 0.0;
    Double lastLongitude = 0.0;
    int count_static = 0;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
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
            Log.d("Background", "Running 2S");
            startReadLocation();
            mHandler.postDelayed(mToastRunnable, 5000);
        }
    };

    private void startReadLocation() {
        FusedLocationProviderClient fusedLocationClient;
        geofencingClient = LocationServices.getGeofencingClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(BackgroundTrackerServices.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                float distance = distanceLocation(currentLatitude, currentLongitude, lastLatitude, lastLongitude);
                if (distance > 15) {
                    count_static = 0;
                } else {
                    count_static = count_static + 1;
                }

                if (count_static > 36) {
                    //Access NearbyLocation & Write to DB
                }
                Log.d("Distance Last =", Float.toString(distance));

                lastLatitude = currentLatitude;
                lastLongitude = currentLongitude;

            }
        });
    }

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
