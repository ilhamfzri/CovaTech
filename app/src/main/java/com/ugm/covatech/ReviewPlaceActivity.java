package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Time;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    MaterialToolbar topBar;

    Handler handler;
    FirebaseFirestore fStore;
    Animation animFadeIn, animFadeOut;

    ImageView imageLocation;
    Bitmap photoLocation;
    FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_place);

        storage = FirebaseStorage.getInstance();

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);

        topBar = findViewById(R.id.topAppBar);

        topBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backActivity = new Intent(ReviewPlaceActivity.this, CovaTributeMain.class);
                backActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(backActivity);
            }
        });

        loadingAnimation = findViewById(R.id.loadingAnimation);
        doneAnimation = findViewById(R.id.doneAnimation);


        doneAnimation.setVisibility(View.GONE);

        loadingStatus = findViewById(R.id.loadingStatus);
        loadingStatus.setText("Mohon Tunggu");

        imageLocation = findViewById(R.id.image_place);

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
                if (state == true) {
                    mainReview.setVisibility(View.GONE);
                    loadingBox.setVisibility(View.VISIBLE);

                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendToDatabase();
                        }
                    }, 4000);
                }
            }
        });
        loadImage();
    }

    public void loadImage() {
        DocumentReference documentReference = fStore.collection("location").document(sPlaceID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d("LOG", "Document Exist!");
                    String photo_url = document.getString("place_photo_url");
                    if (TextUtils.isEmpty(photo_url)) {
                        Log.d("LOG", "load from places api !");
                        Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
                        final PlacesClient placesClient = Places.createClient(ReviewPlaceActivity.this);

                        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG);
                        final FetchPlaceRequest request = FetchPlaceRequest.builder(sPlaceID, placeFields).build();

                        placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                            @Override
                            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                Log.d("Data Places", fetchPlaceResponse.getPlace().toString());
                                Place placesData = fetchPlaceResponse.getPlace();
                                final List<PhotoMetadata> metadata = placesData.getPhotoMetadatas();
                                final PhotoMetadata photoMetadata = metadata.get(0);

                                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
                                placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                                    @Override
                                    public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                                        photoLocation = fetchPhotoResponse.getBitmap();
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        photoLocation.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                        StorageReference mStorageRef = storage.getReference();
                                        StorageReference imageRef = mStorageRef.child("location/" + sPlaceID);
                                        byte[] b = stream.toByteArray();
                                        imageRef.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final Uri downloadUri = uri;
                                                        final Map<String, Object> dataReview = new HashMap<>();
                                                        dataReview.put("place_photo_url", downloadUri.toString());
                                                        DocumentReference documentReference = fStore.collection("location").document(sPlaceID);
                                                        documentReference.set(dataReview, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Glide.with(ReviewPlaceActivity.this).load(downloadUri.toString()).into(imageLocation);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                            }
                        });
                    } else {
                        Log.d("LOG", "Use cache data!");
                        Glide.with(ReviewPlaceActivity.this).load(photo_url).into(imageLocation);

                    }
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

    public boolean validationAnswer() {

        //Check Fasilitas Radio
        if (rbFasilitasYes.isChecked() == false && rbFasilitasNo.isChecked() == false) {
            Toast.makeText(ReviewPlaceActivity.this, "Lengkapi Data!", Toast.LENGTH_LONG).show();
            return false;
        } else {


            if (rbFasilitasYes.isChecked() == true) {
                valFasilitasYes = 1;
                valFasilitasNo = 0;
            } else {
                valFasilitasYes = 0;
                valFasilitasNo = 1;
            }
        }

        //Check SocialDistancing Radio
        if (rbSocialYes.isChecked() == false && rbSocialNo.isChecked() == false) {
            Toast.makeText(ReviewPlaceActivity.this, "Lengkapi Data!", Toast.LENGTH_LONG).show();
            return false;
        } else {

            if (rbSocialYes.isChecked() == true) {
                valSocialYes = 1;
                valSocialNo = 0;
            } else {
                valSocialYes = 0;
                valSocialNo = 1;
            }
        }

        //Check Mask Radio
        if (rbMaskYes.isChecked() == false && rbMaskNo.isChecked() == false) {
            Toast.makeText(ReviewPlaceActivity.this, "Lengkapi Data!", Toast.LENGTH_LONG).show();
            return false;
        } else {

            if (rbMaskYes.isChecked() == true) {
                valMaskYes = 1;
                valMaskNo = 0;
            } else {
                valMaskYes = 0;
                valMaskNo = 1;
            }
        }

        if (mRating.getRating() == 0.0) {
            Toast.makeText(ReviewPlaceActivity.this, "Masukan Rating Tempat", Toast.LENGTH_LONG).show();
            return false;
        } else {
            valRating = Math.round(mRating.getRating());
        }

        if (editTextReview.length() < 50) {
            Toast.makeText(ReviewPlaceActivity.this, "Ulasan Minimal 50 Karakter", Toast.LENGTH_LONG).show();
            return false;
        } else {
            valUlasan = editTextReview.getText().toString();
        }

        Log.d("Info", "Data Tervalidasi");
        Log.d("Fasilitas Yes :", Integer.toString(valFasilitasYes));
        Log.d("Fasilitas No :", Integer.toString(valFasilitasNo));
        Log.d("Social Yes :", Integer.toString(valSocialYes));
        Log.d("Social No :", Integer.toString(valSocialNo));
        Log.d("Mask Yes :", Integer.toString(valMaskYes));
        Log.d("Mask No :", Integer.toString(valMaskNo));
        Log.d("Rating Value :", Integer.toString(valRating));
        Log.d("Ulasan :", valUlasan);
        Log.d("Timestamp : ", Long.toString(Timestamp.now().getSeconds()));

        return true;
    }

    public void sendToDatabase() {

        DocumentReference documentReference = fStore.collection("location").document(sPlaceID);

        //Retrieve Current Location Data
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.getDouble("total_star")!=null) {
                        int total_start = document.getDouble("total_star").intValue();
                        int total_user_review = document.getDouble("total_user_review").intValue();
                        int fasilitas_yes = document.getDouble("fasilitas_yes").intValue();
                        int fasilitas_no = document.getDouble("fasilitas_no").intValue();
                        int social_yes = document.getDouble("social_yes").intValue();
                        int social_no = document.getDouble("social_no").intValue();
                        int mask_yes = document.getDouble("mask_yes").intValue();
                        int mask_no = document.getDouble("mask_no").intValue();
                        write_to_db(total_start, total_user_review, fasilitas_yes, fasilitas_no, social_yes, social_no, mask_yes, mask_no, false);
                    } else {
                        write_to_db(0, 0, 0, 0, 0, 0, 0, 0, true);
                    }
                }
            }
        });
    }

    public void write_to_db(int total_start, int total_user_review, int fasilitas_yes, int fasilitas_no, int social_yes,
                            int social_no, int mask_yes, int mask_no, boolean state_new_document) {
        final Map<String, Object> dataReview = new HashMap<>();
        dataReview.put("total_star", total_start + valRating);
        dataReview.put("total_user_review", total_user_review + 1);
        dataReview.put("fasilitas_yes", fasilitas_yes + valFasilitasYes);
        dataReview.put("fasilitas_no", fasilitas_no + valFasilitasNo);
        dataReview.put("social_yes", social_yes + valSocialYes);
        dataReview.put("social_no", social_no + valSocialNo);
        dataReview.put("mask_yes", mask_yes + valMaskYes);
        dataReview.put("mask_no", mask_no + valMaskNo);

        if (state_new_document == true) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.api_key));
            final PlacesClient placesClient = Places.createClient(this);

            final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG);
            final FetchPlaceRequest request = FetchPlaceRequest.builder(sPlaceID, placeFields).build();

            placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                @Override
                public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                    Log.d("Data Places", fetchPlaceResponse.getPlace().toString());
                    Place placesData = fetchPlaceResponse.getPlace();
                    dataReview.put("place_name", placesData.getName());
                    dataReview.put("place_address", placesData.getAddress());
                    GeoPoint locationGeopoint = new GeoPoint(placesData.getLatLng().latitude, placesData.getLatLng().longitude);
                    dataReview.put("place_location", locationGeopoint);
                    write_to_document(dataReview);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Log.d("LOG", e.toString());
                }
            });
        } else {
            write_to_document(dataReview);
        }

    }

    public void write_to_document(Map<String, Object> dataReview) {
        DocumentReference documentReference = fStore.collection("location").document(sPlaceID);
        documentReference.set(dataReview, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                //Get UserFullname
                final String userID = fAuth.getUid();
                DocumentReference docRef = fStore.collection("users").document(userID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
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