package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class AdminValidationActivity extends AppCompatActivity {

    TabLayout tabLayout;
    FirebaseFirestore db;
    RecyclerView recyclerViewTest;
    MaterialToolbar topBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_validation);
        recyclerViewTest = findViewById(R.id.recyclerView_test);
        db = FirebaseFirestore.getInstance();
        tabLayout = findViewById(R.id.tablayout);
        setViewTest();
        topBar = findViewById(R.id.topAppBar);

        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backActivity = new Intent(AdminValidationActivity.this, MainActivity.class);
                backActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(backActivity);
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override

            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("Tab Status", Integer.toString(tab.getPosition()));
                int tabPosition = tab.getPosition();
                if(tabPosition==0){
                    setViewTest();
                }
                else{
                    setViewVaksin();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void setViewTest(){
        final ArrayList<String> arrayLaporanID= new ArrayList<String>();
        final ArrayList<String> arrayUserName= new ArrayList<String>();
        final ArrayList<String> arrayTanggalLaporan= new ArrayList<String>();

        db.collection("laporan_test").orderBy("tanggal_kirim_laporan", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Cek Apakah Sudah di Review

                        if (document.getBoolean("validation_status")==false) {
                            String pattern = "d MMMM yyyy";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                            Timestamp timestampLaporan = document.getTimestamp("tanggal_kirim_laporan");
                            Date dateLaporan = timestampLaporan.toDate();
                            String stringTanggalLaporan = simpleDateFormat.format(dateLaporan);

                            arrayLaporanID.add(document.getId());
                            arrayUserName.add(document.getString("user_id"));
                            arrayTanggalLaporan.add(stringTanggalLaporan);
                        }
                    }
                    final String[] stringsLaporanID = arrayLaporanID.toArray(new String[0]);
                    final String[] stringsUserName = arrayUserName.toArray(new String[0]);
                    final String[] stringsTanggalLaporan = arrayTanggalLaporan.toArray(new String[0]);

                    AdapterValidasiData adapterValidasiData = new AdapterValidasiData(AdminValidationActivity.this, stringsLaporanID, stringsUserName, stringsTanggalLaporan, new AdapterValidasiData.ClickListener() {
                        @Override
                        public void onPositionClicked(int position) {
                            Log.d("Document ID Clicked : ", arrayLaporanID.get(position));
                            Intent hasilTestIntent = new Intent(AdminValidationActivity.this, HasilTestValidationActivity.class);
                            hasilTestIntent.putExtra("document_uid", arrayLaporanID.get(position));
                            hasilTestIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(hasilTestIntent);
                        }
                    });
                    recyclerViewTest.setAdapter(adapterValidasiData);
                    recyclerViewTest.setLayoutManager(new LinearLayoutManager(AdminValidationActivity.this));
                }
            }
        });

    }

    public void setViewVaksin(){
        final ArrayList<String> arrayLaporanID= new ArrayList<String>();
        final ArrayList<String> arrayUserName= new ArrayList<String>();
        final ArrayList<String> arrayTanggalLaporan= new ArrayList<String>();

        db.collection("laporan_vaksin").orderBy("tanggal_kirim_laporan", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Cek Apakah Sudah di Review

                        if (document.getBoolean("validation_status")==false) {
                            String pattern = "d MMMM yyyy";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                            Timestamp timestampLaporan = document.getTimestamp("tanggal_kirim_laporan");
                            Date dateLaporan = timestampLaporan.toDate();
                            String stringTanggalLaporan = simpleDateFormat.format(dateLaporan);

                            arrayLaporanID.add(document.getId());
                            arrayUserName.add(document.getString("user_id"));
                            arrayTanggalLaporan.add(stringTanggalLaporan);
                        }
                    }
                    final String[] stringsLaporanID = arrayLaporanID.toArray(new String[0]);
                    final String[] stringsUserName = arrayUserName.toArray(new String[0]);
                    final String[] stringsTanggalLaporan = arrayTanggalLaporan.toArray(new String[0]);

                    AdapterValidasiData adapterValidasiData = new AdapterValidasiData(AdminValidationActivity.this, stringsLaporanID, stringsUserName, stringsTanggalLaporan, new AdapterValidasiData.ClickListener() {
                        @Override
                        public void onPositionClicked(int position) {
                            Log.d("Document ID Clicked : ", arrayLaporanID.get(position));
                            Intent hasilTestIntent = new Intent(AdminValidationActivity.this, VacineValidationActivity.class);
                            hasilTestIntent.putExtra("document_uid", arrayLaporanID.get(position));
                            hasilTestIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(hasilTestIntent);
                        }
                    });
                    recyclerViewTest.setAdapter(adapterValidasiData);
                    recyclerViewTest.setLayoutManager(new LinearLayoutManager(AdminValidationActivity.this));
                }
            }
        });
    }
}