 package com.example.denis.myapplication;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;


 /**
 * This interface is for classes that detect faces and
 * locate Landmarks: eyes,mouth and face borders
 */
public interface IFaceLocator extends IDetector {
    PointF getLeftEye();

    PointF getRightEye();

    PointF getMouthLeft();

    PointF getMouthRight();

    RectF getNearestFaceRectangleFloat();

     void setFrame(Bitmap bm);

     boolean isFaceDetected();
}
