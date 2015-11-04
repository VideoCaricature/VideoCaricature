package com.example.denis.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Custom class for camera view and drawing a caricature
 */
public class CameraBridgeViewDrawer extends JavaCameraView {
    protected GmsFaceDetector gms_detector;
    protected CVFaceDetector cv_detector;
    protected IFaceLocator locator;
    TemplateDrawer templateDrawer;
    boolean detect_started;
    int detectorType;
    public CameraBridgeViewDrawer (Context context, int cameraId)
    {
        super(context,cameraId);
    }

    public CameraBridgeViewDrawer(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        detect_started = false;
        detectorType = 0;
    }

    public void setTemplateDrawer(TemplateDrawer drawer)
    {
        synchronized (this) {
            templateDrawer = drawer;
        }
    }

    public void initFaceDetector(int type,Context context)
    {
        synchronized (this) {
            gms_detector = null;
            cv_detector = null;
            detect_started = false;
            detectorType = type;
            if (type == 1) {
                gms_detector = new GmsFaceDetector(context);
                locator = gms_detector;
            } else if (type == 2) {
                cv_detector = new CVFaceDetector(context);
                cv_detector.setAbsoluteFaceSize(100);
                locator = cv_detector;
            }
        }
    }



    @Override
    protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
        Log.d("DELIVER","FRAME");
        Mat modified;

        if(!detect_started ){
            Thread thread;
            if(detectorType == 1){
                thread = new Thread(gms_detector,"Gms detect thread");
            }
            else{
                thread = new Thread(cv_detector,"OpenCV detect thread");
            }

            thread.start();
            detect_started = true;
        }


        if (mListener != null) {
            modified = mListener.onCameraFrame(frame);
        } else {
            modified = frame.rgba();
        }

        boolean bmpValid = true;
        if (modified != null) {
            try {
                Utils.matToBitmap(modified, mCacheBitmap);

            } catch(Exception e) {
                Log.e(TAG, "Mat type: " + modified);
                Log.e(TAG, "Bitmap type: " + mCacheBitmap.getWidth() + "*" + mCacheBitmap.getHeight());
                Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
                bmpValid = false;
            }
        }
        if(locator != null){
            if(detectorType == 1)
            {
                locator.setFrame(mCacheBitmap);
            }
            else {
                locator.setCameraFrame(modified);
            }
        }

        if (bmpValid && mCacheBitmap != null) {
            Canvas canvas = getHolder().lockCanvas();
            Log.d("DETECT", "REPAINTING");
            if (canvas != null) {
                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                Log.d(TAG, "mStretch value: " + mScale);

                double viewWidth = canvas.getWidth();
                double viewHeight = canvas.getHeight();
                double imageWidth = mCacheBitmap.getWidth();
                double imageHeight = mCacheBitmap.getHeight();
                double scale = (float)Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
                Rect destBounds = new Rect(0, 0, (int) (imageWidth * scale), (int) (imageHeight * scale));
                canvas.drawBitmap(mCacheBitmap, null, destBounds, null);

                if(locator != null)
                if(myBitmap!=null || templateDrawer != null)
                {
                    int facesCount = locator.getFacesCount();
                    Log.d("DETECT","repainting faces: "+facesCount);
                    if(locator.isFaceDetected())
                    {
                        if(templateDrawer!=null) {
                            ArrayList<Landmark> landmarks = new ArrayList<>();
                            landmarks.add(new Landmark(locator.getLeftEye(),Landmark.LEFT_EYE));
                            landmarks.add(new Landmark(locator.getRightEye(),Landmark.RIGHT_EYE));
                            landmarks.add(new Landmark(locator.getMouthLeft(),Landmark.LEFT_MOUTH));
                            landmarks.add(new Landmark(locator.getMouthRight(),Landmark.RIGHT_MOUTH));
                            templateDrawer.setLandmarks(landmarks,
                                    locator.getNearestFaceRectangleFloat());
                            templateDrawer.draw(canvas);
                        }
                        /*Paint paint = new Paint();
                        paint.setColor(Color.GREEN);
                        paint.setStyle(Paint.Style.STROKE); //no fill
                        paint.setStrokeWidth(5);
                        RectF face  = locator.getNearestFaceRectangleFloat();
                        float x1 = face.left; //x coordinate of top left position of the face within the image
                        float y1 = face.top; //x coordinate of top left position of the face within the image
                        float x2 = face.right; //width of the face region in pixels
                        float y2 = face.bottom; //height of the face region in pixels
                        canvas.drawRoundRect(new RectF(x1 * (float) scale, y1 * (float) scale,
                                x2 * (float) scale, y2 * (float) scale), 2, 2, paint);
                        PointF leftEye = locator.getLeftEye();
                        if(leftEye!=null)
                            canvas.drawCircle(leftEye.x,leftEye.y,10,paint);
                        else
                            Log.d("DRAW","Not found left eye");
                        PointF rightEye = locator.getRightEye();
                        if(rightEye!=null)
                            canvas.drawCircle(rightEye.x,rightEye.y,10,paint);
                        else
                            Log.d("DRAW", "Not found right eye");
                        PointF mouthLeft = locator.getMouthLeft();
                        if(mouthLeft!=null)
                            canvas.drawCircle(mouthLeft.x,mouthLeft.y,10,paint);
                        PointF mouthRight = locator.getMouthRight();
                        if(mouthRight!=null)
                            canvas.drawCircle(mouthRight.x,mouthRight.y,10,paint);*/

            }

                }
                if (mFpsMeter != null) {
                    mFpsMeter.measure();
                    mFpsMeter.draw(canvas, 20, 30);
                }
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }
}
