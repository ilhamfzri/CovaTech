package com.ugm.covatech;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CovatraceActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AdapterCovaTrace adapterCovaTrace;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    Calendar paginationCalendarCurrent, paginationCalendarOffset;
    TextView paginationText;
    Integer dateOffset=0;
    Date paginationDate;
    Button paginationBefore, paginationAfter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covatrace);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //Get Current Calendar
        paginationCalendarCurrent = Calendar.getInstance();

        paginationAfter = findViewById(R.id.paginationAfter);
        paginationBefore = findViewById(R.id.paginationBefore);
        paginationText = findViewById(R.id.paginationText);

        setPagination();
    }

    public void setPagination(){
        updatePaginationBar();
        paginationAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateOffset = dateOffset + 1;
                updatePaginationBar();
            }
        });

        paginationBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateOffset = dateOffset - 1;
                updatePaginationBar();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public  void updatePaginationBar(){
        paginationCalendarOffset = Calendar.getInstance();
        paginationCalendarOffset.add(Calendar.DAY_OF_MONTH, dateOffset);
        paginationDate = paginationCalendarOffset.getTime();
        String df = DateFormat.getDateInstance(DateFormat.LONG).format(paginationDate);


        //Set Pagination Button Enable/Disable
        if(dateOffset==0){
            paginationAfter.setEnabled(false);
        }
        else if(dateOffset==-14){
            paginationBefore.setEnabled(false);
        }
        else{
            paginationAfter.setEnabled(true);
            paginationBefore.setEnabled(true);
        }

        //Set Pagination Text
        if(dateOffset==0){
            paginationText.setText("Hari Ini");
        }
        else{

            paginationText.setText(df);
        }
    }

//    public void updateRecycleView(){
//        final String[] stringLocationName = {"Universitas Gudang Mantu"};
//        final String[] stringTimeRange = {"2.31 AM - 25.21 PM"};
//        adapterCovaTrace = new AdapterCovaTrace(CovatraceActivity.this, )
//    }


}