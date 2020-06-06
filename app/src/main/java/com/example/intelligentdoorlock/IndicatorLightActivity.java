package com.example.intelligentdoorlock;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class IndicatorLightActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.indicator_light_introduction);
        Toolbar toolbar = findViewById(R.id.toolbar_indicator);
        setSupportActionBar(toolbar);
    }
}
