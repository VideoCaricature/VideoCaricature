package com.example.denis.myapplication;

import android.content.Context;
import android.util.AttributeSet;


import org.opencv.android.JavaCameraView;

/**
 * Created by Кей on 11.10.2015.
 */
public class CameraBridgeViewDrawer extends JavaCameraView {

    public CameraBridgeViewDrawer (Context context, int cameraId)
    {
        super(context,cameraId);
    }

    public CameraBridgeViewDrawer(Context context, AttributeSet attrs)
    {
        super(context,attrs);
    }
}
