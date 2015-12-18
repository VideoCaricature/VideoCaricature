package com.example.denis.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Face detection and landmarks location via OpenCV
 * Detection is being run in it's own thread continuously
 * When any face (nearest one) is detected, it's  geometry is cached
 * All get methods return this cache
 */
public class CVFaceDetector implements Runnable, IFaceLocator {
    protected Mat image;
    org.opencv.core.Rect[] faces;
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;
    boolean isFaceDetected;
    boolean detected;
    Rect nearestFaceRectangle;
    RectF nearestFaceRectangleFloat;
    PointF leftEye,rightEye,leftMouth,rightMouth;


    public CVFaceDetector (Context context)    {
        detected = false;
        isFaceDetected = false;

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);



            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);

        }
    }

    @Override
    public void setAbsoluteFaceSize(int size) {
        absoluteFaceSize = size;
    }

    @Override
    public void setCameraFrame(Mat mat) {
        detected = false;
        image = mat;
    }

    @Override
    public Rect getNearestFaceRectangle() {
        return null;
    }

    @Override
    public RectF getNearestFaceRectangleFloat() {
        return nearestFaceRectangleFloat;
    }

    @Override
    public void detect() {
        if(cascadeClassifier == null || image == null || detected)
            return;
        MatOfRect facesArray = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(image, facesArray, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        else return;
        Log.d("DETECT","detected "+facesArray.size());
        synchronized (this)
        {
            detected = true;
            faces = facesArray.toArray();
            if(faces.length > 0 )
                isFaceDetected = true;
            else
            {
                isFaceDetected = false;
                return;
            }

            //find nearest face
            int maxd = 0;
            org.opencv.core.Rect maxface = null;
            for(org.opencv.core.Rect face: faces)  {
                int d = face.height*face.height+face.width*face.width;
                if(d>maxd) {
                    maxd = d;
                    maxface = face;
                }
            }
            float faceLeft = maxface.x;
            float faceTop = maxface.y;
            float faceBottom = maxface.height+maxface.y;
            float faceRight = maxface.width+maxface.x;
            nearestFaceRectangleFloat = new RectF(faceLeft,faceTop,faceRight,faceBottom);
            float eyesLevel = faceTop+(faceBottom-faceTop)*2.0f/5.0f;
            float faceWidth = maxface.width;
            leftEye = new PointF(faceLeft+faceWidth/4.0f,eyesLevel);
            rightEye = new PointF(faceLeft+faceWidth*3.0f/4.0f,eyesLevel);
            float mouthLevel = faceTop+(faceBottom-faceTop)*4.0f/5.0f;
            leftMouth = new PointF(faceLeft+faceWidth*3.0f/8.0f,mouthLevel);
            rightMouth = new PointF(faceLeft+faceWidth*5.0f/8.0f,mouthLevel);
        }

    }

    @Override
    public int getFacesCount() {
        if(faces!=null)
            return faces.length;
        else return 0;
    }

    @Override
    public boolean isFaceDetected() {
        return isFaceDetected;
    }

    @Override
    public PointF getLeftEye() {
        return leftEye;
    }

    @Override
    public PointF getMouthLeft() {
        return leftMouth;
    }

    @Override
    public PointF getMouthRight() {
        return rightMouth;
    }

    @Override
    public PointF getRightEye() {
        return rightEye;
    }



    @Override
    public void setFrame(Bitmap bm) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bm, mat);
        setCameraFrame(mat);
    }

    @Override
    public void run() {
        while(true)
        {
            detect();
        }
    }


}
