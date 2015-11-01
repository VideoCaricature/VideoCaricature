package com.example.denis.myapplication;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.*;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.graphics.Bitmap.Config.RGB_565;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private CameraBridgeViewDrawer mOpenCvCameraView;
    private OpenCvFaceDetector detector;
    private String text_str;
    private String api_text;
    private MenuItem               heisenberg; //Menu pictures
    private MenuItem               pict;
    private MenuItem               mousestache;
    private MenuItem               r2d2;
    private MenuItem               black_hat;
    //private Map<MenuItem, Integer> items = new HashMap<>();//for Menu




    /*
    Connect to OpenCVManager
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    /*
    Load data required for OpenCV Haar cascade
     */
    private void initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);



            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            detector = new OpenCvFaceDetector();
            // Load the cascade classifier
            if(!detector.loadCascadeClassifier(mCascadeFile))
            {
                Log.e("OpenCVActivity","Failed to load cascade from file");
            }

        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);

        }



        // And we are ready to go
        mOpenCvCameraView.enableView();
    }

    private void changeBitmap(int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        mOpenCvCameraView.setBitmap(bitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewDrawer) findViewById(R.id.view);
        mOpenCvCameraView.setCameraIndex(98);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnClickListener(onClickListener);
        TextView texty = (TextView) findViewById(R.id.textView);
        texty.setOnClickListener(onClickListener);
        changeBitmap(R.drawable.heisenberg);

        TemplateElement element = new TemplateElement();
        element.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.glasses));
        element.addLandmark(TemplateElement.LandmarkType.EYES_LEFT, new PointF(83, 54));
        element.addLandmark(TemplateElement.LandmarkType.EYES_RIGHT,new PointF(256,55));
        TemplateDrawer drawer = new TemplateDrawer();
        drawer.addElement(element);
        mOpenCvCameraView.setTemplateDrawer(drawer);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /*@Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initDebug();
        //mOpenCvCameraView.enableView();
    }*/

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }


    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        detector.setAbsoluteFaceSize((int) (height * 0.2));
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        /*detector.setCameraFrame(inputFrame.gray());


        // Use the classifier to detect faces
        detector.detect();


        // If there are any faces found, draw a rectangle around it
        String new_text_str = String.valueOf(detector.getFacesCount());
        if(!new_text_str.equals(text_str))        {
            text_str = new_text_str;
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView textView = (TextView)(findViewById(R.id.textView));
                    textView.setText(text_str);
                }
            });
        }
        android.graphics.Rect rect = detector.getNearestFaceRectangle();
        if(rect!=null)
            mOpenCvCameraView.setFaceRect(rect);
        else mOpenCvCameraView.setFaceRect(null);*/

        return inputFrame.rgba();
    }



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            showPopupMenu(v,popupMenu);
        }
    };

    private void showPopupMenu(View v,PopupMenu popupMenu) {

        popupMenu.inflate(R.menu.pictures_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bitmap bitmap = null;
                switch (item.getItemId()) {
                    case R.id.pict_heisenberg:
                        changeBitmap(R.drawable.heisenberg);
                        break;
                    case R.id.pict_moustache:
                        changeBitmap(R.drawable.moustache);
                        break;
                    default:
                        return false;

                }
                // mOpenCvCameraView.setBitmap(bitmap);
                return true;
            }
        });
        Log.d("MENU","Showing popup menu");
        popupMenu.show();
    }


}