package com.example.intelligentdoorlock;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FaceControlActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_control);
        Toolbar toolbar = findViewById(R.id.toolbar_face_control);
        setSupportActionBar(toolbar);

    }
}
