
package com.example.denis.myapplication;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


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
public class MainActivity extends Activity {

        int numberOfFaceDetected ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        {
            super.onCreate(savedInstanceState);
            setContentView(new myView(this));
        }

    }

    private class myView extends View {


        private int imageWidth, imageHeight;
        private int numberOfFace = 5;
        private FaceDetector myFaceDetect;
        private FaceDetector.Face[] myFace;
        float myEyesDistance;

        private OpenCvFaceDetector detector;

        Bitmap myBitmap;

        public myView(Context context) {
            super(context);
            detector = new OpenCvFaceDetector();

            BitmapFactory.Options BitmapFactoryOptionsbfo = new BitmapFactory.Options();
            BitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565;
            myBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.face5, BitmapFactoryOptionsbfo);
            imageWidth = myBitmap.getWidth();
            imageHeight = myBitmap.getHeight();
            myFace = new FaceDetector.Face[numberOfFace];
            myFaceDetect = new FaceDetector(imageWidth, imageHeight,
                    numberOfFace);
            numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace);

        }


        protected void onDraw(Canvas canvas) {

            canvas.drawBitmap(myBitmap, 0, 0, null);

            Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(3);

            for (int i = 0; i < numberOfFaceDetected; i++) {
                Face face = myFace[i];
                PointF myMidPoint = new PointF();
                face.getMidPoint(myMidPoint);
                myEyesDistance = face.eyesDistance();

                canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 2),
                        (int) (myMidPoint.y - myEyesDistance * 2),
                        (int) (myMidPoint.x + myEyesDistance * 2),
                        (int) (myMidPoint.y + myEyesDistance * 2), myPaint);
            }
                              Toast.makeText(MainActivity.this,
                        "number of faces = " + numberOfFaceDetected,
                        Toast.LENGTH_LONG).show();
         }
    }
}





