package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HasilTestValidationActivity extends AppCompatActivity {
    String sDocumentUID;
    String userIDRef;
    FirebaseFirestore db;
    EditText editTextTanggalTest, editTextNamaLengkap, editTextNIK, editTextInstansiKesehatan, editTextTanggalPemeriksaan, editTextJenisPemeriksaan, editTextHasilPemeriksaan;
    MaterialButton buttonShowDocument;
    CheckBox checkBox;
    TextView buttonTolakLaporan, buttonTerimaLaporan;
    Handler handler, handler3;
    String tanggalLaporanNotification;
    LinearLayout mainLayout, loadingLayout;
    LottieAnimationView loadingAnimation, doneAnimation;
    TextView loadingStatus;
    Button buttonHome;


    final ArrayList<String> arrayDocumentID = new ArrayList<String>();
    final ArrayList<String> arrayUserID = new ArrayList<String>();
    final ArrayList<String> arrayPlaceID = new ArrayList<String>();
    final ArrayList<Timestamp> arrayTimestampStart = new ArrayList<Timestamp>();
    final ArrayList<Timestamp> arrayTimestampEnd = new ArrayList<Timestamp>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        sDocumentUID = getIntent().getStringExtra("document_uid");
        setContentView(R.layout.activity_hasil_test_validation);
        mainLayout = findViewById(R.id.main_layout);
        loadingLayout = findViewById(R.id.loading_layout);

        mainLayout.setVisibility(View.VISIBLE);
        loadingLayout.setVisibility(View.GONE);

        loadingAnimation = findViewById(R.id.loadingAnimation);
        doneAnimation = findViewById(R.id.doneAnimation);
        loadingStatus = findViewById(R.id.loadingStatus);
        buttonHome = findViewById(R.id.button_home);


        checkBox = findViewById(R.id.checkbox_pernyataan);

        buttonShowDocument = findViewById(R.id.button_document);
        buttonShowDocument.setEnabled(false);

        buttonTolakLaporan = findViewById(R.id.button_tolak_laporan);
        buttonTerimaLaporan = findViewById(R.id.button_terima_laporan);

        editTextNamaLengkap = findViewById(R.id.edittext_nama_lengkap);
        editTextNIK = findViewById(R.id.edittext_nik);
        editTextInstansiKesehatan = findViewById(R.id.edittext_tempat_test);
        editTextTanggalPemeriksaan = findViewById(R.id.edittext_tanggal_test);
        editTextJenisPemeriksaan = findViewById(R.id.edittext_jenis_pemeriksaan);
        editTextHasilPemeriksaan = findViewById(R.id.edittext_hasil_pemeriksaan);

        editTextNamaLengkap.setFocusable(false);
        editTextNIK.setFocusable(false);
        editTextInstansiKesehatan.setFocusable(false);
        editTextTanggalPemeriksaan.setFocusable(false);
        editTextJenisPemeriksaan.setFocusable(false);
        editTextHasilPemeriksaan.setFocusable(false);
        loadData();


    }

    public void loadData(){
        db.collection("laporan_test").document(sDocumentUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    final DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String pattern = "d MMMM yyyy";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        Timestamp timestampLaporan = document.getTimestamp("tanggal_kirim_laporan");
                        final Date dateLaporan = timestampLaporan.toDate();
                        String stringTanggalLaporan = simpleDateFormat.format(dateLaporan);

                        tanggalLaporanNotification = stringTanggalLaporan;
                        editTextNamaLengkap.setText(document.getString("nama"));
                        editTextNIK.setText(document.getString("nik"));
                        editTextInstansiKesehatan.setText(document.getString("instansi_kesehatan"));
                        editTextTanggalPemeriksaan.setText(stringTanggalLaporan);
                        editTextJenisPemeriksaan.setText(document.getString("jenis_test"));
                        editTextHasilPemeriksaan.setText(document.getString("hasil_test"));
                        userIDRef = document.getString("user_id");

                        buttonShowDocument.setEnabled(true);
                        buttonShowDocument.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView documentImage;
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(HasilTestValidationActivity.this, R.style.BottomSheetDialogTheme);
                                View bottomSheetView = LayoutInflater.from(getApplicationContext())
                                        .inflate(R.layout.layout_bottom_sheet_show_document, (LinearLayout) findViewById(R.id.bottomSheetContainer));
                                documentImage = bottomSheetView.findViewById(R.id.image);
                                Glide.with(HasilTestValidationActivity.this).load(document.getString("document_link")).into(documentImage);
                                bottomSheetDialog.setContentView(bottomSheetView);
                                bottomSheetDialog.show();
                            }
                        });

                        buttonTerimaLaporan.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("TAG", "HERE");
                                if(checkBox.isChecked()==false){
                                    Toast.makeText(HasilTestValidationActivity.this, "Mohon Centang Pernyataan!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    mainLayout.setVisibility(View.GONE);
                                    loadingLayout.setVisibility(View.VISIBLE);
                                    loadingStatus.setText("Mohon Tunggu");
                                    loadingAnimation.setVisibility(View.VISIBLE);
                                    buttonHome.setVisibility(View.GONE);
                                    doneAnimation.setVisibility(View.GONE);

                                    final Map<String, Object> dataValidasi = new HashMap<>();
                                    dataValidasi.put("validation_status", true);
                                    dataValidasi.put("document_validity", true);
                                    DocumentReference documentRefValidation = db.collection("laporan_test").document(sDocumentUID);
                                    documentRefValidation.set(dataValidasi, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            setTerimaLaporan(document.getString("hasil_test"),document.getString("user_id"), dateLaporan);
                                        }
                                    });
                                }
                            }
                        });

                        buttonTolakLaporan.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(checkBox.isChecked()==false){
                                    Toast.makeText(HasilTestValidationActivity.this, "Mohon Centang Pernyataan!", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    mainLayout.setVisibility(View.GONE);
                                    loadingLayout.setVisibility(View.VISIBLE);
                                    loadingStatus.setText("Mohon Tunggu");
                                    loadingAnimation.setVisibility(View.VISIBLE);
                                    buttonHome.setVisibility(View.GONE);
                                    doneAnimation.setVisibility(View.GONE);
                                    setTolakLaporan();
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    public void setTolakLaporan(){
        final Map<String, Object> dataValidasi = new HashMap<>();
        dataValidasi.put("validation_status", true);
        dataValidasi.put("document_validity", false);
        DocumentReference documentRefValidation = db.collection("laporan_test").document(sDocumentUID);
        documentRefValidation.set(dataValidasi, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("LOG", "Data Berhasil di Tolak!");
                Map<String, Object> dataUpdateNotificationUserRef= new HashMap<>();
                dataUpdateNotificationUserRef.put("notification_type", "LAPORAN");
                dataUpdateNotificationUserRef.put("tanggal_laporan", tanggalLaporanNotification);
                dataUpdateNotificationUserRef.put("jenis_laporan", "Laporan Test COVID-19");
                dataUpdateNotificationUserRef.put("validity", false);
                String documentNotificationUID = Long.toString(Timestamp.now().getSeconds())+userIDRef;
                db.collection("users").document(userIDRef).collection("notification").document(documentNotificationUID)
                        .set(dataUpdateNotificationUserRef, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("LOG", "User Notification Updated!");
                        handler3=new Handler();
                        handler3.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("LOG", "User Notification Updated!");
                                buttonHome.setVisibility(View.VISIBLE);
                                loadingAnimation.setVisibility(View.GONE);
                                doneAnimation.setVisibility(View.VISIBLE);
                                loadingStatus.setText("Proses Berhasil");
                                buttonHome.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent homeActivity = new Intent(HasilTestValidationActivity.this, MainActivity.class);
                                        homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        startActivity(homeActivity);
                                    }
                                });
                            }
                        },5000);
                    }
                });
            }
        });
    }

    public void setTerimaLaporan(String hasilTest, final String userId, Date dateLaporan){
        if(TextUtils.equals(hasilTest, "Positif/Reaktif")){
            //Check lokasi 14 hari terakhir yang dikunjungi
            Calendar lastCalendar = Calendar.getInstance();
            lastCalendar.setTime(dateLaporan);
            lastCalendar.add(Calendar.DATE, -14);
            Date date14HariTerakhir = lastCalendar.getTime();

            Log.d("Tanggal Laporan", dateLaporan.toString());
            Log.d("14 Hari Terakhir", date14HariTerakhir.toString());

            db.collection("users").document(userId).collection("tracking_data").whereLessThan("start_time", dateLaporan).
                    whereGreaterThan("start_time", date14HariTerakhir).orderBy("start_time", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            final Date lastDateRange = document.getTimestamp("end_time").toDate();
                            final Date firstDateRange = document.getTimestamp("start_time").toDate();
                            Log.d("Location ID", document.getString("place_id"));
                            Log.d("Last Time", document.getTimestamp("end_time").toDate().toString());
                            Log.d("Start Time", document.getTimestamp("start_time").toDate().toString());
                            final String place_id = document.getString("place_id");

                            db.collection("location").document(place_id).collection("tracking_data")
                                    .whereLessThan("start_time", lastDateRange).whereGreaterThan("start_time", firstDateRange).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if(TextUtils.equals(document.getString("user_id"), userId)==false && arrayDocumentID.contains(document.getId()) == false){
                                                arrayDocumentID.add(document.getId());
                                                arrayTimestampStart.add(document.getTimestamp("start_time"));
                                                arrayTimestampEnd.add(document.getTimestamp("end_time"));
                                                arrayUserID.add(document.getString("user_id"));
                                                arrayPlaceID.add(place_id);
                                            }
                                        }
                                        db.collection("location").document(place_id).collection("tracking_data")
                                                .whereLessThan("end_time", lastDateRange).whereGreaterThan("end_time", firstDateRange).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                if(task.isSuccessful()){
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        if(TextUtils.equals(document.getString("user_id"), userId)==false && arrayDocumentID.contains(document.getId()) == false){
                                                            arrayDocumentID.add(document.getId());
                                                            arrayUserID.add(document.getString("user_id"));
                                                            arrayPlaceID.add(place_id);
                                                        }
                                                    }

                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        }
                        handler=new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                final Map<String, Object> dataUpdateProfile = new HashMap<>();
                                dataUpdateProfile.put("CovidStatus", false);

                                db.collection("users").document(userIDRef).set(dataUpdateProfile, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("LOG","Updated User Positive Successfull");
                                        Log.d("User Data", arrayUserID.toString());
                                        Log.d("Place ID", arrayPlaceID.toString());
                                        Log.d("Start Time", arrayTimestampStart.toString());
                                        Log.d("End Time", arrayTimestampEnd.toString());
                                        for(int i=0; i< arrayPlaceID.size(); i ++){
                                            String documentNotificationID = Long.toString(Timestamp.now().getSeconds()) + arrayPlaceID.get(i);
                                            Map<String, Object> dataUpdateNotification= new HashMap<>();
                                            dataUpdateNotification.put("place_id", arrayPlaceID.get(i));
                                            dataUpdateNotification.put("start_time", arrayTimestampStart.get(i));
                                            dataUpdateNotification.put("end_time", arrayTimestampEnd.get(i));
                                            dataUpdateNotification.put("notification_type", "COVATRACE");
                                            db.collection("users").document(arrayUserID.get(i)).collection("notification").document(documentNotificationID)
                                                    .set(dataUpdateNotification, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d("LOG", "Notification Updated");
                                                }
                                            });
                                        }

                                        Map<String, Object> dataUpdateNotificationUserRef= new HashMap<>();
                                        dataUpdateNotificationUserRef.put("notification_type", "LAPORAN");
                                        dataUpdateNotificationUserRef.put("tanggal_laporan", tanggalLaporanNotification);
                                        dataUpdateNotificationUserRef.put("jenis_laporan", "Laporan Test COVID-19");
                                        dataUpdateNotificationUserRef.put("validity", true);
                                        String documentNotificationUID = Long.toString(Timestamp.now().getSeconds())+userIDRef;
                                        db.collection("users").document(userIDRef).collection("notification").document(documentNotificationUID)
                                                .set(dataUpdateNotificationUserRef, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d("LOG", "User Notification Updated!");
                                            }
                                        });

                                        handler3=new Handler();
                                        handler3.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                buttonHome.setVisibility(View.VISIBLE);
                                                loadingAnimation.setVisibility(View.GONE);
                                                doneAnimation.setVisibility(View.VISIBLE);
                                                loadingStatus.setText("Proses Berhasil");
                                                buttonHome.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent homeActivity = new Intent(HasilTestValidationActivity.this, MainActivity.class);
                                                        homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                        startActivity(homeActivity);
                                                    }
                                                });
                                            }
                                        },5000);
                                    }
                                    //Show Animation
                                });

                            }
                        },5000);
                    }
                }
            });
        }
        else{
            final Map<String, Object> dataUpdateProfile = new HashMap<>();
            dataUpdateProfile.put("CovidStatus", true);

            db.collection("users").document(userIDRef).set(dataUpdateProfile, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Map<String, Object> dataUpdateNotificationUserRef= new HashMap<>();
                    dataUpdateNotificationUserRef.put("notification_type", "LAPORAN");
                    dataUpdateNotificationUserRef.put("tanggal_laporan", tanggalLaporanNotification);
                    dataUpdateNotificationUserRef.put("jenis_laporan", "Laporan Test COVID-19");
                    dataUpdateNotificationUserRef.put("validity", true);
                    String documentNotificationUID = Long.toString(Timestamp.now().getSeconds())+userIDRef;
                    db.collection("users").document(userIDRef).collection("notification").document(documentNotificationUID)
                            .set(dataUpdateNotificationUserRef, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            handler3=new Handler();
                            handler3.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("LOG", "User Notification Updated Negative!");
                                    buttonHome.setVisibility(View.VISIBLE);
                                    loadingAnimation.setVisibility(View.GONE);
                                    doneAnimation.setVisibility(View.VISIBLE);
                                    loadingStatus.setText("Proses Berhasil");
                                    buttonHome.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent homeActivity = new Intent(HasilTestValidationActivity.this, MainActivity.class);
                                            homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(homeActivity);
                                        }
                                    });
                                }
                            },5000);
                        }
                    });
                }
            });

        }
    }
}