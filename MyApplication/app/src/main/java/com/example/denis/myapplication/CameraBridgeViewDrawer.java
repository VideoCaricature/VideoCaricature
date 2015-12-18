package com.example.denis.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Custom class for camera view and drawing a caricature
 */
public class CameraBridgeViewDrawer implements Camera.PreviewCallback{
    protected GmsFaceDetector gms_detector;
    protected CVFaceDetector cv_detector;
    protected IFaceLocator locator;
    protected SurfaceHolder holder;
    protected SurfaceView cameraSv, drawSv;
    protected boolean surfaceReady;
    //protected Bitmap bmp;
    TemplateDrawer templateDrawer;
    boolean detect_started;
    int detectorType;
    Bitmap saveBitmap, dumpBitmap;
    Matrix sizeMatrix;
    int orientation;
    RectF prevRect;
    RectF dispRect;

    static int fileCounter = 1;

    MainActivity activity;  //временно

    //public CameraBridgeViewDrawer (Context context, int cameraId)
    //{
    //    super(context,cameraId);
    //}

//    public CameraBridgeViewDrawer(Context context, AttributeSet attrs)
//    {
//        super(context, attrs);
//        // setLayerType(LAYER_TYPE_SOFTWARE, null);
//        // setDrawingCacheEnabled(true);
//        detect_started = false;
//        detectorType = 0;
//    }

    public CameraBridgeViewDrawer(SurfaceHolder holder, SurfaceView cameraView, SurfaceView drawView)
    {
        //this.activity = activity;
        cameraSv = cameraView;
        drawSv = drawView;
        detect_started = false;
        surfaceReady = false;
        detectorType = 0;
        this.holder = holder;
    }

    public void setSurfaceReady()
    {
        surfaceReady = true;
    }

    public void setDispRect(RectF rect){
        dispRect = rect;
    }

    public void setMatrix(Matrix matrix)
    {
        sizeMatrix = matrix;
    }

    public void setPrevRect(RectF rect)
    {
        prevRect = rect;
    }

    public void setOrientation(int orientation)
    {
//        Camera.CameraInfo ci = new Camera.CameraInfo();
//        Camera.getCameraInfo(MainActivity.CAMERA_ID, ci);
//        if(ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            if(orientation == 270)
//                this.orientation = 90;
//            else if(orientation == 90)
//                this.orientation = 270;
//        } else
            this.orientation = orientation;
    }

    public void setTemplateDrawer(TemplateDrawer drawer)
    {
        synchronized (this) {
            templateDrawer = drawer;
        }
    }

