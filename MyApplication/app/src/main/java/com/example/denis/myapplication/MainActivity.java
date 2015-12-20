package com.example.denis.myapplication;


import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;

import android.app.Activity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**Main Activity application*/
public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private CameraBridgeViewDrawer cameraDrawer;
    SurfaceView drawSv;
    SurfaceHolder drawHolder;
    SurfaceView cameraSv;
    SurfaceHolder cameraHolder;
    Camera camera;

    final static int CAMERA_ID = 0;
    final static boolean FULL_SCREEN = true;

    private List<TemplateDrawer> drawers;

    /**
    Connect to OpenCVManager
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    //initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    /*
    Load data required for OpenCV Haar cascade
     */

//    private void initializeOpenCVDependencies() {
//        cameraDrawer.initFaceDetector(1,getApplicationContext());
//
//        // And we are ready to go
//        cameraDrawer.enableView();
//    }

    @Deprecated
    private void changeBitmap(int id) {
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        //cameraDrawer.setBitmap(bitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        drawSv = (SurfaceView) findViewById(R.id.drawSurfaceView);
        drawHolder = drawSv.getHolder();
        drawHolder.addCallback(drawHolderCallback);
        drawHolder.setFormat(PixelFormat.TRANSLUCENT);

        cameraSv = (SurfaceView) findViewById(R.id.cameraSurfaceView);
        //cameraSv.setDrawingCacheEnabled(true);
        cameraHolder = cameraSv.getHolder();
        cameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        cameraHolder.addCallback(this);
        cameraSv.setOnClickListener(onClickListener);

        cameraDrawer = new CameraBridgeViewDrawer(drawHolder, cameraSv, drawSv);
        cameraDrawer.initFaceDetector(1, getApplicationContext());

        ImageButton texty = (ImageButton) findViewById(R.id.textView);
        texty.setOnClickListener(onClickListener);
        texty = (ImageButton) findViewById(R.id.textView2);
        texty.setOnClickListener(onDetectorClickListener);

        drawers = new ArrayList<>();

        //add badass drawer
        TemplateElement element_glasses = new TemplateElement();
        element_glasses.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.badass_glasses));
        element_glasses.setBitmapSize(156, 75);
        element_glasses.addLandmark(TemplateElement.LandmarkType.EYES_LEFT, new PointF(32, 42));
        element_glasses.addLandmark(TemplateElement.LandmarkType.EYES_RIGHT, new PointF(93, 42));
        TemplateElement element_moustache = new TemplateElement();
        element_moustache.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.moustache));
        element_moustache.setBitmapSize(800,330);
        element_moustache.addLandmark(TemplateElement.LandmarkType.MOUTH_LEFT, new PointF(205, 258));
        element_moustache.addLandmark(TemplateElement.LandmarkType.MOUTH_RIGHT, new PointF(537, 251));
        TemplateDrawer drawer = new TemplateDrawer("Badass");
        drawer.addElement(element_glasses);
        drawer.addElement(element_moustache);
        drawers.add(drawer);

        cameraDrawer.setTemplateDrawer(drawer);

        //add heisenberg drawer
        TemplateElement element_heis_glasses = new TemplateElement();
        element_heis_glasses.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.heis_glasses));
        element_heis_glasses.setBitmapSize(330, 108);
        element_heis_glasses.addLandmark(TemplateElement.LandmarkType.EYES_LEFT, new PointF(80, 54));
        element_heis_glasses.addLandmark(TemplateElement.LandmarkType.EYES_RIGHT, new PointF(256, 54));
        TemplateElement element_hat = new TemplateElement();
        element_hat.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.heis_hat));
        element_hat.setBitmapSize(517,249);
        element_hat.addLandmark(TemplateElement.LandmarkType.FACE_TOP_LEFT, new PointF(85, 148));
        element_hat.addLandmark(TemplateElement.LandmarkType.FACE_TOP_RIGHT, new PointF(413, 148));
        drawer = new TemplateDrawer("Heisenberg");
        drawer.addElement(element_hat);
        drawer.addElement(element_heis_glasses);

        drawers.add(drawer);

    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
        //camera.setPreviewCallback(cameraDrawer);
        setPreviewSize(FULL_SCREEN);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.release();
        }
        camera = null;
    }

    /*@Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initDebug();
        //cameraDrawer.enableView();
    }*/

//    public void onDestroy() {
//        super.onDestroy();
//        if (cameraDrawer != null)
//            cameraDrawer.disableView();
//    }

//    public void onCameraViewStarted(int width, int height) {
//
//    }
//
//    public void onCameraViewStopped() {
//    }
//
//    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
//
//        return inputFrame.rgba();
//    }

    /**save Bitmap*/
    public void saveBitmap(View view) {

        boolean res = cameraDrawer.saveSignature(getApplicationContext());
        if (res) {
            Toast toast = Toast.makeText(getApplicationContext(),"Image saved to gallery", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),"Failed to save image", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**surfaceCreated*/
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try {
            camera.setPreviewDisplay(holder);
            //camera.setPreviewCallback(cameraDrawer);
            camera.startPreview();
            //Log.d("SURFACE", "Surface created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**surfaceChanged*/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        camera.stopPreview();
        setCameraDisplayOrientation(CAMERA_ID);
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(cameraDrawer);
            camera.startPreview();
            //Log.d("SURFACE", "Surface changed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

    }

    /**получение размеров экрана, установка размеров surface*/
    void setPreviewSize(boolean fullScreen)
    {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();
        cameraDrawer.setDispRect(new RectF(0, 0, display.getWidth(), display.getHeight()));

        // определяем размеры превью камеры
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);
        cameraDrawer.setPrevRect(rectPreview);

        // установка размеров surface из получившегося преобразования

        cameraSv.getLayoutParams().height = (int) (rectPreview.bottom);
        cameraSv.getLayoutParams().width = (int) (rectPreview.right);
        drawSv.getLayoutParams().height = (int) (rectPreview.bottom);
        drawSv.getLayoutParams().width = (int) (rectPreview.right);
    }

    /**ориентация камеры*/
    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        cameraDrawer.setOrientation(result);
        Log.d("DISPLAY ORIENTATION", "Result: " + result);
        camera.setDisplayOrientation(result);
    }

    SurfaceHolder.Callback drawHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            cameraDrawer.setSurfaceReady();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    };

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            showPopupMenu(v,popupMenu);
        }
    };

    View.OnClickListener onDetectorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
            showDetectorPopupMenu(v, popupMenu);
        }
    };

    /**меню для выбора карикатур*/
    private void showPopupMenu(View v,PopupMenu popupMenu) {

        popupMenu.inflate(R.menu.pictures_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pict_heisenberg:
                        cameraDrawer.setTemplateDrawer(drawers.get(1));
                        break;
                    case R.id.pict_badass:
                        cameraDrawer.setTemplateDrawer(drawers.get(0));
                        break;
                    default:
                        return false;

                }
                return true;
            }
        });
        Log.d("MENU","Showing popup menu");
        popupMenu.show();
    }


    /**меню для выбора детектора*/
    private void showDetectorPopupMenu(View v,PopupMenu popupMenu) {

        popupMenu.inflate(R.menu.detector_menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.detector_opencv:
                        cameraDrawer.initFaceDetector(2, getApplicationContext());
                        break;
                    case R.id.detector_gms:
                        cameraDrawer.initFaceDetector(1, getApplicationContext());
                        break;
                    default:
                        return false;

                }
                return true;
            }
        });
        Log.d("MENU","Showing popup menu");
        popupMenu.show();
    }


}