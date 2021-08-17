package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;


import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ReviewPlaceActivity extends AppCompatActivity {
    int valFasilitasYes = 0, valFasilitasNo = 0;
    int valSocialYes = 0, valSocialNo = 0;
    int valMaskYes = 0, valMaskNo = 0;
    int valRating = 0;
    String valUlasan;

    LottieAnimationView loadingAnimation;
    LottieAnimationView doneAnimation;

    TextView loadingStatus, loadingDescription;

    LinearLayout mainReview;
    LinearLayout loadingBox;

    FirebaseAuth fAuth;
    Button reviewButton, doneButton;
    TextView textPlaceName;
    RadioButton rbFasilitasYes, rbFasilitasNo, rbSocialYes, rbSocialNo, rbMaskNo, rbMaskYes;
    MaterialRatingBar mRating;
    EditText editTextReview;
    String sPlaceName, sPlaceID, sDocumentUID;


    Handler handler;
    FirebaseFirestore fStore;
    Animation animFadeIn, animFadeOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_place);

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);

        loadingAnimation = findViewById(R.id.loadingAnimation);
        doneAnimation = findViewById(R.id.doneAnimation);



        doneAnimation.setVisibility(View.GONE);

        loadingStatus = findViewById(R.id.loadingStatus);
        loadingStatus.setText("Mohon Tunggu");

        loadingDescription = findViewById(R.id.textDescription);
        loadingDescription.setVisibility(View.GONE);

        doneButton = findViewById(R.id.button_home);
        doneButton.setVisibility(View.GONE);


        mainReview = findViewById(R.id.menu_review);
        loadingBox = findViewById(R.id.loading);

        mainReview.setVisibility(View.VISIBLE);
        loadingBox.setVisibility(View.GONE);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        sPlaceName = getIntent().getStringExtra("place_name");
        sPlaceID = getIntent().getStringExtra("place_id");
        sDocumentUID = getIntent().getStringExtra("document_uid");

        Log.d("Place ID", sPlaceID);

        textPlaceName = findViewById(R.id.placeName);
        textPlaceName.setText(sPlaceName);

        rbFasilitasYes = findViewById(R.id.FasilitasRadioYes);
        rbFasilitasNo = findViewById(R.id.FasilitasRadionNo);

        rbSocialYes = findViewById(R.id.SocialRadioYes);
        rbSocialNo = findViewById(R.id.SocialRadioNo);

        rbMaskYes = findViewById(R.id.MaskRadioYes);
        rbMaskNo = findViewById(R.id.MasRadioNo);

        mRating = findViewById(R.id.ratingBar);
        mRating.getRating();

        editTextReview = findViewById(R.id.reviewBox);

        reviewButton = findViewById(R.id.button);

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = validationAnswer();
                if(state==true){
                    mainReview.setVisibility(View.GONE);
                    loadingBox.setVisibility(View.VISIBLE);

                    handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendToDatabase();
                        }
                    },4000);


                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent covaTributeActivity = new Intent(ReviewPlaceActivity.this, CovaTributeMain.class);
        covaTributeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(covaTributeActivity);
    }

    public boolean validationAnswer(){

        //Check Fasilitas Radio
        if(rbFasilitasYes.isChecked()==false && rbFasilitasNo.isChecked()==false){
            Toast.makeText(ReviewPlaceActivity.this, "Lengkapi Data!",Toast.LENGTH_LONG).show();
            return false;
        }
        else{


            if(rbFasilitasYes.isChecked()==true){
                valFasilitasYes = 1;
                valFasilitasNo = 0;
            }
            else{
                valFasilitasYes = 0;
                valFasilitasNo = 1;
            }
        }

        //Check SocialDistancing Radio
        if(rbSocialYes.isChecked()==false && rbSocialNo.isChecked()==false){
            Toast.makeText(ReviewPlaceActivity.this, "Lengkapi Data!",Toast.LENGTH_LONG).show();
            return false;
        }
        else{

            if(rbSocialYes.isChecked()==true){
                valSocialYes = 1;
                valSocialNo = 0;
            }
            else{
                valSocialYes = 0;
                valSocialNo = 1;
            }
        }

        //Check Mask Radio
        if(rbMaskYes.isChecked()==false && rbMaskNo.isChecked()==false){
            Toast.makeText(ReviewPlaceActivity.this, "Lengkapi Data!",Toast.LENGTH_LONG).show();
            return false;
        }
        else{

            if(rbMaskYes.isChecked()==true){
                valMaskYes = 1;
                valMaskNo = 0;
            }
            else{
                valMaskYes = 0;
                valMaskNo = 1;
            }
        }

        if(mRating.getRating()==0.0){
            Toast.makeText(ReviewPlaceActivity.this, "Masukan Rating Tempat",Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            valRating = Math.round(mRating.getRating());
        }

        if(editTextReview.length()<50){
            Toast.makeText(ReviewPlaceActivity.this, "Ulasan Minimal 50 Karakter",Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            valUlasan = editTextReview.getText().toString();
        }

        Log.d("Info", "Data Tervalidasi");
        Log.d("Fasilitas Yes :",Integer.toString(valFasilitasYes));
        Log.d("Fasilitas No :",Integer.toString(valFasilitasNo));
        Log.d("Social Yes :",Integer.toString(valSocialYes));
        Log.d("Social No :",Integer.toString(valSocialNo));
        Log.d("Mask Yes :",Integer.toString(valMaskYes));
        Log.d("Mask No :",Integer.toString(valMaskNo));
        Log.d("Rating Value :", Integer.toString(valRating));
        Log.d("Ulasan :", valUlasan);
        Log.d("Timestamp : ", Long.toString(Timestamp.now().getSeconds()));

        return true;
    }

    public void sendToDatabase(){

        DocumentReference documentReference = fStore.collection("location").document(sPlaceID);

        //Retrieve Current Location Data
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        int total_start = document.getDouble("total_star").intValue();
                        int total_user_review = document.getDouble("total_user_review").intValue();
                        int fasilitas_yes = document.getDouble("fasilitas_yes").intValue();
                        int fasilitas_no = document.getDouble("fasilitas_no").intValue();
                        int social_yes = document.getDouble("social_yes").intValue();
                        int social_no = document.getDouble("social_no").intValue();
                        int mask_yes = document.getDouble("mask_yes").intValue();
                        int mask_no = document.getDouble("mask_no").intValue();
                        write_to_db(total_start, total_user_review, fasilitas_yes, fasilitas_no, social_yes, social_no, mask_yes, mask_no);
                    }
                    else{
                        write_to_db(0,0,0,0,0,0,0,0);
                    }
                }
            }
        });
    }

    public void write_to_db(int total_start, int total_user_review, int fasilitas_yes, int fasilitas_no, int social_yes,
                            int social_no, int mask_yes, int mask_no) {
        Map<String, Object> dataReview = new HashMap<>();
        dataReview.put("total_star", total_start + valRating);
        dataReview.put("total_user_review", total_user_review + 1);
        dataReview.put("fasilitas_yes", fasilitas_yes + valFasilitasYes);
        dataReview.put("fasilitas_no", fasilitas_no + valFasilitasNo);
        dataReview.put("social_yes", social_yes + valSocialYes);
        dataReview.put("social_no", social_no + valSocialNo);
        dataReview.put("mask_yes", mask_yes + valMaskYes);
        dataReview.put("mask_no", mask_no + valMaskNo);

        DocumentReference documentReference = fStore.collection("location").document(sPlaceID);
        documentReference.set(dataReview).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                //Get UserFullname
                final String userID = fAuth.getUid();
                DocumentReference docRef = fStore.collection("users").document(userID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            String userFullName = document.getString("Name");

                            //Save Ulasan to Database
                            String collection_id = Long.toString(Timestamp.now().getSeconds());
                            DocumentReference documentReferenceUlasan = fStore.collection("location").
                                    document(sPlaceID).collection("ulasan").document(collection_id);

                            final Map<String, Object> dataUlasan = new HashMap<>();
                            dataUlasan.put("user_name", userFullName);
                            dataUlasan.put("ulasan", valUlasan);
                            dataUlasan.put("date", Timestamp.now());


                            documentReferenceUlasan.set(dataUlasan).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    DocumentReference documentReferenceUID = fStore.collection("users").
                                            document(userID).collection("tracking_data").document(sDocumentUID);

                                    Map<String, Object> dataUpdate = new HashMap<>();
                                    dataUpdate.put("review_state", true);

                                    documentReferenceUID.set(dataUpdate, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("INFO", "Data Sucessfully Saved to Database");
                                            loadingStatus.startAnimation(animFadeOut);
                                            loadingStatus.setText("Success");
                                            loadingStatus.startAnimation(animFadeIn);
                                            loadingAnimation.setVisibility(View.GONE);
                                            doneAnimation.setVisibility(View.VISIBLE);
                                            loadingDescription.setVisibility(View.VISIBLE);
                                            doneButton.setVisibility(View.VISIBLE);
                                            doneButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent homeActivity = new Intent(ReviewPlaceActivity.this, MainActivity.class);
                                                    homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                    startActivity(homeActivity);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                });


            }
        });

    }
}