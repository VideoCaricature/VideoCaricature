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
 * Created by Кей on 11.10.2015.
 */
public class CameraBridgeViewDrawer extends JavaCameraView {
    //protected FaceDetector faceDetector;
    protected GmsFaceDetector gms_detector;
    TemplateDrawer templateDrawer;
    boolean detect_started;
    public CameraBridgeViewDrawer (Context context, int cameraId)
    {
        super(context,cameraId);
    }

    public CameraBridgeViewDrawer(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        /*faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(true).setMode(0).
                setProminentFaceOnly(true).build();*/
        gms_detector = new GmsFaceDetector(context);
        detect_started = false;
    }

    public void setTemplateDrawer(TemplateDrawer drawer)
    {
        synchronized (this) {
            templateDrawer = drawer;
        }
    }



    @Override
    protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
        Log.d("DELIVER","FRAME");
        Mat modified;

        if(!detect_started){
            Thread thread = new Thread(gms_detector,"Gms detect thread");
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
        gms_detector.setFrame(mCacheBitmap);
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

                if(myBitmap!=null || templateDrawer != null)
                {
                    int facesCount = gms_detector.getFacesCount();
                    Log.d("DETECT","repainting faces: "+facesCount);
                    if(gms_detector.isFaceDetected())
                    {
                        if(templateDrawer!=null) {
                            ArrayList<Landmark> landmarks = new ArrayList<>();
                            landmarks.add(new Landmark(gms_detector.getLeftEye(),Landmark.LEFT_EYE));
                            landmarks.add(new Landmark(gms_detector.getRightEye(),Landmark.RIGHT_EYE));
                            landmarks.add(new Landmark(gms_detector.getMouthLeft(),Landmark.LEFT_MOUTH));
                            landmarks.add(new Landmark(gms_detector.getMouthRight(),Landmark.RIGHT_MOUTH));
                            templateDrawer.setLandmarks(landmarks,
                                    gms_detector.getNearestFaceRectangleFloat());
                            templateDrawer.draw(canvas);
                        }
                        Paint paint = new Paint();
                        paint.setColor(Color.GREEN);
                        paint.setStyle(Paint.Style.STROKE); //no fill
                        paint.setStrokeWidth(5);
                        RectF face  = gms_detector.getNearestFaceRectangleFloat();
                        float x1 = face.left; //x coordinate of top left position of the face within the image
                        float y1 = face.top; //x coordinate of top left position of the face within the image
                        float x2 = face.right; //width of the face region in pixels
                        float y2 = face.bottom; //height of the face region in pixels
                        /*canvas.drawRoundRect(new RectF(x1 * (float) scale, y1 * (float) scale,
                                x2 * (float) scale, y2 * (float) scale), 2, 2, paint);*/
                        /*PointF leftEye = gms_detector.getLeftEye();
                        if(leftEye!=null)
                            canvas.drawCircle(leftEye.x,leftEye.y,10,paint);
                        else
                            Log.d("DRAW","Not found left eye");
                        PointF rightEye = gms_detector.getRightEye();
                        if(rightEye!=null)
                            canvas.drawCircle(rightEye.x,rightEye.y,10,paint);
                        else
                            Log.d("DRAW", "Not found right eye");
                        PointF mouthLeft = gms_detector.getMouthLeft();
                        if(mouthLeft!=null)
                            canvas.drawCircle(mouthLeft.x,mouthLeft.y,10,paint);
                        PointF mouthRight = gms_detector.getMouthRight();
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
