package com.example.john.contrainttest;

import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.LocationManager;

public class MainActivity extends AppCompatActivity {
    final double[] topLeft = {39.510926, -84.733827};
    final double[] topRight = {39.510920, -84.733218};
    final double[] botLeft = {39.510614, -84.733829};
    final double[] botRight = {39.510607, -84.733220};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager location = (LocationManager) getSystemService(LOCATION_SERVICE);

        Button button1 = findViewById(R.id.Button2); // Down
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iv = findViewById(R.id.imageView);
                TextView up = findViewById(R.id.textView2);
                if (up.getText().equals("1")){
                    up.setText("0");
                    iv.setImageResource(R.drawable.benton0);
                } else if (up.getText().equals("2")){
                    up.setText("1");
                    iv.setImageResource(R.drawable.benton1);
                }
                // Code here executes on main thread after user presses button
            }
        });
        Button button2 = findViewById(R.id.Button1); // Up
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView iv = findViewById(R.id.imageView);
                TextView up = findViewById(R.id.textView2);
                if (up.getText().equals("1")){
                    up.setText("2");
                    iv.setImageResource(R.drawable.benton2);
                } else if (up.getText().equals("0")){
                    up.setText("1");
                    iv.setImageResource(R.drawable.benton1);
                }
                // Code here executes on main thread after user presses button
            }
        });
    }


}
