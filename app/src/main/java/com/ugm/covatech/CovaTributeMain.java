package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    final ArrayList<String> arrayNamePlace = new ArrayList<String>();
    final ArrayList<String> arrayPlaceID = new ArrayList<String>();
    final ArrayList<String> arrayAddress = new ArrayList<String>();
    final ArrayList<Float> arrayRating = new ArrayList<Float>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cova_tribute_main);

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
                        Log.d("Place", document.getString("place_name"));

                        Timestamp startTime = document.getTimestamp("start_time");
                        Timestamp endTime = document.getTimestamp("end_time");


                        String pattern = "hh:mm a";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

                        Date dateStartTime = startTime.toDate();
                        Date dateEndTime = endTime.toDate();

                        String sStartTime = simpleDateFormat.format(dateStartTime);
                        String sEndTime = simpleDateFormat.format(dateEndTime);

                        arrayPlaceID.add(document.getString("place_id"));
                        arrayNamePlace.add(document.getString("place_name"));
                        arrayAddress.add(sStartTime + " - " + sEndTime);
                    }
                }
                loadLocationState = true;
                Log.d("STATE", loadLocationState.toString());
                getPlaceRating();
            }
        });
    }


    public void getPlaceRating(){
        for(int i=0; i<arrayPlaceID.size(); i++){
            String place_id = arrayPlaceID.get(i);
            db.collection("location").document(place_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            Double total_star = document.getDouble("total_star");
                            Double total_user_review = document.getDouble("total_user_review");
                            if(total_star!=null && total_user_review!=null){
                                place_rating = total_star.floatValue()/total_user_review.floatValue();
                                arrayRating.add(place_rating);
                            }
                            else{
                                arrayRating.add((float) 0.0);
                            }
                        }
                        else{
                            arrayRating.add((float) 0.0);
                        }
                    }
                    if(arrayRating.size()==arrayPlaceID.size()){
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
                Log.d("Clicked", "Click");
            }
        });

        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapterCovaTribute);
        recyclerView.setLayoutManager(new LinearLayoutManager(CovaTributeMain.this));
    }



}
