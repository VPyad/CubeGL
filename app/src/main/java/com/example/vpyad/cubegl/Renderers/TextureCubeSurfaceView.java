package com.example.vpyad.cubegl.Renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TextureCubeSurfaceView extends GLSurfaceView {
    private TextureCubeRenderer mRenderer;

    // Offsets for touch events
    private float mPreviousX;
    private float mPreviousY;

    private float mDensity;

    public TextureCubeSurfaceView(Context context)
    {
        super(context);
    }

    public TextureCubeSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event != null)
        {
            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                if (mRenderer != null)
                {
                    float deltaX = (x - mPreviousX) / mDensity / 2f;
                    float deltaY = (y - mPreviousY) / mDensity / 2f;

                    mRenderer.mDeltaX += deltaX;
                    mRenderer.mDeltaY += deltaY;
                }
            }

            mPreviousX = x;
            mPreviousY = y;

            return true;
        }
        else
        {
            return super.onTouchEvent(event);
        }
    }

    // Hides superclass method.
    public void setRenderer(TextureCubeRenderer renderer, float density)
    {
        mRenderer = renderer;
        mDensity = density;
        super.setRenderer(renderer);
    }
}
