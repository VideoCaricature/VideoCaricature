package com.example.denis.myapplication;

import android.graphics.Rect;

import org.opencv.core.Mat;

/**
 * Created by vasil on 11.10.2015.
 *
 * This interface contains methods essential for face detection
 * OpenCV detector and API detector must implement this interface
 */
public interface IDetector {

    /*
    Method called for every frame to set up a picture for detector
     */
    public void setCameraFrame(Mat mat);

    /*
    Method is called after void setCameraFrame(Mat mat); to detect all faces
     */
    public void detect();

    /*
    Method returns number of recognised faces
     */
    public int getFacesCount();

    /*
    Must return the biggest face rectangle avaliable
    or null, if no faces were detected
     */
    public Rect getNearestFaceRectangle();

    /*
    Used to set face size for detector to work properly
     */
    public void setAbsoluteFaceSize(int size);
}
