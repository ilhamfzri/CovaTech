package com.ugm.covatech;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;

public class SplashScreen1 extends AppCompatActivity {
    Handler handler;
    FirebaseAuth fAuth;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen1);

        fAuth=FirebaseAuth.getInstance();
        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fAuth.getCurrentUser() != null) {
                    intent= new Intent(SplashScreen1.this, MainActivity.class);
                }
                else{
                    intent= new Intent(SplashScreen1.this, SplashScreen2.class);
                }
                startActivity(intent);
                finish();
            }
        },3000);

    }
}