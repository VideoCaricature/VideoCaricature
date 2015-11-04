package com.example.denis.myapplication;

import android.content.Context;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.opencv.core.Mat;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Face detection and landmarks location via GMS of Google Play Services
 * Detection is being run in it's own thread continuously
 * When any face (nearest one) is detected, it's  geometry is cached
 * All get methods return this cache
 */
public class GmsFaceDetector implements Runnable , IFaceLocator {
    private FaceDetector faceDetector; //Android services face detector
    private SparseArray<Face> faces;    //array of detected faces
    private boolean detected;           //if any faces detected
    private boolean isFaceDetected;
    private Frame frame;                //camera frame
    private Rect cachedNearestFace;     //nearest face rectangle detected in previous detection cycle
                                        //this rect is calculated using landmarks, rect returned by detector is unaccurate
    private RectF cachedNearestFaceFloat;
    private int cachedFacesCount;
    private PointF leftEye,rightEye,mouthLeft,mouthRight; //landmarks of detected face
    private List<Landmark> landmarksList;


    @Override
    public int getFacesCount() {
        return cachedFacesCount;
    }

    /*
    This function is called in separate thread infinite loop
    It performs detection and caches face geometry
     */
    @Override
    public void detect() {
        if(faceDetector != null && frame != null && detected==false)
        {
            Log.d("DETECT","DETECT STARTED");
            faces = faceDetector.detect(frame);
            Log.d("DETECT", "DETECTED " + faces.size());
            synchronized (this) {
                detected = true;
                cachedFacesCount = faces.size();
                if (cachedFacesCount == 0) { //no faces found
                    isFaceDetected = false;
                    return;
                }
                isFaceDetected  =true;

                //find biggest face detected
                float maxd = 0;
                Face maxface = null;
                for (int i = 0; i < faces.size(); i++) {
                    Face face = faces.valueAt(i);
                    float d = face.getWidth() * face.getHeight() + face.getWidth() * face.getHeight();
                    if (d > maxd) {
                        maxd = d;
                        maxface = face;
                    }
                }
                /*cachedNearestFace = new android.graphics.Rect((int) maxface.getPosition().x, (int) maxface.getPosition().y,
                        (int) maxface.getWidth(), (int) maxface.getHeight());
                cachedNearestFaceFloat = new RectF(maxface.getPosition().x,maxface.getPosition().y,
                        maxface.getWidth()+maxface.getPosition().x,maxface.getHeight()+maxface.getPosition().x);*/

                //clone landmarks list
                landmarksList =  new ArrayList<>();
                for (Landmark l:
                     maxface.getLandmarks()) {
                    landmarksList.add(new Landmark(l.getPosition(),l.getType()));

                }

                //set class fields to landmarks positions
                for(Landmark landmark: landmarksList){
                    if(landmark.getType() == Landmark.RIGHT_EYE)
                        leftEye = landmark.getPosition();
                    if(landmark.getType() == Landmark.LEFT_EYE)
                        rightEye = landmark.getPosition();
                    if(landmark.getType() == Landmark.RIGHT_MOUTH)
                        mouthLeft = landmark.getPosition();
                    if(landmark.getType() == Landmark.LEFT_MOUTH)
                        mouthRight = landmark.getPosition();
                }
                if(mouthLeft==null || mouthRight == null || rightEye == null || leftEye == null)
                {
                    isFaceDetected = false;
                    return;
                }
                //calculate face rectangle
                float eyesMidLevel = (leftEye.y+rightEye.y)/2;
                float mouthMidLevel = (mouthLeft.y+mouthRight.y)/2;
                float eyesToMouth = Math.abs(mouthMidLevel-eyesMidLevel);
                float mouthToChin = eyesToMouth/2;
                float faceBottom = mouthMidLevel + mouthToChin;
                float faceTop = eyesMidLevel-Math.abs(faceBottom-eyesMidLevel);
                if(faceTop<0)
                    faceTop = 0;
                if(faceBottom>frame.getBitmap().getHeight())
                    faceBottom = frame.getBitmap().getHeight();
                float faceCenter = (leftEye.x+rightEye.x)/2;
                float eyesDistance = Math.abs(leftEye.x - faceCenter);
                float faceLeft = leftEye.x - eyesDistance;
                float faceRight = rightEye.x + eyesDistance;
                cachedNearestFace = null;
                cachedNearestFaceFloat = new RectF(faceLeft,faceTop,faceRight,faceBottom);
                Log.d("DETECT","detected "+faceLeft+" "+faceTop+" "+faceRight+" "+faceBottom);
                Log.d("DETECT","eyes "+leftEye.x+" "+leftEye.y+" "+rightEye.x+" "+rightEye.y);
                if(mouthLeft!=null && mouthRight != null)
                Log.d("DETECT","mouth "+mouthLeft.x+" "+mouthLeft.y+" "+mouthRight.x+" "+mouthRight.y);



            }
        }
    }

    public List<Landmark> getLandmarksList()    {
        return landmarksList;
    }

    @Override
    public PointF getLeftEye()
    {
        return leftEye;
    }

    @Override
    public PointF getRightEye()  {
        return rightEye;
    }

    @Override
    public PointF getMouthLeft() {
        return mouthLeft;
    }

    @Override
    public PointF getMouthRight() {
        return mouthRight;
    }

    @Override
    public Rect getNearestFaceRectangle() {
        return cachedNearestFace;
    }

    @Override
    public RectF getNearestFaceRectangleFloat()
    {
        return cachedNearestFaceFloat;
    }

    @Override
    public void setCameraFrame(Mat mat) {

    }

    @Override
    public void setFrame(Bitmap bm){
        frame = new Frame.Builder().setBitmap(bm).build();
        detected = false;
    }

    @Override
    public void setAbsoluteFaceSize(int size) {

    }

    public GmsFaceDetector (Context context)    {
        faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(true).setMode(0).
                setLandmarkType(FaceDetector.ALL_LANDMARKS).
                setProminentFaceOnly(true).build();
        detected = false;
        isFaceDetected = false;
        cachedNearestFace = null;
        cachedFacesCount  = 0;
    }

    @Override
    public void run() {
        while(true)
        {
            detect();
        }
    }

    @Override
    public boolean isFaceDetected()
    {
        return isFaceDetected;
    }
}
