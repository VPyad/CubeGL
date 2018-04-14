package com.example.vpyad.cubegl.Renderers;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.vpyad.cubegl.Shapes.Cube2;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Cube2Renderer implements GLSurfaceView.Renderer {

    private int mWidth;
    private int mHeight;
    private static String TAG = "myRenderer";
    public Cube2 mCube;
    private float mAngle =0;
    private float mTransY=0;
    private float mTransX=0;
    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 40f;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};

    private float xLightPosition, yLightPosition, zLightPosition;

    public Cube2Renderer(Context context) {
        xLightPosition = 0.3f;
        yLightPosition = 0.2f;
        zLightPosition = 0.5f;
    }

    public static int LoadShader(int type, String shaderSrc) {
        int shader;
        int[] compiled = new int[1];

        // создание шейдерного объекта
        shader = GLES30.glCreateShader(type);

        if (shader == 0) {
            return 0;
        }

        // загрузка источника шейдера
        GLES30.glShaderSource(shader, shaderSrc);

        // компиляция
        GLES30.glCompileShader(shader);

        // проверка статуса
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.e(TAG, "Erorr!!!!");
            Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    ///
    // инициализация шейдера и программы
    //
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // uncomment for light scene background color
        //GLES30.glClearColor(0.9f, .9f, 0.9f, 0.9f);

        mCube = new Cube2();
    }

    public void onDrawFrame(GL10 glUnused) {

        // очищение буфера цвета
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // установка позиции камеры
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // задание вращения кубу
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.translateM(mRotationMatrix, 0, mTransX, mTransY, 0);

        // можно настроить скорость (mAngle), и направление вращения
        Matrix.rotateM(mRotationMatrix, 0, mAngle, 1.0f, 1.0f, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        mCube.draw(mMVPMatrix);

        // изменение угла для возможности вращения
        mAngle+=.4;

    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        mWidth = width;
        mHeight = height;
        GLES30.glViewport(0, 0, mWidth, mHeight);
        float aspect = (float) width / height;

        Matrix.perspectiveM(mProjectionMatrix, 0, 53.13f, aspect, Z_NEAR, Z_FAR);
    }

    // перемещение куба
    public float getY() {
        return mTransY;
    }

    public void setY(float mY) {
        mTransY = mY;
    }

    public float getX() {
        return mTransX;
    }

    public void setX(float mX) {
        mTransX = mX;
    }
}
