package com.ugm.covatech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.shuhart.stepview.StepView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class UploadMedicalTestActivity extends AppCompatActivity {
    StepView stepView;
    Button buttonNext,buttonTakePicture, buttonUploadFromGallery;
    EditText editTextTanggalTest, editTextNamaLengkap, editTextNIK, editTextInstansiKesehatan;
    MaterialDatePicker datePicker;
    Calendar calendarTanggalTest;
    Date dateTanggalTest;
    CalendarConstraints dateLimit;
    AutoCompleteTextView jenisPemeriksaan, hasilPemeriksaan;
    ImageView documentImage;

    String valNamaLengkap, valNIK, valInstansiKesehatan, valJenisTest, valHasilTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_medical_test);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, JENIS_TEST);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, HASIL_TEST);


        documentImage = findViewById(R.id.document);
        buttonTakePicture = findViewById(R.id.take_photo);
        buttonUploadFromGallery = findViewById(R.id.getFromGallery);

        buttonNext = findViewById(R.id.button_next);
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
                if(validateDataStepKeterangan()==true){
                    stepView.go(stepView.getCurrentStep()+1,true);
                }
            }
        });

        set_date_picker();

        setUploadStep();
    }

    private static final String[] JENIS_TEST = new String[] {
            "PCR", "Rapid Antigen", "Rapid Test Antibodi"
    };

    private static final String[] HASIL_TEST = new String[] {
            "Positif/Reaktif", "Negatif/Non-Reaktif"
    };

    public void set_date_picker(){
        editTextTanggalTest.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Tanggal Test").build();
                datePicker.show(getSupportFragmentManager(),"tag");
                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override public void onPositiveButtonClick(Long selection) {
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

    public boolean validateDataStepKeterangan (){
        valNamaLengkap = editTextNamaLengkap.getText().toString();
        valNIK = editTextNIK.getText().toString();
        valInstansiKesehatan = editTextInstansiKesehatan.getText().toString();
        valJenisTest = jenisPemeriksaan.getText().toString();
        valHasilTest = hasilPemeriksaan.getText().toString();

        Log.d("Data Validasi","");
        Log.d("Nama Lengkap : ", valNamaLengkap);
        Log.d("NIK",valNIK);
        Log.d("Jenis Test", valJenisTest);
        Log.d("Hasil Test", valHasilTest);

        if(valNamaLengkap.isEmpty()==true || valNIK.isEmpty()==true || valInstansiKesehatan.isEmpty()==true ) {
            Toast.makeText(UploadMedicalTestActivity.this, "Mohon Lengkapi Data!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(valHasilTest.isEmpty()==true || valJenisTest.isEmpty()==true){
            Toast.makeText(UploadMedicalTestActivity.this, "Mohon Lengkapi Data!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(dateTanggalTest == null){
            Toast.makeText(UploadMedicalTestActivity.this, "Mohon Lengkapi Data!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(valNIK.length()!=16){
            Toast.makeText(UploadMedicalTestActivity.this, "Pastikan NIK Terdiri 16 Digit Angka!", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    public void setUploadStep(){
        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            }
        });

        buttonUploadFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        documentImage.setImageBitmap(selectedImage);
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();


                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);

                                Bitmap bitmap;
                                bitmap = BitmapFactory.decodeFile(picturePath);
                                int width = UploadMedicalTestActivity.this.getResources().getDisplayMetrics().widthPixels;
                                int height = (width*bitmap.getHeight())/bitmap.getWidth();
                                Bitmap new_bitmap;
                                new_bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                                documentImage.setImageBitmap(new_bitmap);
                                documentImage.setBackground(UploadMedicalTestActivity.this.getDrawable(R.drawable.ic_box_border));
                                cursor.close();
                            }
                        }

                    }
                    break;
            }
        }
    }
}