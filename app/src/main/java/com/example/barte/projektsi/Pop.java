package com.example.barte.projektsi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by barte on 14.05.2017.
 */

public class Pop extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popwindow);

        final Button bZapisz = (Button) findViewById(R.id.bZapisz);
        final EditText etZnak = (EditText) findViewById(R.id.etZnak);
        final ImageView ivSymbol = (ImageView) findViewById(R.id.ivSymbol);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        Intent getIntent = getIntent();
        /*pozycja wypisywanego znaku*/
        final int x = getIntent.getIntExtra("x", -1);
        final int y = getIntent.getIntExtra("y", -1);
        final int rectangleNumber = getIntent.getIntExtra("rectangleNumber", -1);
        final Bitmap croppedSymbol = (Bitmap) getIntent.getParcelableExtra("croppedSymbol");

        ivSymbol.setImageBitmap(croppedSymbol);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.8), (int)(height*.9));

        bZapisz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String znak = etZnak.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("znak", znak);
                intent.putExtra("x",x);
                intent.putExtra("y",y);
                intent.putExtra("rectangleNumber", rectangleNumber);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
    }
}
