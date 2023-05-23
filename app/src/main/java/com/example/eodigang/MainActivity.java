package com.example.eodigang;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button feedback_btn, empty_class_btn, timetable_btn;
        empty_class_btn = findViewById(R.id.empty_class);
        feedback_btn = findViewById(R.id.feedback);
        timetable_btn = findViewById(R.id.timetable);

        feedback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, feedback.class);
                startActivity(intent);
            }
        });

        empty_class_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, empty_class.class);
                startActivity(intent);
            }
        });

        timetable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, timetable.class);
                startActivity(intent);
            }
        });

    }
}