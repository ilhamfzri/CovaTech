package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CovaTributeMain extends AppCompatActivity {

    private ShimmerFrameLayout mShimmerViewContainer;

    RecyclerView recyclerView;
    Handler handler;
    AdapterCovaTribute adapterCovaTribute;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    Boolean loadLocationState = false;
    float place_rating = (float) 0.0;
    String sUserFullname;
    int i;

    LinearLayout notAvailableLayout, availableLayout;

    final ArrayList<String> arrayNamePlace = new ArrayList<String>();
    final ArrayList<String> arrayPlaceID = new ArrayList<String>();
    final ArrayList<String> arrayAddress = new ArrayList<String>();
    final ArrayList<String> arrayDocumentUID = new ArrayList<String>();


    ArrayList<Float> arrayRating = new ArrayList<Float>();
    ArrayList<Float> dump = new ArrayList<Float>();

    MaterialToolbar topBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cova_tribute_main);

        topBar = findViewById(R.id.topAppBar);

        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backActivity = new Intent(CovaTributeMain.this, MainActivity.class);
                backActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(backActivity);
            }
        });

        notAvailableLayout = findViewById(R.id.layoutNotAvailable);
        availableLayout = findViewById(R.id.layoutAvailable);

        availableLayout.setVisibility(View.VISIBLE);
        notAvailableLayout.setVisibility(View.GONE);

        arrayRating.add((float) 0.0);
        arrayRating.add((float) 0.0);
        arrayRating.add((float) 0.0);

        sUserFullname = getIntent().getStringExtra("user_fullname");

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mShimmerViewContainer = findViewById(R.id.shimmerFrameLayout);
        mShimmerViewContainer.startShimmerAnimation();

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setViewLastLocation();
            }
        },1000);

    }


    public void setViewLastLocation() {

        String userUID = firebaseAuth.getUid();
        db.collection("users").document(userUID).collection("tracking_data").orderBy("start_time", Query.Direction.DESCENDING).limit(3)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Cek Apakah Sudah di Review
                        if(document.getBoolean("review_state")==false){
                            Log.d("Place", document.getString("place_name"));

                            Timestamp startTime = document.getTimestamp("start_time");
                            Timestamp endTime = document.getTimestamp("end_time");


                            String pattern = "d MMMM yyyy, HH:mm a";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                            Date dateStartTime = startTime.toDate();
                            String sStartTime = simpleDateFormat.format(dateStartTime);

                            arrayPlaceID.add(document.getString("place_id"));
                            arrayNamePlace.add(document.getString("place_name"));
                            arrayAddress.add("Dikunjungi : " + sStartTime);
                            arrayDocumentUID.add(document.getId());
                        }
                    }
                }
                loadLocationState = true;
                Log.d("STATE", loadLocationState.toString());
                if(arrayPlaceID.size()>0){
                    getPlaceRating();
                }
                else{
                    notAvailableLayout.setVisibility(View.VISIBLE);
                    availableLayout.setVisibility(View.GONE);
                }
            }
        });
    }


    public void getPlaceRating(){
        for(i=0; i<arrayPlaceID.size(); i++){
            final String place_id = arrayPlaceID.get(i);
            db.collection("location").document(place_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    int index = i;
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            Log.d("Document ID", document.getId());
                            Log.d("Index", Integer.toString(index));
                            Double total_star = document.getDouble("total_star");
                            Double total_user_review = document.getDouble("total_user_review");
                            if(total_star!=null && total_user_review!=null){
                                place_rating = total_star.floatValue()/total_user_review.floatValue();
                                arrayRating.set(arrayPlaceID.indexOf(place_id), place_rating);
                            }
                            Log.d("ArrayRating", arrayRating.toString() + dump.toString());
                            dump.add((float) 0.0);
                        }
                        else{
                            arrayRating.set(index, (float) 0.0);
                            dump.add((float) 0.0);
                        }
                    }
                    if(dump.size()==arrayPlaceID.size()){
                        setRecyclerView();
                    }
                }
            });
        }
    }

    public void setRecyclerView(){
        final String[] stringLocationName = arrayNamePlace.toArray(new String[0]);
        final String[] stringAddress = arrayAddress.toArray(new String[0]);
        final Float[] floatRating = arrayRating.toArray(new Float[0]);


        adapterCovaTribute = new AdapterCovaTribute(CovaTributeMain.this, stringLocationName, stringAddress, floatRating, new AdapterCovaTribute.ClickListener() {
            @Override
            public void onPositionClicked(int position) {
                Log.d("Place ID Clicked", arrayPlaceID.get(position));
                String place_id_next = arrayPlaceID.get(position);
                String place_name_next = arrayNamePlace.get(position);
                String document_uid_next = arrayDocumentUID.get(position);

                Intent reviewIntent = new Intent(CovaTributeMain.this, ReviewPlaceActivity.class);
                reviewIntent.putExtra("document_uid", document_uid_next);
                reviewIntent.putExtra("place_name", place_name_next);
                reviewIntent.putExtra("place_id", place_id_next);
                reviewIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(reviewIntent);
            }
        });

        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapterCovaTribute);
        recyclerView.setLayoutManager(new LinearLayoutManager(CovaTributeMain.this));
    }

    public void onBackPressed(){
        Intent backActivity = new Intent(CovaTributeMain.this, MainActivity.class);
        backActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(backActivity);
    }
}
