package com.example.denis.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for caricature template
 */
public class TemplateDrawer {
    List<TemplateElement> elements;

    public TemplateDrawer()
    {
        elements = new ArrayList<>();
    }

    void setLandmarks(List<Landmark> frameLandmarks,RectF faceRect)
    {
        ArrayList<TemplateElement.LandmarkPoint> landmarks = new ArrayList<>();
        for (Landmark landmark:
                frameLandmarks) {
            TemplateElement.LandmarkType type = null;
            switch (landmark.getType())
            {
                case Landmark.LEFT_EYE:
                    type = TemplateElement.LandmarkType.EYES_LEFT;
                    break;
                case Landmark.RIGHT_EYE:
                    type = TemplateElement.LandmarkType.EYES_RIGHT;
                    break;
                case Landmark.LEFT_MOUTH:
                    type = TemplateElement.LandmarkType.MOUTH_LEFT;
                    break;
                case Landmark.RIGHT_MOUTH:
                    type = TemplateElement.LandmarkType.MOUTH_RIGHT;
                    break;
                default:
                    continue;
            }

            TemplateElement.LandmarkPoint point = new TemplateElement.LandmarkPoint(landmark.getPosition(),type);
            landmarks.add(point);
        }
        landmarks.add(new TemplateElement.LandmarkPoint(new PointF(faceRect.left,faceRect.top),
                 TemplateElement.LandmarkType.FACE_TOP_LEFT));
        landmarks.add(new TemplateElement.LandmarkPoint(new PointF(faceRect.left,faceRect.bottom),
                TemplateElement.LandmarkType.FACE_BOTTOM_LEFT));
        landmarks.add(new TemplateElement.LandmarkPoint(new PointF(faceRect.right,faceRect.top),
                TemplateElement.LandmarkType.FACE_TOP_RIGHT));
        landmarks.add(new TemplateElement.LandmarkPoint(new PointF(faceRect.right,faceRect.bottom),
                TemplateElement.LandmarkType.FACE_BOTTOM_RIGHT));
        for (TemplateElement e:
             elements) {
            e.setFrameLandmarks(landmarks);
        }
    }

    void draw(Canvas canvas)
    {
        for (TemplateElement e:
                elements) {
            e.draw(canvas);
        }
    }

    void addElement(TemplateElement elem)
    {
        elements.add(elem);
    }
}
