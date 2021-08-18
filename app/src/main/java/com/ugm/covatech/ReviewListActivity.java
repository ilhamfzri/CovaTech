package com.ugm.covatech;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ReviewListActivity extends AppCompatActivity {
    Button dummyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
        dummyButton = findViewById(R.id.dummyButton);
        dummyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextActivity = new Intent(ReviewListActivity.this, ReviewResultActivity.class);
                nextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(nextActivity);
            }
        });
    }
}