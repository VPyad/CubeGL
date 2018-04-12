package com.example.vpyad.cubegl.Renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class Cube2GlSurfaceView extends GLSurfaceView {

    Cube2Renderer myRender;

    public Cube2GlSurfaceView(Context context) {
        super(context);

        // создание OpenGL ES 3.0 контекста.
        setEGLContextClientVersion(3);

        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        // устанавливаем класс для рендера
        myRender = new Cube2Renderer(context);
        setRenderer(myRender);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private static final float TOUCH_SCALE_FACTOR = 0.015f;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                // вычитаем для того, чтобы куб двигался в том же направлении что и жест
                myRender.setX(myRender.getX() - (dx * TOUCH_SCALE_FACTOR));

                float dy = y - mPreviousY;
                myRender.setY(myRender.getY() - (dy * TOUCH_SCALE_FACTOR));
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
