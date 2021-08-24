package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VacineValidationActivity extends AppCompatActivity {

    String sDocumentUID;
    String userIDRef;
    FirebaseFirestore db;
    EditText editTextNamaLengkap, editTextNIK, editTextInstansiKesehatan, editTextTanggalVaksinasi, editTextJenisPemeriksaan, editTextHasilPemeriksaan;
    MaterialButton buttonShowDocument;
    CheckBox checkBox;
    TextView buttonTolakLaporan, buttonTerimaLaporan;
    Handler handler, handler3;
    String tanggalLaporanNotification;
    LinearLayout mainLayout, loadingLayout;
    LottieAnimationView loadingAnimation, doneAnimation;
    TextView loadingStatus;
    Button buttonHome;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacine_validation);
        db = FirebaseFirestore.getInstance();
        sDocumentUID = getIntent().getStringExtra("document_uid");
        setContentView(R.layout.activity_vacine_validation);
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
        editTextTanggalVaksinasi = findViewById(R.id.edittext_tanggal_vaksin);
        editTextJenisPemeriksaan = findViewById(R.id.edittext_jenis_vaksin);

        editTextNamaLengkap.setFocusable(false);
        editTextNIK.setFocusable(false);
        editTextInstansiKesehatan.setFocusable(false);
        editTextTanggalVaksinasi.setFocusable(false);
        editTextJenisPemeriksaan.setFocusable(false);
        loadData();

    }

    public void loadData(){
        db.collection("laporan_vaksin").document(sDocumentUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                        editTextTanggalVaksinasi.setText(stringTanggalLaporan);
                        editTextJenisPemeriksaan.setText(document.getString("jenis_test"));
                        userIDRef = document.getString("user_id");

                        buttonShowDocument.setEnabled(true);
                        buttonShowDocument.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView documentImage;
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(VacineValidationActivity.this, R.style.BottomSheetDialogTheme);
                                View bottomSheetView = LayoutInflater.from(getApplicationContext())
                                        .inflate(R.layout.layout_bottom_sheet_show_document, (LinearLayout) findViewById(R.id.bottomSheetContainer));
                                documentImage = bottomSheetView.findViewById(R.id.image);
                                Glide.with(VacineValidationActivity.this).load(document.getString("document_link")).into(documentImage);
                                bottomSheetDialog.setContentView(bottomSheetView);
                                bottomSheetDialog.show();
                            }
                        });

                        buttonTerimaLaporan.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("TAG", "HERE");
                                if(checkBox.isChecked()==false){
                                    Toast.makeText(VacineValidationActivity.this, "Mohon Centang Pernyataan!", Toast.LENGTH_SHORT).show();
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
                                    DocumentReference documentRefValidation = db.collection("laporan_vaksin").document(sDocumentUID);
                                    documentRefValidation.set(dataValidasi, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            setTerimaLaporan();
                                        }
                                    });
                                }
                            }
                        });

                        buttonTolakLaporan.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(checkBox.isChecked()==false){
                                    Toast.makeText(VacineValidationActivity.this, "Mohon Centang Pernyataan!", Toast.LENGTH_SHORT).show();
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
        DocumentReference documentRefValidation = db.collection("laporan_vaksin").document(sDocumentUID);
        documentRefValidation.set(dataValidasi, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("LOG", "Data Berhasil di Tolak!");
                Map<String, Object> dataUpdateNotificationUserRef= new HashMap<>();
                dataUpdateNotificationUserRef.put("notification_time", Timestamp.now());
                dataUpdateNotificationUserRef.put("notification_type", "LAPORAN");
                dataUpdateNotificationUserRef.put("tanggal_laporan", tanggalLaporanNotification);
                dataUpdateNotificationUserRef.put("jenis_laporan", "Laporan Vaksin");
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
                                        Intent homeActivity = new Intent(VacineValidationActivity.this, MainActivity.class);
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

    public void setTerimaLaporan(){
        final Map<String, Object> dataUpdateProfile = new HashMap<>();
        dataUpdateProfile.put("VaksinStatus", true);

        db.collection("users").document(userIDRef).set(dataUpdateProfile, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                final Map<String, Object> dataValidasi = new HashMap<>();
                dataValidasi.put("validation_status", true);
                dataValidasi.put("document_validity", true);
                DocumentReference documentRefValidation = db.collection("laporan_vaksin").document(sDocumentUID);
                documentRefValidation.set(dataValidasi, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("LOG", "Data Berhasil di Terima!");
                        Map<String, Object> dataUpdateNotificationUserRef= new HashMap<>();
                        dataUpdateNotificationUserRef.put("notification_time", Timestamp.now());
                        dataUpdateNotificationUserRef.put("notification_type", "LAPORAN");
                        dataUpdateNotificationUserRef.put("tanggal_laporan", tanggalLaporanNotification);
                        dataUpdateNotificationUserRef.put("jenis_laporan", "Laporan Vaksin");
                        dataUpdateNotificationUserRef.put("validity", true);
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
                                                Intent homeActivity = new Intent(VacineValidationActivity.this, MainActivity.class);
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
        });

    }
}