package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    final ArrayList<String> arrayTitleNotification= new ArrayList<String>();
    final ArrayList<String> arrayBodyNotification= new ArrayList<String>();
    final ArrayList<String> arrayDateNotification= new ArrayList<String>();
    final ArrayList<Integer> arrayIconNotification= new ArrayList<Integer>();

    FirebaseAuth fAuth;
    FirebaseFirestore db;
    Handler handler;

    RecyclerView recyclerView;
    MaterialToolbar topBar;
    LottieAnimationView loadingAnimation;
    ImageView imageViewNoNotification;
    TextView textViewNoNotification;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        loadingAnimation = findViewById(R.id.loadingAnimation);
        loadingAnimation.setVisibility(View.VISIBLE);

        textViewNoNotification = findViewById(R.id.text_no_notification);
        textViewNoNotification.setVisibility(View.GONE);

        imageViewNoNotification = findViewById(R.id.image_no_notification);
        imageViewNoNotification.setVisibility(View.GONE);

        topBar = findViewById(R.id.topAppBar);
        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backActivity = new Intent(NotificationActivity.this, MainActivity.class);
                backActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(backActivity);
            }
        });

        loadData();
    }

    public void loadData(){
        String userUID = fAuth.getUid();
        db.collection("users").document(userUID).collection("notification").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String pattern = "HH:mm a , d MMMM yyyy";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        Timestamp timestampNotification = document.getTimestamp("notification_time");
                        Date dateNotification = timestampNotification.toDate();
                        String tanggalNotifikasi = simpleDateFormat.format(dateNotification);
                        arrayDateNotification.add(tanggalNotifikasi);
//                        arrayIconNotification.add(R.drawable.ic_baseline_account_circle_24);

                        if(TextUtils.equals(document.getString("notification_type"), "LAPORAN")){
                            String tanggalLaporan = document.getString("tanggal_laporan");
                            if(TextUtils.equals(document.getString("jenis_laporan"), "Laporan Test COVID-19")){
                                arrayTitleNotification.add("Laporan Test COVID-19");
                                if(document.getBoolean("validity")==true){
                                    arrayIconNotification.add(R.drawable.ic_baseline_done_all_24);
                                    arrayBodyNotification.add("Laporan hasil test COVID-19 yang anda kirimkan pada tanggal "+tanggalLaporan+"  telah divalidasi, terimakasih atas partipisasi anda.");
                                }
                                else{
                                    arrayIconNotification.add(R.drawable.ic_baseline_cancel_24);
                                    arrayBodyNotification.add("Laporan hasil test COVID-19 yang anda kirimkan pada tanggal "+tanggalLaporan+"  ditolak oleh Admin, pastikan data yang anda kirimkan benar.");
                                }
                            }
                            else{
                                arrayTitleNotification.add("Laporan Vaksin");
                                if(document.getBoolean("validity")==true){
                                    arrayIconNotification.add(R.drawable.ic_baseline_done_all_24);
                                    arrayBodyNotification.add("Laporan vaksinasi yang anda kirimkan pada tanggal "+tanggalLaporan+"  telah divalidasi, terimakasih atas partipisasi anda.");
                                }
                                else{
                                    arrayIconNotification.add(R.drawable.ic_baseline_cancel_24);
                                    arrayBodyNotification.add("Laporan vaksinasi yang anda kirimkan pada tanggal "+tanggalLaporan+"  ditolak oleh Admin, pastikan data yang anda kirimkan benar.");
                                }
                            }
                        }
                        else{
                            arrayTitleNotification.add("Peringatan !");
                            arrayIconNotification.add(R.drawable.ic_warning);

                            Timestamp startTime = document.getTimestamp("start_time");
                            Timestamp endTime = document.getTimestamp("end_time");

                            Date dateStartTime = startTime.toDate();
                            Date dateEndTime = endTime.toDate();

                            String startPattern = "d MMMM yyyy, HH:mm a";
                            final String endPattern = "HH:mm a";

                            SimpleDateFormat simpleDateFormatStart = new SimpleDateFormat(startPattern);
                            SimpleDateFormat simpleDateFormatEnd = new SimpleDateFormat(endPattern);

                            final String stringStartTime  = simpleDateFormatStart.format(dateStartTime);
                            final String stringEndTime = simpleDateFormatEnd.format(dateEndTime);

                            Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
                            final PlacesClient placesClient = Places.createClient(NotificationActivity.this);

                            Log.d("Here", document.getString("place_id"));

                            final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);
                            final FetchPlaceRequest request = FetchPlaceRequest.builder(document.getString("place_id"), placeFields).build();
                            placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                                @Override
                                public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                    Log.d("Location", fetchPlaceResponse.getPlace().getName());
                                    arrayBodyNotification.add("Anda berpapasan dengan penderita COVID-19 pada " + stringStartTime + " - " + stringEndTime + " di " + fetchPlaceResponse.getPlace().getName()
                                    + ". Silahkan lakukan pemeriksaan COVID-19 secepatnya.");
                                }
                            });

                        }
                    }

                    handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingAnimation.setVisibility(View.GONE);
                            Log.d("datae", arrayBodyNotification.toString());
                            final String[] stringsTitleNotification = arrayTitleNotification.toArray(new String[0]);
                            final String[] stringsBodyNotification = arrayBodyNotification.toArray(new String[0]);
                            final String[] stringsDateNotification = arrayDateNotification.toArray(new String[0]);
                            final Integer[] integersIconNotification = arrayIconNotification.toArray(new Integer[0]);
                            AdapterNotification adapterNotification = new AdapterNotification(NotificationActivity.this, stringsTitleNotification, stringsBodyNotification,
                                    stringsDateNotification,integersIconNotification, new AdapterNotification.ClickListener() {
                                @Override
                                public void onPositionClicked(int position) {

                                }
                            });

                            if(arrayBodyNotification.size()==0){
                                imageViewNoNotification.setVisibility(View.VISIBLE);
                                textViewNoNotification.setVisibility(View.VISIBLE);
                            }

                            recyclerView.setAdapter(adapterNotification);
                            recyclerView.setLayoutManager(new LinearLayoutManager(NotificationActivity.this));
                        }
                    },3000);

                }
            }
        });
    }
}