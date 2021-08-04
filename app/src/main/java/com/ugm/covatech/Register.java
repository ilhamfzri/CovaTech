package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    EditText mName, mEmail, mPassword1, mPassword2;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        fAuth = FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        mName = findViewById(R.id.editName);
        mEmail = findViewById(R.id.editEmail);
        mPassword1 = findViewById(R.id.editPassword);
        mPassword2 = findViewById(R.id.editPassword2);
    }

    public static boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public void setRegister(View view){
        final String vEmail = mEmail.getText().toString().trim();
        final String vName = mName.getText().toString().trim();
        final String vPassword= mPassword1.getText().toString().trim();
        final String vPassword2 = mPassword2.getText().toString().trim();

        //Check Email Validity
        if(isValid(vEmail)==false){
            mEmail.setError("Masukan email yang benar!");

            return;
        }

        //Check Name
        if(TextUtils.isEmpty(vName)){
            mName.setError("Masukan Nama anda!");
            return;
        }

        //Check Password length, if below 6 character return error
        if(vPassword.length()<6 || vPassword2.length()<6){
            Toast.makeText(Register.this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Check Password
        if(vPassword.equals(vPassword2)==false){
            Toast.makeText(Register.this, "Password yang anda masukan berbeda dengan password konfirmasi!", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.createUserWithEmailAndPassword(vEmail, vPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = fAuth.getCurrentUser();
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(Register.this, "Email Verifikasi Telah Dikirim!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                String userID = fAuth.getCurrentUser().getUid();
                DocumentReference documentReference = fStore.collection("users").document(userID);

                // Save user data to firestore
                Map<String, Object> userData = new HashMap<>();
                userData.put("Name", vName);
                userData.put("Email", vEmail);
                documentReference.set(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fAuth.signOut();
                        Intent loginActivity = new Intent(Register.this, LoginActivity.class);
                        startActivity(loginActivity, ActivityOptions.makeSceneTransitionAnimation(Register.this).toBundle());
                    }
                });
            }



        });
    }

    public void setLogin(View view){
        Intent loginActivity = new Intent(Register.this, LoginActivity.class);
        startActivity(loginActivity, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}