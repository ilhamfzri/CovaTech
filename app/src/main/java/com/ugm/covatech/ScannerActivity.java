package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.google.zxing.WriterException;

import java.security.Permission;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import pub.devrel.easypermissions.EasyPermissions;

public class ScannerActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    FirebaseFirestore db;
    QRGEncoder qrgEncoder;
    Bitmap bitmap;
    ImageView qrImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ContextCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(ScannerActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 2);
        }
        setContentView(R.layout.activity_scanner);
        db = FirebaseFirestore.getInstance();
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ScannerActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                        DocumentReference docRef = db.collection("users").document(result.getText());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {

                                        final TextView textViewEmail, textViewName, textViewStatusVaksin, textViewStatusCovid;
                                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ScannerActivity.this, R.style.BottomSheetDialogTheme);

                                        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                                                .inflate(R.layout.layout_bottom_sheet_fasilitas_scan_profile, (LinearLayout) findViewById(R.id.bottomSheetContainer));

                                        textViewEmail = bottomSheetView.findViewById(R.id.textEmail);
                                        textViewName = bottomSheetView.findViewById(R.id.textName);
                                        textViewStatusVaksin = bottomSheetView.findViewById(R.id.textStatusVaksin);
                                        textViewStatusCovid = bottomSheetView.findViewById(R.id.textStatusCovid);
                                        qrImage = bottomSheetView.findViewById(R.id.qr_image);

                                        Log.d("User Data", document.getData().toString());

                                        textViewEmail.setText(document.getString("Email"));
                                        textViewName.setText(document.getString("Name"));

                                        //VaksinStatus = True => Sudah di Vaksin
                                        Color color;
                                        if(document.getBoolean("VaksinStatus") == true){
                                            textViewStatusVaksin.setText("Sudah");
                                            textViewStatusVaksin.setTextColor(Color.GREEN);
                                        }
                                        else{
                                            textViewStatusVaksin.setText("Belum");
                                            textViewStatusVaksin.setTextColor(Color.RED);
                                        }

                                        //CovidStatus = True => Positif
                                        if(document.getBoolean("CovidStatus") == true){
                                            textViewStatusCovid.setText("Negatif");
                                            textViewStatusCovid.setTextColor(Color.GREEN);
                                        }
                                        else{
                                            textViewStatusCovid.setText("Positif");
                                            textViewStatusCovid.setTextColor(Color.RED);
                                        }

                                        QRCodeGenerator(result.getText());
                                        bottomSheetDialog.setContentView(bottomSheetView);
                                        bottomSheetDialog.show();


                                    } else {
                                        Log.d("Failed", "No such document");
                                    }
                                } else {

                                }
                            }
                        });

                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    public void QRCodeGenerator(String UID){

        Point point = new Point(30,30);
        int width = point.x;
        int height = point.y;
        int dimen = width < height ? width : height;
        dimen = dimen * 12;
        qrgEncoder = new QRGEncoder(UID, null, QRGContents.Type.TEXT, dimen);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }

    }
}