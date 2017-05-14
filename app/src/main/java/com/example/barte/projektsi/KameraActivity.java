package com.example.barte.projektsi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class KameraActivity extends AppCompatActivity{

    private static final String TAG = "KameraActivity";
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private int coordinatesCounter = 0;
    private int maxCoordinates = 4;
    private Point[] coordinates = new Point[4];
    private Mat gray = new Mat();
    private Canvas canvas;
    private String znak ="";
    private int boundingRectangleThicnkess = 15;
    private TextView[] textViewArray;

    List<Rect> bounding_rect = new ArrayList<Rect>();
    Rect rect;
    String mCurrentPhotoPath;
    Mat img;
    ImageView showPhoto;
    RelativeLayout layout;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_kamera);

        Log.v("projektSI", "create, znak: " + znak);

        showPhoto = (ImageView) findViewById(R.id.mat);
        layout = (RelativeLayout) findViewById(R.id.layout);

        dispatchTakePictureIntent();

        //showPhoto.setOnTouchListener(this);
        showPhoto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    /*if (coordinatesCounter < maxCoordinates) {
                        coordinates[coordinatesCounter] = new Point(event.getX(), event.getY());
                        coordinatesCounter++;
                        Toast.makeText(getApplicationContext(), "Touch coordinates : " + String.valueOf(event.getX()) + "x" + String.valueOf(event.getY()) +
                                        "\n" + coordinatesCounter + "/" + maxCoordinates,
                                Toast.LENGTH_SHORT).show();
                    }*/

                    for(int i =0; i< bounding_rect.size();i++) {
                        /*Log.v("projektSI","szerokosc: " + showPhoto.getWidth() + "wysokosc: " + showPhoto.getHeight());
                        Log.v("projektSI","rect[" + i + "} -> " + bounding_rect.get(i).x  + " " + bounding_rect.get(i).y );
                        Log.v("projektSI","kliknięto: " + event.getX() + " " + event.getY());*/
                        if ( bounding_rect.get(i).x < event.getX() && event.getX() < (bounding_rect.get(i).x + bounding_rect.get(i).width) && bounding_rect.get(i).y < event.getY() && event.getY() < (bounding_rect.get(i).y + bounding_rect.get(i).height)) {
                            Toast.makeText(getApplicationContext(), "kliknięto "+i+" obiekt", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(KameraActivity.this, Pop.class);
                            intent.putExtra("x", bounding_rect.get(i).x);
                            intent.putExtra("y", bounding_rect.get(i).y);
                            intent.putExtra("rectangleNumber", i);
                            Log.v("projektSI", "x - " + bounding_rect.get(i).x + " y - " + bounding_rect.get(i).y);
                            startActivityForResult(intent, 0);
                        }
                    }
                }
                return true;
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File imgFile = new File(mCurrentPhotoPath);

            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                img = new Mat(myBitmap.getWidth(), myBitmap.getHeight(), CvType.CV_8UC1);

                Mat hierarchy = new Mat();

                Utils.bitmapToMat(myBitmap, img);

                Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(gray, gray, 25, 225, Imgproc.THRESH_BINARY_INV);
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                gray = morphology(gray);
                Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

                Log.v("projektSI", "rozmiar: " + contours.size());
                for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
                    /*wypelnienie kazdego konturu kolorem*/
                    Imgproc.drawContours(img, contours, contourIdx, new Scalar(0, 0, 255), -1);
                    bounding_rect.add(Imgproc.boundingRect(contours.get(contourIdx)));
                    Imgproc.rectangle(img, new Point(bounding_rect.get(contourIdx).x, bounding_rect.get(contourIdx).y), new Point(bounding_rect.get(contourIdx).x + bounding_rect.get(contourIdx).width, bounding_rect.get(contourIdx).y + bounding_rect.get(contourIdx).height), new Scalar(255, 0, 0), boundingRectangleThicnkess);
                }

                hierarchy.release();
                // Log.v("projektSI", "Display width in px is " + img.width());
                Utils.matToBitmap(img, myBitmap);
                //Log.v("projektSI", "Display bitmap width in px is " + myBitmap.getWidth());
                myBitmap = getResizedBitmap(myBitmap, 2560, 1248);
                showPhoto.setImageBitmap(myBitmap);

                textViewArray = new TextView[contours.size()];
                for(int i=0;i<textViewArray.length;i++){
                    textViewArray[i] = new TextView(KameraActivity.this);
                }
                //showPhoto.setAdjustViewBounds(true);
            }
        }

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                int rectX, rectY, recNum;
                znak = data.getStringExtra("znak");
                rectX = data.getIntExtra("x", -1);
                rectY = data.getIntExtra("y", -1);
                recNum = data.getIntExtra("rectangleNumber", -1);
                showSymbol(rectX, rectY, recNum);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    Mat morphology(Mat img){
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size( 20, 20 ), new Point( -1,-1 ));
        Imgproc.morphologyEx(img, img, Imgproc.MORPH_CLOSE, element);
        Imgproc.morphologyEx(img, img, Imgproc.MORPH_OPEN, element);
        return img;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void showSymbol(int x, int y, int num){
        Log.v("projektSI", "num: " + num);

        if(textViewArray[num] !=null || !textViewArray[num].getText().equals("") ){
            layout.removeView(textViewArray[num]);
        }

        //textViewArray[num] = new TextView(KameraActivity.this);
        textViewArray[num].setId(num);
        textViewArray[num].setText(znak);
        textViewArray[num].setTextSize(35);
        textViewArray[num].setTextColor(Color.RED);
        textViewArray[num].setPadding(x, y - (int) textViewArray[num].getTextSize() - boundingRectangleThicnkess, 0, 0);
        layout.addView(textViewArray[num]);
    }
}
