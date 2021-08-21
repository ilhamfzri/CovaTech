package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    Button buttonAdminValidation;

    CardView cardViewLaporTest, cardViewLaporVaksinasi;
    FloatingActionButton scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNavigationView();

        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cardViewLaporTest = findViewById(R.id.card_lapor_test);
        cardViewLaporVaksinasi = findViewById(R.id.card_lapor_vaksinasi);

        buttonAdminValidation = findViewById(R.id.button_admin);

        scanButton = findViewById(R.id.addFab);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivity = new Intent(ProfileActivity.this, ScannerActivity.class);
                nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(nextActivity);
            }
        });

        cardViewLaporTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeActivity = new Intent(ProfileActivity.this, UploadMedicalTestActivity.class);
                homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(homeActivity);
            }
        });

        cardViewLaporVaksinasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeActivity = new Intent(ProfileActivity.this, UploadVaccineTestActivity.class);
                homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(homeActivity);
            }
        });

        buttonAdminValidation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adminActivity = new Intent(ProfileActivity.this, AdminValidationActivity.class);
                adminActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(adminActivity);
            }
        });

        loadProfileCard();


    }

    public void loadProfileCard(){
        final TextView mEmail, mName, mStatusCovid, mStatusVaksin;
        mEmail = findViewById(R.id.textEmail);
        mName = findViewById(R.id.textName);
        mStatusCovid = findViewById(R.id.textStatusCovid);
        mStatusVaksin = findViewById(R.id.textStatusVaksin);

        //Get User ID
        final String userID = fAuth.getUid();

        // Get User Data
        DocumentReference docRef = db.collection("users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("User Data", document.getData().toString());

                        mEmail.setText(document.getString("Email"));
                        mName.setText(document.getString("Name"));

                        //VaksinStatus = True => Sudah di Vaksin
                        Color color;
                        if(document.getBoolean("VaksinStatus") == true){
                            mStatusVaksin.setText("Sudah");
                            mStatusVaksin.setTextColor(Color.GREEN);
                        }
                        else{
                            mStatusVaksin.setText("Belum");
                            mStatusVaksin.setTextColor(Color.RED);
                        }

                        //CovidStatus = True => Positif
                        if(document.getBoolean("CovidStatus") == true){
                            mStatusCovid.setText("Negatif");
                            mStatusCovid.setTextColor(Color.GREEN);
                        }
                        else{
                            mStatusCovid.setText("Positif");
                            mStatusCovid.setTextColor(Color.RED);
                        }

                        QRCodeGenerator(userID);



                    } else {
                        Log.d("Failed", "No such document");
                    }
                } else {

                }
            }
        });

    }

    public void QRCodeGenerator(String UID){
        ImageView qrImage = findViewById(R.id.qr_image);
        Point point = new Point(30,30);
        int width = point.x;
        int height = point.y;
        int dimen = width < height ? width : height;
        dimen = dimen * 12;
        qrgEncoder = new QRGEncoder(UID, null, QRGContents.Type.TEXT, dimen);
        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder.encodeAsBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }
        final String userID = UID;
        qrImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQRCodeBottomSheet(userID);
            }
        });

    }

    public void showQRCodeBottomSheet(String UID){
        ImageView qrImageBig;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottom_sheet_qrcode, (LinearLayout) findViewById(R.id.bottomSheetContainer));

        qrImageBig = bottomSheetView.findViewById(R.id.qr_big);

        Point point = new Point(30,30);
        int width = point.x;
        int height = point.y;
        int dimen = width < height ? width : height;
        dimen = dimen * 12;
        qrgEncoder = new QRGEncoder(UID, null, QRGContents.Type.TEXT, dimen);
        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder.encodeAsBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            qrImageBig.setImageBitmap(bitmap);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        } catch (WriterException e) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.profil);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        Intent nextActivity =  new Intent(ProfileActivity.this, MainActivity.class);
                        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(nextActivity);
                        overridePendingTransition(0, 0);
                }
                return true;
            }
        });
    }
}