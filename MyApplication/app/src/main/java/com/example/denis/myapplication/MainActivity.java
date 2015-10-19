package com.example.denis.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static android.graphics.Bitmap.Config.RGB_565;


public class MainActivity extends Activity implements CvCameraViewListener2 {
    private CameraBridgeViewDrawer mOpenCvCameraView;
    private OpenCvFaceDetector detector;
    private String text_str;
    private String api_text;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewDrawer) findViewById(R.id.view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.heisenberg);
        mOpenCvCameraView.setBitmap(bitmap);
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

        detector.setCameraFrame(inputFrame.gray());


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
        Rect rect = detector.getNearestFaceRectangle();
        if(rect!=null)
            mOpenCvCameraView.setFaceRect(new android.graphics.Rect(rect.x,rect.y,rect.width+rect.x,rect.height+rect.y)  );
        else mOpenCvCameraView.setFaceRect(null);




        /*for (int i = 0; i <facesArray.length; i++)
            Core.rectangle(inputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);*/
        return inputFrame.rgba();
    }
}