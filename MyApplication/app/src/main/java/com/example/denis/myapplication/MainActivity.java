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
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.xmlpull.v1.XmlPullParser;

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



        mOpenCvCameraView.initFaceDetector(1,getApplicationContext());

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
        ImageButton texty = (ImageButton) findViewById(R.id.textView);
        texty.setOnClickListener(onClickListener);
        texty = (ImageButton) findViewById(R.id.textView2);
        texty.setOnClickListener(onDetectorClickListener);

        drawers = new ArrayList<>();

        XmlTemplateReader tmpBadass = new XmlTemplateReader(getApplicationContext(),"Badass");
        TemplateDrawer drawer = tmpBadass.getElement();
        drawers.add(drawer);
        mOpenCvCameraView.setTemplateDrawer(drawer);
        XmlTemplateReader tmpHeisenberg = new XmlTemplateReader(getApplicationContext(),"Heisenberg");
        drawer = (tmpHeisenberg.getElement());
        drawers.add(drawer);
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

    public void saveBitmap(View view) {

        boolean res = mOpenCvCameraView.saveSignature(getApplicationContext());
        if (res) {
            Toast toast = Toast.makeText(getApplicationContext(),"Image saved to gallery", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),"Failed to save image", Toast.LENGTH_SHORT);
            toast.show();
        }
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