    public void initFaceDetector(int type, Context context)
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
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        Log.d("DELIVER","FRAME");

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


//        if (mListener != null) {
//            modified = mListener.onCameraFrame(frame);
//        } else {
//            modified = frame.rgba();
//        }
//
//        boolean bmpValid = true;
//        if (modified != null) {
//            try {
//                Utils.matToBitmap(modified, mCacheBitmap);
//
//            } catch(Exception e) {
//                Log.e(TAG, "Mat type: " + modified);
//                Log.e(TAG, "Bitmap type: " + mCacheBitmap.getWidth() + "*" + mCacheBitmap.getHeight());
//                Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
//                bmpValid = false;
//            }
//        }
        Bitmap bmp;
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        int rgb[] = new int[previewSize.width * previewSize.height];
        decodeYUV420SP(rgb, data, previewSize.width, previewSize.height);
        bmp = Bitmap.createBitmap(rgb, previewSize.width, previewSize.height, Bitmap.Config.ARGB_8888);
        Rect frame = holder.getSurfaceFrame();
        //bmp = Bitmap.createScaledBitmap(bmp, frame.width(), frame.height(), false);
        Camera.CameraInfo ci = new Camera.CameraInfo();
        Camera.getCameraInfo(MainActivity.CAMERA_ID, ci);
        int t_orientation = orientation;
        if(ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            if (orientation == 270)
                t_orientation = 90;
            else if (orientation == 90)
                t_orientation = 270;
        }
        bmp = rotateImage(bmp, t_orientation);
        if(ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            bmp = mirrorImage(bmp);

        if(locator != null)
            locator.setFrame(bmp);
        if (bmp != null) {
            Canvas srcCanvas = holder.lockCanvas();
            Log.d("DETECT", "REPAINTING");
            if (srcCanvas != null) {
                srcCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                saveBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
                //saveBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(saveBitmap);

                //Log.d(TAG, "mStretch value: " + mScale);

                //double viewWidth = canvas.getWidth();
                //double viewHeight = canvas.getHeight();
                //double imageWidth = bmp.getWidth();
                //double imageHeight = bmp.getHeight();
                //double scale = (float)Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
                //Rect destBounds = new Rect(0, 0, (int) (imageWidth * scale), (int) (imageHeight * scale));
                //canvas.drawBitmap(bmp, null, destBounds, null);

                if(locator != null || templateDrawer != null)
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
                        Paint paint = new Paint();
                        paint.setColor(Color.GREEN);
                        paint.setStyle(Paint.Style.STROKE); //no fill
                        paint.setStrokeWidth(5);
                        RectF face  = locator.getNearestFaceRectangleFloat();
                        float x1 = face.left; //x coordinate of top left position of the face within the image
                        float y1 = face.top; //x coordinate of top left position of the face within the image
                        float x2 = face.right; //width of the face region in pixels
                        float y2 = face.bottom; //height of the face region in pixels
                        /*canvas.drawRoundRect(new RectF(x1 * (float) scale, y1 * (float) scale,
                                x2 * (float) scale, y2 * (float) scale), 2, 2, paint);*/
                        /*PointF leftEye = locator.getLeftEye();
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
//                if (mFpsMeter != null) {
//                    mFpsMeter.measure();
//                    mFpsMeter.draw(canvas, 20, 30);
//                }
                Bitmap scaledTemplate = Bitmap.createScaledBitmap(saveBitmap, (int)(prevRect.width()), (int)(prevRect.height()), false);
                int x = 0, y = 0;
//                getRelativeLeft(cameraSv);
//                getRelativeTop(cameraSv);
//                getRelativeLeft(drawSv);
//                getRelativeTop(drawSv);
//                cameraSv.getBottom();
//                drawSv.getBottom();
//                x = Math.abs(cameraSv.getLeft() - drawSv.getLeft());
//                y = Math.abs(cameraSv.getTop() - drawSv.getTop());
//                int[] cLoc = new int[2];
//                int[] dLoc = new int[2];
//                cameraSv.getLocationOnScreen(cLoc);
//                drawSv.getLocationOnScreen(dLoc);
//                if(cameraSv.getLeft() < drawSv.getLeft())
//                    x = -x;
//                if(cameraSv.getTop() < drawSv.getTop())
//                    y = -y;
//                srcCanvas.drawBitmap(scaledTemplate, x, y, null);
                switch(orientation){
                    case 90: x = (int)(dispRect.width() - prevRect.width()); break;
                    case 180: y = (int)(dispRect.height() - prevRect.height()); break;
                }
                Log.d("SHIFT", "orientation: " + orientation + " dx: " + x + " dy: " + y);
                srcCanvas.drawBitmap(scaledTemplate, x, y, null);
                dumpBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
                Canvas c = new Canvas(dumpBitmap);
                c.drawBitmap(bmp, new Matrix(), null);
                c.drawBitmap(saveBitmap, 0, 0, null);
                holder.unlockCanvasAndPost(srcCanvas);
            }
        }
    }

    private int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    private int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    Bitmap scaleImage(Bitmap bmp, int width, int height)
    {
        //int x = 0, y = 0;
        Bitmap t_bmp = Bitmap.createScaledBitmap(bmp, (int)(prevRect.width()), (int)(prevRect.height()), false);
//        if(t_bmp.getWidth() != width)
//            x = (t_bmp.getWidth() - width)/2;
//        else
//            y = (t_bmp.getHeight() - height)/2;
        return Bitmap.createBitmap(t_bmp, 0, 0, width, height);
    }

    Bitmap mirrorImage(Bitmap bmp)
    {
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
    }

    static Bitmap rotateImage(Bitmap bmp, int deg)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(deg);
//        int height, width;
//        if(deg == 90 || deg == 270){
//            height = bmp.getWidth();
//            width = bmp.getHeight();
//        }else{
//            height = bmp.getHeight();
//            width = bmp.getWidth();
//        }

        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    public boolean saveSignature(Context context){
//        Bitmap  bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        this.draw(canvas);

//        Bitmap bitmap = getDrawingCache();

        String fileName = "Caricature " + Integer.toString(fileCounter) ;
        fileCounter++;

        //File file = new File(Environment.getExternalStorageDirectory() + fileName);
        String res = null;
        if (dumpBitmap != null)
            res = MediaStore.Images.Media.insertImage(context.getContentResolver(), dumpBitmap, fileName ,"Made with VideoCaricature");
        if (res != null){
            Log.d("SAVED",res);
            return true;
        }
        else return false;

        /*try {
            if (dumpBitmap != null) {
                dumpBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)                  r = 0;               else if (r > 262143)
                    r = 262143;
                if (g < 0)                  g = 0;               else if (g > 262143)
                    g = 262143;
                if (b < 0)                  b = 0;               else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
}
