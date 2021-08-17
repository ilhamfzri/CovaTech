package com.ugm.covatech;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    EditText mEmail, mPassword;
    FirebaseAuth fAuth;
    TextView textLupa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.editEmail);
        mPassword = findViewById(R.id.editPassword);
        textLupa = findViewById(R.id.lupa_password);

        fAuth = FirebaseAuth.getInstance();

        textLupa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetEmail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Masukan Email Anda Untuk Menerima Link Reset");
                passwordResetDialog.setView(resetEmail);
                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetEmail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Link Reset Password Telah Dikirim.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error! Reset Link Tidak Terkirim", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                passwordResetDialog.show();
            }
        });

    }

    public void setRegister(View view) {
        Intent nextActivity = new Intent(this, Register.class);
//        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(nextActivity, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        overridePendingTransition(0,0);
    }

    public void setLogin(View view) {
        String vEmail = mEmail.getText().toString();
        String vPassword = mPassword.getText().toString();
        if (TextUtils.isEmpty(vPassword)) {
            Toast.makeText(LoginActivity.this, "Lengkapi email dan password!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(vEmail)) {
            Toast.makeText(LoginActivity.this, "Lengkapi email dan password!", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.signInWithEmailAndPassword(vEmail, vPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = fAuth.getCurrentUser();
                    if (!user.isEmailVerified()) {
                        fAuth.signOut();
                        Toast.makeText(LoginActivity.this, "Cek Email dan Mohon Verifikasi Akun Anda!", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(mainActivity, ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this).toBundle());
                    }
                } else {
                    mEmail.setError("Email atau password anda salah!");
                    mPassword.setError("Email atau password anda salah!");
                    Toast.makeText(LoginActivity.this, "Login Gagal, Email atau Password Salah!!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

    }

}