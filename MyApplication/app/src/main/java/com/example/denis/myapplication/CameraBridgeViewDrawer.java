package com.example.denis.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by Кей on 11.10.2015.
 */
public class CameraBridgeViewDrawer extends JavaCameraView {
    protected FaceDetector faceDetector;
    public CameraBridgeViewDrawer (Context context, int cameraId)
    {
        super(context,cameraId);
    }

    public CameraBridgeViewDrawer(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(true).setMode(0).
                setProminentFaceOnly(true).build();

    }

    @Override
    protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
        Log.d("DELIVER","FRAME");
        Mat modified;

        if (!faceDetector.isOperational()) {
            Log.d("DELIVER", "IS NOT OPERATIONAL");
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

        if (bmpValid && mCacheBitmap != null) {
            Frame detect_frame = new Frame.Builder().setBitmap(mCacheBitmap).build();
            SparseArray<Face> faces = faceDetector.detect(detect_frame);
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
                Log.d(TAG, "mStretch value: " + mScale);

                if (mScale != 0) {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2),
                                    (int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2 + mScale*mCacheBitmap.getWidth()),
                                    (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2 + mScale*mCacheBitmap.getHeight())), null);
                } else {
                    canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                            new Rect((canvas.getWidth() - mCacheBitmap.getWidth()) / 2,
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2,
                                    (canvas.getWidth() - mCacheBitmap.getWidth()) / 2 + mCacheBitmap.getWidth(),
                                    (canvas.getHeight() - mCacheBitmap.getHeight()) / 2 + mCacheBitmap.getHeight()), null);
                }
                if(myBitmap!=null && faceRect != null)
                {
                    canvas.drawBitmap(myBitmap,new Rect(0,0,myBitmap.getWidth(), myBitmap.getHeight()),
                            faceRect,null);
                    if(faces.size()>0)
                    {
                        Face face = faces.valueAt(0);
                        Paint paint = new Paint();
                        paint.setColor(Color.GREEN);
                        paint.setStyle(Paint.Style.STROKE); //no fill
                        paint.setStrokeWidth(5);
                        canvas.drawRect(face.getPosition().x,face.getPosition().y,face.getWidth(),face.getHeight(),paint);

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
