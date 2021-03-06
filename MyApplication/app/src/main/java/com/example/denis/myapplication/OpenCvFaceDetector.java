package com.example.denis.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/* Created by vasil on 11.10.2015.
        *
        * This interface contains methods essential for face detection
        * OpenCV detector and API detector must implement this interface
*/

/**
Class for face detection via OpenCV
 */
@Deprecated
public class OpenCvFaceDetector implements Runnable, IFaceLocator {
    protected Mat image;
    Rect[] faces;
    private CascadeClassifier cascadeClassifier;
    private int absoluteFaceSize;

    @Override
    public void setCameraFrame(Mat mat) {
        image = mat;
    }

    @Override
    public void detect() {
        MatOfRect facesArray = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(image, facesArray, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        faces = facesArray.toArray();
    }

    /*
    This method loads cascade classifier from already loaded xml-formatted File
     */
    public boolean loadCascadeClassifier(File f)
    {
        try {
            cascadeClassifier = new CascadeClassifier(f.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
            return false;
        }
    }

    @Override
    public void setAbsoluteFaceSize(int size) {
        absoluteFaceSize = size;
    }

    @Override
    public int getFacesCount() {
        if(faces != null)
            return faces.length;
        else
            return 0;
    }


    /*
    DUMMY. Must be implemented later
     */
    @Override
    public android.graphics.Rect getNearestFaceRectangle() {
        if(faces.length==0)
        {
            return null;
        }
        int maxd = 0;
        Rect maxface = null;
        for(Rect face: faces)  {
            int d = face.height*face.height+face.width*face.width;
            if(d>maxd) {
                maxd = d;
                maxface = face;
            }
        }
        return new android.graphics.Rect(maxface.x,maxface.y,maxface.width,maxface.height);
    }

    @Override
    public PointF getLeftEye() {
        return null;
    }

    @Override
    public PointF getRightEye() {
        return null;
    }

    @Override
    public PointF getMouthLeft() {
        return null;
    }

    @Override
    public PointF getMouthRight() {
        return null;
    }

    @Override
    public RectF getNearestFaceRectangleFloat() {
        return null;
    }

    @Override
    public void setFrame(Bitmap bm) {

    }

    @Override
    public boolean isFaceDetected() {
        return false;
    }

    @Override
    public void run() {

    }
}
