package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CovaTributeMain extends AppCompatActivity {

    RecyclerView recyclerView;
    AdapterCovaTribute adapterCovaTribute;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cova_tribute_main);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        setViewLastLocation();
    }

    public void setViewLastLocation(){

        final ArrayList<String> arrayNamePlace = new ArrayList<String>();
        final ArrayList<String> arrayAddress = new ArrayList<String>();
        final ArrayList<Float> arrayRating = new ArrayList<Float>();
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

                        arrayNamePlace.add(document.getString("place_name"));
                        arrayAddress.add(sStartTime + " - " + sEndTime);
                        arrayRating.add((float) 2.5);
                    }
                    final String[] stringLocationName = arrayNamePlace.toArray(new String[0]);
                    final String[] stringAddress = arrayAddress.toArray(new String[0]);
                    final Float[] floatRating = arrayRating.toArray(new Float[0]);

                    adapterCovaTribute = new AdapterCovaTribute(CovaTributeMain.this, stringLocationName, stringAddress, floatRating, new AdapterCovaTribute.ClickListener() {
                        @Override
                        public void onPositionClicked(int position) {
                            Log.d("Clicked", "Click");
                        }
                    });
                    recyclerView = findViewById(R.id.recyclerView);
                    recyclerView.setAdapter(adapterCovaTribute);
                    recyclerView.setLayoutManager(new LinearLayoutManager(CovaTributeMain.this));
                }
            }
        });
    }
}