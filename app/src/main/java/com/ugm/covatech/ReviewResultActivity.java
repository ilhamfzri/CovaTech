package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.axes.Linear;
import com.anychart.core.cartesian.series.Bar;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.LabelsOverlapMode;
import com.anychart.enums.Orientation;
import com.anychart.enums.ScaleStackMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.enums.TooltipPositionMode;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ReviewResultActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    TextView textViewPlaceName, textViewPlaceAddress, textViewCuciTanganYes, textViewCuciTanganNo, textViewSocialDistancingYes, textViewSocialDistancingNo, textViewMaskerYes, textViewMaskerNo, textViewReviewer, textViewKomentar, textViewTanggalKomentar;
    MaterialRatingBar ratingPlaces;
    ImageView imagePlace;
    FirebaseStorage storage;
    MaterialButton buttonForward, buttonBack;
    int ulasanSize = -1;
    int ulasanPosition = 0;

    final ArrayList<String> arrayReviewer = new ArrayList<String>();
    final ArrayList<String> arrayKomentar = new ArrayList<String>();
    final ArrayList<String> arrayTanggal = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_result);

        final String placeID = getIntent().getStringExtra("place_id");

        buttonBack = findViewById(R.id.button_back);
        buttonForward = findViewById(R.id.button_forward);
        buttonBack.setEnabled(false);
        buttonForward.setEnabled(true);

        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        ratingPlaces = findViewById(R.id.starBar);
        imagePlace = findViewById(R.id.image_place);
        textViewPlaceName = findViewById(R.id.textview_place_name);
        textViewPlaceAddress = findViewById(R.id.textview_place_address);
        textViewCuciTanganYes = findViewById(R.id.textview_cucitangan_yes);
        textViewCuciTanganNo = findViewById(R.id.textview_cucitangan_no);
        textViewSocialDistancingYes = findViewById(R.id.textview_socialdistancing_yes);
        textViewSocialDistancingNo = findViewById(R.id.textview_socialdistancing_no);
        textViewMaskerYes = findViewById(R.id.textview_masker_yes);
        textViewMaskerNo = findViewById(R.id.textview_masker_no);
        textViewReviewer = findViewById(R.id.textview_reviewer);
        textViewKomentar = findViewById(R.id.textview_komentar);
        textViewTanggalKomentar = findViewById(R.id.textview_tanggal_komentar);

        DocumentReference documentReference = firestore.collection("location").document(placeID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        int total_start = document.getDouble("total_star").intValue();
                        int total_user_review = document.getDouble("total_user_review").intValue();
                        int fasilitas_yes = document.getDouble("fasilitas_yes").intValue();
                        int fasilitas_no = document.getDouble("fasilitas_no").intValue();
                        int social_yes = document.getDouble("social_yes").intValue();
                        int social_no = document.getDouble("social_no").intValue();
                        int mask_yes = document.getDouble("mask_yes").intValue();
                        int mask_no = document.getDouble("mask_no").intValue();

                        ratingPlaces.setRating(total_start / total_user_review);
                        textViewPlaceName.setText(document.getString("place_name"));
                        ;
                        textViewPlaceAddress.setText(document.getString("place_address"));
                        textViewCuciTanganYes.setText(Integer.toString(fasilitas_yes));
                        textViewCuciTanganNo.setText(Integer.toString(fasilitas_no));
                        textViewSocialDistancingYes.setText(Integer.toString(social_yes));
                        textViewSocialDistancingNo.setText(Integer.toString(social_no));
                        textViewMaskerYes.setText(Integer.toString(mask_yes));
                        textViewMaskerNo.setText(Integer.toString(mask_no));

                        StorageReference storageRef = storage.getReference().child("location")
                                .child(placeID);
                        storageRef.getBytes(1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                imagePlace.setImageBitmap(bitmap);
                            }
                        });
                        firestore.collection("location").document(placeID).collection("ulasan")
                                .orderBy("date", Query.Direction.DESCENDING).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                Timestamp timestamp = document.getTimestamp("date");
                                                Date date = timestamp.toDate();

                                                String pattern = "d MMMM yyyy";
                                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                                                String stringTanggal = simpleDateFormat.format(date);

                                                Log.d("document_id", document.getId());
                                                Log.d("user_name", document.getString("user_name"));
                                                Log.d("ulasan", document.getString("ulasan"));
                                                Log.d("date", stringTanggal);

                                                arrayReviewer.add(document.getString("user_name"));
                                                arrayKomentar.add(document.getString("ulasan"));
                                                arrayTanggal.add(stringTanggal);
                                            }
                                            ulasanSize = arrayReviewer.size();
                                            showUlasanData(ulasanPosition);
                                            checkButton();
                                            ulasanController();
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    public void ulasanController() {

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ulasanPosition = ulasanPosition - 1;
                showUlasanData(ulasanPosition);
                checkButton();
            }
        });

        buttonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ulasanPosition = ulasanPosition + 1;
                showUlasanData(ulasanPosition);
                checkButton();
            }
        });
}

    public void showUlasanData(int position) {
        textViewReviewer.setText(arrayReviewer.get(position));
        textViewKomentar.setText(arrayKomentar.get(position));
        textViewTanggalKomentar.setText(arrayTanggal.get(position));
    }

    public void checkButton() {
        Log.d("ulasan_size", Integer.toString(ulasanSize));
        Log.d("ulasan_position", Integer.toString(ulasanPosition));
        if (ulasanPosition == 0) {
            buttonBack.setEnabled(false);
        } else {
            buttonBack.setEnabled(true);
        }
        if (ulasanPosition == ulasanSize - 1) {
            buttonForward.setEnabled(false);
        } else {
            buttonForward.setEnabled(true);
        }

    }


}