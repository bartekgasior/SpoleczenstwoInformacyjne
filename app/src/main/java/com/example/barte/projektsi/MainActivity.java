package com.example.barte.projektsi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    static{
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OPENCV not loaded");
        } else{
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button bKamera = (Button) findViewById(R.id.bKamera);

        bKamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent kameraIntent = new Intent(MainActivity.this, KameraActivity.class);
                MainActivity.this.startActivity(kameraIntent);
            }
        });
    }
}
