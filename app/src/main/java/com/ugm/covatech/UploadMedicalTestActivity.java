package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shuhart.stepview.StepView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadMedicalTestActivity extends AppCompatActivity {
    StepView stepView;
    Button buttonNext, buttonTakePicture, buttonUploadFromGallery;
    EditText editTextTanggalTest, editTextNamaLengkap, editTextNIK, editTextInstansiKesehatan;
    MaterialDatePicker datePicker;
    Calendar calendarTanggalTest;
    Date dateTanggalTest;
    CalendarConstraints dateLimit;
    AutoCompleteTextView jenisPemeriksaan, hasilPemeriksaan;
    ImageView documentImage;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    TextView buttonNext2, loadingStatus, textDescription, textLongDescription;
    CheckBox checkBoxPernyataan;
    LottieAnimationView lottieLoading, lottieDone;

    LinearLayout Layout1, Layout2, Layout3;

    Bitmap image;

    String currentPhotoPath;

    Handler handler;

    Button homeButton;


    static final int REQUEST_IMAGE_CAPTURE = 1;

    String valNamaLengkap, valNIK, valInstansiKesehatan, valJenisTest, valHasilTest;
    String varUserUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_medical_test);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, JENIS_TEST);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, HASIL_TEST);

        Layout1 = findViewById(R.id.layout_keterangan);
        Layout2 = findViewById(R.id.layout_document);
        Layout3 = findViewById(R.id.loading);

        lottieLoading = findViewById(R.id.loadingAnimation);
        lottieDone = findViewById(R.id.doneAnimation);

        loadingStatus = findViewById(R.id.loadingStatus);
        textDescription = findViewById(R.id.textDescription);
        textLongDescription = findViewById(R.id.textDescriptionLong);
        homeButton = findViewById(R.id.button_home);

        loadingStatus.setText("Mohon Tunggu");
        textDescription.setVisibility(View.GONE);
        textLongDescription.setVisibility(View.GONE);
        homeButton.setVisibility(View.GONE);

        Layout1.setVisibility(View.VISIBLE);
        Layout2.setVisibility(View.GONE);
        Layout3.setVisibility(View.GONE);

        storage = FirebaseStorage.getInstance();

        auth = FirebaseAuth.getInstance();
        varUserUID = auth.getUid();


        firestore = FirebaseFirestore.getInstance();

        checkBoxPernyataan = findViewById(R.id.checkbox_pernyataan);
        documentImage = findViewById(R.id.document);
        buttonTakePicture = findViewById(R.id.take_photo);
        buttonUploadFromGallery = findViewById(R.id.getFromGallery);

        buttonNext = findViewById(R.id.button_next);
        buttonNext2 = findViewById(R.id.button_next_form2);

        editTextTanggalTest = findViewById(R.id.edittext_tanggal_test);
        jenisPemeriksaan = findViewById(R.id.autoComplete_jenis_pemeriksaan);
        hasilPemeriksaan = findViewById(R.id.autoComplete_hasil_pemeriksaan);
        editTextNamaLengkap = findViewById(R.id.edittext_nama_lengkap);
        editTextNIK = findViewById(R.id.edittext_nik);
        editTextInstansiKesehatan = findViewById(R.id.edittext_tempat_test);

        calendarTanggalTest = Calendar.getInstance();

        jenisPemeriksaan.setAdapter(adapter);
        hasilPemeriksaan.setAdapter(adapter2);


        stepView = findViewById(R.id.step_view);
        stepView.getState()
                .animationType(StepView.ANIMATION_CIRCLE)
                .steps(new ArrayList<String>() {{
                    add("Keterangan");
                    add("Document");
                    add("Kirim");
                }})
                .stepsNumber(0)
                .animationDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .commit();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateDataStepKeterangan() == true) {
                    Layout1.setVisibility(View.GONE);
                    Layout2.setVisibility(View.VISIBLE);
                    stepView.go(stepView.getCurrentStep() + 1, true);
                }
            }
        });

        buttonNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image == null || !checkBoxPernyataan.isChecked()) {
                    Toast.makeText(UploadMedicalTestActivity.this, "Mohon Lengkapi Data", Toast.LENGTH_SHORT).show();
                } else {
                    Layout2.setVisibility(View.GONE);

                    Layout3.setVisibility(View.VISIBLE);
                    stepView.go(stepView.getCurrentStep() + 1, true);
                    handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadData();
                        }
                    },4000);
                }
            }
        });
        set_date_picker();
        setUploadStep();
    }

    private static final String[] JENIS_TEST = new String[]{
            "PCR", "Rapid Antigen", "Rapid Test Antibodi"
    };

    private static final String[] HASIL_TEST = new String[]{
            "Positif/Reaktif", "Negatif/Non-Reaktif"
    };

    public void set_date_picker() {
        editTextTanggalTest.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Tanggal Test").build();
                datePicker.show(getSupportFragmentManager(), "tag");
                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        calendarTanggalTest.setTimeInMillis(selection);
                        dateTanggalTest = calendarTanggalTest.getTime();
                        Log.d("Tanggal Test", dateTanggalTest.toString());

                        String pattern = "dd-MMMM-yyyy";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        String tanggal = simpleDateFormat.format(dateTanggalTest);
                        editTextTanggalTest.setText(tanggal);
                        editTextTanggalTest.setFocusableInTouchMode(false);
                        editTextTanggalTest.setFocusable(false);
                        editTextTanggalTest.setFocusableInTouchMode(true);
                        editTextTanggalTest.setFocusable(true);
                        datePicker.dismiss();

                    }
                });
            }
        });
    }

    public boolean validateDataStepKeterangan() {
        valNamaLengkap = editTextNamaLengkap.getText().toString();
        valNIK = editTextNIK.getText().toString();
        valInstansiKesehatan = editTextInstansiKesehatan.getText().toString();
        valJenisTest = jenisPemeriksaan.getText().toString();
        valHasilTest = hasilPemeriksaan.getText().toString();

        Log.d("Data Validasi", "");
        Log.d("Nama Lengkap : ", valNamaLengkap);
        Log.d("NIK", valNIK);
        Log.d("Jenis Test", valJenisTest);
        Log.d("Hasil Test", valHasilTest);

        if (valNamaLengkap.isEmpty() == true || valNIK.isEmpty() == true || valInstansiKesehatan.isEmpty() == true) {
            Toast.makeText(UploadMedicalTestActivity.this, "Mohon Lengkapi Data!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (valHasilTest.isEmpty() == true || valJenisTest.isEmpty() == true) {
            Toast.makeText(UploadMedicalTestActivity.this, "Mohon Lengkapi Data!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dateTanggalTest == null) {
            Toast.makeText(UploadMedicalTestActivity.this, "Mohon Lengkapi Data!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (valNIK.length() != 16) {
            Toast.makeText(UploadMedicalTestActivity.this, "Pastikan NIK Terdiri 16 Digit Angka!", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    public void setUploadStep() {
        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(UploadMedicalTestActivity.this,
                                "com.ugm.covatech.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                    }
                }
            }
        });
    }


    public void uploadData() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image = BitmapFactory.decodeFile(currentPhotoPath);
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        StorageReference mStorageRef = storage.getReference();
        final String collection_id = Long.toString(Timestamp.now().getSeconds()) + varUserUID;

        StorageReference imageRef = mStorageRef.child("data_lapor_test/" + collection_id + "/" + "document");

        byte[] b = stream.toByteArray();

        imageRef.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUri = uri;
                        Log.d("Download Link", downloadUri.toString());
                        DocumentReference documentReferenceLaporan = firestore.collection("laporan_test").
                                document(collection_id);

                        final Map<String, Object> dataTest = new HashMap<>();

                        dataTest.put("user_id", varUserUID);
                        dataTest.put("nik", valNIK);
                        dataTest.put("instansi_kesehatan", valInstansiKesehatan);
                        dataTest.put("jenis_test", valJenisTest);
                        dataTest.put("hasil_test", valHasilTest);
                        dataTest.put("tanggal_test", dateTanggalTest);
                        dataTest.put("tanggal_kirim_laporan", Timestamp.now());
                        dataTest.put("document_link", downloadUri.toString());
                        dataTest.put("validation_status", false);
                        dataTest.put("document_validity", false);

                        documentReferenceLaporan.set(dataTest).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                stepView.go(stepView.getCurrentStep() + 1, true);
                                lottieLoading.setVisibility(View.GONE);
                                lottieDone.setVisibility(View.VISIBLE);
                                loadingStatus.setText("Success");
                                textDescription.setVisibility(View.VISIBLE);
                                textLongDescription.setVisibility(View.VISIBLE);
                                homeButton.setVisibility(View.VISIBLE);
                                homeButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent homeActivity = new Intent(UploadMedicalTestActivity.this, MainActivity.class);
                                        homeActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                        startActivity(homeActivity);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        Toast.makeText(UploadMedicalTestActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            olahGambar();

        }
    }

    private void olahGambar() {
        int targetW = documentImage.getWidth();
        int targetH = documentImage.getHeight();
        Log.i("Resolution", String.valueOf(targetW));
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        image = bitmap;
        documentImage.setImageBitmap(RotateBitmap(bitmap,90));

    }
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}