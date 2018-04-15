package com.example.vpyad.cubegl.Renderers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.vpyad.cubegl.R;
import com.example.vpyad.cubegl.Utils.RawResourceReader;
import com.example.vpyad.cubegl.Utils.ShaderHelper;
import com.example.vpyad.cubegl.Utils.TextureHelper;

public class TextureCubeRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "CubeGL";

    private final Context mActivityContext;
    private float[] mModelMatrix = new float[16];
    // матрица представления
    private float[] mViewMatrix = new float[16];
    // матрица проекции
    private float[] mProjectionMatrix = new float[16];

    private float[] mMVPMatrix = new float[16];
    private final float[] mAccumulatedRotation = new float[16];
    private final float[] mCurrentRotation = new float[16];
    private float[] mTemporaryMatrix = new float[16];
    private float[] mLightModelMatrix = new float[16];

    private final FloatBuffer mCubePositions;
    private final FloatBuffer mCubeNormals;
    private final FloatBuffer mCubeTextureCoordinates;
    private final FloatBuffer mCubeTextureCoordinatesForPlane;

    private int mMVPMatrixHandle;
    private int mMVMatrixHandle;
    private int mLightPosHandle;
    private int mTextureUniformHandle;
    private int mPositionHandle;
    private int mNormalHandle;
    private int mTextureCoordinateHandle;
    private final int mBytesPerFloat = 4;
    private final int mPositionDataSize = 3;
    private final int mNormalDataSize = 3;
    private final int mTextureCoordinateDataSize = 2;

    private final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    private int mProgramHandle;
    private int mPointProgramHandle;

    private int mBrickDataHandle;
    private int mGrassDataHandle;

    public volatile float mDeltaX;
    public volatile float mDeltaY;

    public TextureCubeRenderer(final Context activityContext) {
        mActivityContext = activityContext;

        // Define points for a cube.

        float size = 0.65f;
        // X, Y, Z
        final float[] cubePositionData =
                {
                        // Front face
                        -size, size, size,
                        -size, -size, size,
                        size, size, size,
                        -size, -size, size,
                        size, -size, size,
                        size, size, size,

                        // Right face
                        size, size, size,
                        size, -size, size,
                        size, size, -size,
                        size, -size, size,
                        size, -size, -size,
                        size, size, -size,

                        // Back face
                        size, size, -size,
                        size, -size, -size,
                        -size, size, -size,
                        size, -size, -size,
                        -size, -size, -size,
                        -size, size, -size,

                        // Left face
                        -size, size, -size,
                        -size, -size, -size,
                        -size, size, size,
                        -size, -size, -size,
                        -size, -size, size,
                        -size, size, size,

                        // Top face
                        -size, size, -size,
                        -size, size, size,
                        size, size, -size,
                        -size, size, size,
                        size, size, size,
                        size, size, -size,

                        // Bottom face
                        size, -size, -size,
                        size, -size, size,
                        -size, -size, -size,
                        size, -size, size,
                        -size, -size, size,
                        -size, -size, -size,
                };

        // X, Y, Z нормаль
        final float[] cubeNormalData =
                {
                        // Front face
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,
                        0.0f, 0.0f, 1.0f,

                        // Right face
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,
                        1.0f, 0.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 0.0f, -1.0f,

                        // Left face
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,
                        -1.0f, 0.0f, 0.0f,

                        // Top face
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,
                        0.0f, 1.0f, 0.0f,

                        // Bottom face
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f,
                        0.0f, -1.0f, 0.0f
                };

        // S, T (or X, Y) координаты текстур
        final float[] cubeTextureCoordinateData =
                {
                        // Front face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Right face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Left face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Top face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,

                        // Bottom face
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f
                };

        // S, T (or X, Y) коордитаны текступ земли
        final float[] cubeTextureCoordinateDataForPlane =
                {
                        // Front face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Right face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Back face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Left face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Top face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f,

                        // Bottom face
                        0.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 0.0f,
                        0.0f, 25.0f,
                        25.0f, 25.0f,
                        25.0f, 0.0f
                };

        // инициализация буфферов
        mCubePositions = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubePositions.put(cubePositionData).position(0);

        mCubeNormals = ByteBuffer.allocateDirect(cubeNormalData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeNormals.put(cubeNormalData).position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

        mCubeTextureCoordinatesForPlane = ByteBuffer.allocateDirect(cubeTextureCoordinateDataForPlane.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinatesForPlane.put(cubeTextureCoordinateDataForPlane).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // установка черного фона
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // позиция наблюдателя
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.0f; // -0.5f - original

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f; // 1.0f; // original
        final float upZ = 0.0f;

        // установка матрица представления
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        // загрузка шейдеров
        final String vertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_vertex_shader_tex_and_light);
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.per_pixel_fragment_shader_tex_and_light);

        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                new String[]{"a_Position", "a_Normal", "a_TexCoordinate"});

        final String pointVertexShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_vertex_shader);
        final String pointFragmentShader = RawResourceReader.readTextFileFromRawResource(mActivityContext, R.raw.point_fragment_shader);

        final int pointVertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = ShaderHelper.createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[]{"a_Position"});

        // загрузка текстур
        mBrickDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.water);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        mGrassDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.grass);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        Matrix.setIdentityM(mAccumulatedRotation, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 1000.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // поворот земли
        long time = SystemClock.uptimeMillis() % 10000L;
        long slowTime = SystemClock.uptimeMillis() % 100000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        float slowAngleInDegrees = (360.0f / 100000.0f) * ((int) slowTime);

        // установка программы света
        GLES20.glUseProgram(mProgramHandle);

        // отрисовка куба
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // вычисление позиции света
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -2.0f);
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 1.5f, 0.0f); // originally x = 0.0f, y = 0.0f, z = 3.5f

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.8f, -3.5f);

        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
        mDeltaX = 0.0f;
        mDeltaY = 0.0f;

        Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

        Matrix.multiplyMM(mTemporaryMatrix, 0, mModelMatrix, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBrickDataHandle);

        GLES20.glUniform1i(mTextureUniformHandle, 0);
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        drawCube();

        // отрисовка земли
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -2.0f, -5.0f);
        Matrix.scaleM(mModelMatrix, 0, 25.0f, 1.0f, 25.0f);
        Matrix.rotateM(mModelMatrix, 0, slowAngleInDegrees, 0.0f, 1.0f, 0.0f);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // привязка текстуры
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGrassDataHandle);

        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mCubeTextureCoordinatesForPlane.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinatesForPlane);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        drawCube();

        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();
    }

    private void drawCube() {

        mCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                0, mCubePositions);

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mCubeNormals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
                0, mCubeNormals);

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
    }

    // отрисовка точки, обозначающий свет
    private void drawLight() {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }
}
