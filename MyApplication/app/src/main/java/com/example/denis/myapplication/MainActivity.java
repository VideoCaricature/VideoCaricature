package com.example.denis.myapplication;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends Activity implements CvCameraViewListener2 {
    private CameraBridgeViewDrawer mOpenCvCameraView;

    private List<TemplateDrawer> drawers;

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

        /*try {
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

        }*/

        mOpenCvCameraView.initFaceDetector(2,getApplicationContext());

        // And we are ready to go
        mOpenCvCameraView.enableView();
    }

    @Deprecated
    private void changeBitmap(int id) {
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        //mOpenCvCameraView.setBitmap(bitmap);
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
        texty = (TextView)findViewById(R.id.textView2);
        texty.setOnClickListener(onDetectorClickListener);

        drawers = new ArrayList<>();

        //add badass drawer
        TemplateElement element_glasses = new TemplateElement();
        element_glasses.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.badass_glasses));
        element_glasses.addLandmark(TemplateElement.LandmarkType.EYES_LEFT, new PointF(31, 41));
        element_glasses.addLandmark(TemplateElement.LandmarkType.EYES_RIGHT, new PointF(94, 42));
        TemplateElement element_moustache = new TemplateElement();
        element_moustache.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.moustache));
        element_moustache.addLandmark(TemplateElement.LandmarkType.MOUTH_LEFT, new PointF(205, 258));
        element_moustache.addLandmark(TemplateElement.LandmarkType.MOUTH_RIGHT, new PointF(537, 251));
        TemplateDrawer drawer = new TemplateDrawer("Badass");
        drawer.addElement(element_glasses);
        drawer.addElement(element_moustache);
        drawers.add(drawer);

        //add heisenberg drawer
        TemplateElement element_heis_glasses = new TemplateElement();
        element_heis_glasses.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.heis_glasses));
        element_heis_glasses.addLandmark(TemplateElement.LandmarkType.EYES_LEFT, new PointF(80, 54));
        element_heis_glasses.addLandmark(TemplateElement.LandmarkType.EYES_RIGHT, new PointF(256, 54));
        TemplateElement element_hat = new TemplateElement();
        element_hat.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.heis_hat));
        element_hat.addLandmark(TemplateElement.LandmarkType.FACE_TOP_LEFT, new PointF(85, 148));
        element_hat.addLandmark(TemplateElement.LandmarkType.FACE_TOP_RIGHT, new PointF(413, 148));
        drawer = new TemplateDrawer("Heisenberg");
        drawer.addElement(element_hat);
        drawer.addElement(element_heis_glasses);

        drawers.add(drawer);
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

    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        return inputFrame.rgba();
    }



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            showPopupMenu(v,popupMenu);
        }
    };

    View.OnClickListener onDetectorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            showDetectorPopupMenu(v, popupMenu);
        }
    };

    private void showPopupMenu(View v,PopupMenu popupMenu) {

        popupMenu.inflate(R.menu.pictures_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pict_heisenberg:
                        mOpenCvCameraView.setTemplateDrawer(drawers.get(1));
                        break;
                    case R.id.pict_badass:
                        mOpenCvCameraView.setTemplateDrawer(drawers.get(0));
                        break;
                    default:
                        return false;

                }
                return true;
            }
        });
        Log.d("MENU","Showing popup menu");
        popupMenu.show();
    }

    private void showDetectorPopupMenu(View v,PopupMenu popupMenu) {

        popupMenu.inflate(R.menu.detector_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.detector_opencv:
                        mOpenCvCameraView.initFaceDetector(2,getApplicationContext());
                        break;
                    case R.id.detector_gms:
                        mOpenCvCameraView.initFaceDetector(1, getApplicationContext());
                        break;
                    default:
                        return false;

                }
                return true;
            }
        });
        Log.d("MENU","Showing popup menu");
        popupMenu.show();
    }


}