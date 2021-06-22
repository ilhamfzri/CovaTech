package com.ugm.covatech;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SplashScreen4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen4);
    }

    public void setNext(View view) {
        Intent nextActivity = new Intent(this, LoginActivity.class);
//        nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(nextActivity, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        overridePendingTransition(0,0);
    }
}