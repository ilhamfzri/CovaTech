package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CovatraceActivity extends AppCompatActivity {

    private ShimmerFrameLayout mShimmerViewContainer;

    RecyclerView recyclerView;
    AdapterCovaTrace adapterCovaTrace;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    Calendar paginationCalendarCurrent, paginationCalendarOffset;
    TextView paginationText;
    Integer dateOffset = 0;
    Date paginationDate;
    Button paginationBefore, paginationAfter;
    LinearLayout not_available_layout;

    Animation animFadeIn, animFadeOut;

    Handler handler;
    MaterialToolbar topBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_covatrace);

        topBar = findViewById(R.id.topAppBar);

        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backActivity = new Intent(CovatraceActivity.this, MainActivity.class);
                backActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(backActivity);
            }
        });

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);

        not_available_layout = findViewById(R.id.not_available_layout);

        mShimmerViewContainer = findViewById(R.id.shimmerFrameLayout);

        handler = new Handler();
        //Get Current Calendar
        paginationCalendarCurrent = Calendar.getInstance();

        paginationAfter = findViewById(R.id.paginationAfter);
        paginationAfter.setEnabled(false);
        paginationBefore = findViewById(R.id.paginationBefore);
        paginationText = findViewById(R.id.paginationText);

        setPagination();
    }

    public void setPagination() {

        not_available_layout.setVisibility(View.GONE);

        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmerAnimation();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updatePaginationBar();
            }
        }, 1000);

        paginationAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                not_available_layout.clearAnimation();
                not_available_layout.setVisibility(View.GONE);

                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmerAnimation();
                dateOffset = dateOffset + 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updatePaginationBar();
                    }
                }, 1000);

            }
        });

        paginationBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                not_available_layout.clearAnimation();
                not_available_layout.setVisibility(View.GONE);

                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmerAnimation();
                dateOffset = dateOffset - 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updatePaginationBar();
                    }
                }, 1000);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void updatePaginationBar() {

        paginationCalendarOffset = Calendar.getInstance();
        paginationCalendarOffset.add(Calendar.DAY_OF_MONTH, dateOffset);
        paginationDate = paginationCalendarOffset.getTime();
        String df = DateFormat.getDateInstance(DateFormat.LONG).format(paginationDate);


        //Set Pagination Button Enable/Disable
        if (dateOffset == 0) {
            paginationAfter.setEnabled(false);
        } else if (dateOffset == -14) {
            paginationBefore.setEnabled(false);
        } else {
            paginationAfter.setEnabled(true);
            paginationBefore.setEnabled(true);
        }

        //Set Pagination Text
        if (dateOffset == 0) {
            paginationText.setText("Hari Ini");
        } else {
            paginationText.setText(df);
        }

        updateRecycleView();
    }

    public void updateRecycleView() {
        final ArrayList<String> arrayNamePlace = new ArrayList<String>();
        final ArrayList<String> arrayRangeTime = new ArrayList<String>();

        int mm = paginationCalendarOffset.get(Calendar.MONTH);
        int dd = paginationCalendarOffset.get(Calendar.DAY_OF_MONTH);
        int yy = paginationCalendarOffset.get(Calendar.YEAR);

        Log.d("Month", Integer.toString(mm));
        Log.d("Day", Integer.toString(dd));
        Log.d("Year", Integer.toString(yy));

        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.set(yy, mm, dd, 0, 0, 0);
        endCalendar.set(yy, mm, dd + 1, 0, 0, 0);

        Date startDate = startCalendar.getTime();
        Date endDate = endCalendar.getTime();
        Log.d("Start Date", startDate.toString());
        Log.d("End Date", endDate.toString());

        String userUID = firebaseAuth.getUid();
        db.collection("users").document(userUID).collection("tracking_data").whereLessThan("start_time", endDate).
                whereGreaterThan("start_time", startDate).orderBy("start_time", Query.Direction.DESCENDING).get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Place", document.getString("place_name"));

                                Timestamp startTime = document.getTimestamp("start_time");
                                Timestamp endTime = document.getTimestamp("end_time");


                                String pattern = "hh:mm a";
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                                Date dateStartTime = startTime.toDate();
                                Date dateEndTime = endTime.toDate();

                                String sStartTime = simpleDateFormat.format(dateStartTime);
                                String sEndTime = simpleDateFormat.format(dateEndTime);

                                arrayNamePlace.add(document.getString("place_name"));
                                arrayRangeTime.add(sStartTime + " - " + sEndTime);
                            }
                            final String[] stringLocationName = arrayNamePlace.toArray(new String[0]);
                            final String[] stringTimeRange = arrayRangeTime.toArray(new String[0]);

                            if (arrayNamePlace.size() == 0) {
                                not_available_layout.setVisibility(View.VISIBLE);
                                not_available_layout.startAnimation(animFadeIn);
                            } else {
                                adapterCovaTrace = new AdapterCovaTrace(CovatraceActivity.this, stringLocationName, stringTimeRange, new AdapterCovaTrace.ClickListener() {
                                    @Override
                                    public void onPositionClicked(int position) {
                                        Log.d("Clicked", "Click");
                                    }
                                });

                                mShimmerViewContainer.setVisibility(View.GONE);
                                mShimmerViewContainer.stopShimmerAnimation();
                                recyclerView = findViewById(R.id.recyclerView);
                                recyclerView.setAdapter(adapterCovaTrace);
                                recyclerView.setLayoutManager(new LinearLayoutManager(CovatraceActivity.this));

                            }
                        }
                    }
                });


    }


}