package com.example.vpyad.cubegl.Shapes;

import android.opengl.GLES30;
import android.util.Log;

import com.example.vpyad.cubegl.Renderers.Cube2Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Cube2 {
    private int mProgramObject;
    private int mMVPMatrixHandle;
    private int mColorHandle;
    private FloatBuffer mVertices;

    // size of cube
    float size = 0.5f;

    // initial data for cube
    float[] mVerticesData = new float[]{
            ////////////////////////////////////////////////////////////////////
            // FRONT
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, size, // top-left
            -size, -size, size, // bottom-left
            size, -size, size, // bottom-right
            // Triangle 2
            size, -size, size, // bottom-right
            size, size, size, // top-right
            -size, size, size, // top-left
            ////////////////////////////////////////////////////////////////////
            // BACK
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, -size, // top-left
            -size, -size, -size, // bottom-left
            size, -size, -size, // bottom-right
            // Triangle 2
            size, -size, -size, // bottom-right
            size, size, -size, // top-right
            -size, size, -size, // top-left

            ////////////////////////////////////////////////////////////////////
            // LEFT
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, -size, // top-left
            -size, -size, -size, // bottom-left
            -size, -size, size, // bottom-right
            // Triangle 2
            -size, -size, size, // bottom-right
            -size, size, size, // top-right
            -size, size, -size, // top-left
            ////////////////////////////////////////////////////////////////////
            // RIGHT
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            size, size, -size, // top-left
            size, -size, -size, // bottom-left
            size, -size, size, // bottom-right
            // Triangle 2
            size, -size, size, // bottom-right
            size, size, size, // top-right
            size, size, -size, // top-left

            ////////////////////////////////////////////////////////////////////
            // TOP
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, size, -size, // top-left
            -size, size, size, // bottom-left
            size, size, size, // bottom-right
            // Triangle 2
            size, size, size, // bottom-right
            size, size, -size, // top-right
            -size, size, -size, // top-left
            ////////////////////////////////////////////////////////////////////
            // BOTTOM
            ////////////////////////////////////////////////////////////////////
            // Triangle 1
            -size, -size, -size, // top-left
            -size, -size, size, // bottom-left
            size, -size, size, // bottom-right
            // Triangle 2
            size, -size, size, // bottom-right
            size, -size, -size, // top-right
            -size, -size, -size // top-left
    };

    float colorcyan[] = Cube2Color.cyan();
    float colorblue[] = Cube2Color.blue();
    float colorred[] = Cube2Color.red();
    float colorgray[] = Cube2Color.gray();
    float colorgreen[] = Cube2Color.green();
    float coloryellow[] = Cube2Color.yellow();

    // вершинный шейдер
    String vShaderStr =
            "#version 300 es 			  \n" // version of shader
                    + "uniform mat4 uMVPMatrix;     \n"
                    + "in vec4 vPosition;           \n" // input parameter
                    + "void main()                  \n"
                    + "{                            \n"
                    + "   gl_Position = uMVPMatrix * vPosition;  \n"
                    + "}                            \n";

    // фрагментный шейдер
    String fShaderStr =
            "#version 300 es		 			          	\n"
                    + "precision mediump float;					  	\n" // точность вычисления
                    + "uniform vec4 vColor;	 			 		  	\n"
                    + "out vec4 fragColor;	 			 		  	\n"
                    + "void main()                                  \n"
                    + "{                                            \n"
                    + "  fragColor = vColor;                    	\n"
                    + "}                                            \n";

    String TAG = "Cube";

    public Cube2() {
        //установка вершин куба
        mVertices = ByteBuffer
                .allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVerticesData);
        mVertices.position(0);

        // подготовка шейдеров
        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        // загрузка шейдеров
        vertexShader = Cube2Renderer.LoadShader(GLES30.GL_VERTEX_SHADER, vShaderStr);
        fragmentShader = Cube2Renderer.LoadShader(GLES30.GL_FRAGMENT_SHADER, fShaderStr);

        // создание программного opengl
        programObject = GLES30.glCreateProgram();

        if (programObject == 0) {
            Log.e(TAG, "something went wrong");
            return;
        }

        // добавление шейдеров к программе
        GLES30.glAttachShader(programObject, vertexShader);
        GLES30.glAttachShader(programObject, fragmentShader);
        GLES30.glBindAttribLocation(programObject, 0, "vPosition");

        // связываем созданную программу
        GLES30.glLinkProgram(programObject);

        // проверяем статус "свзяывания"
        GLES30.glGetProgramiv(programObject, GLES30.GL_LINK_STATUS, linked, 0);

        if (linked[0] == 0) {
            Log.e(TAG, "Error linking program:");
            Log.e(TAG, GLES30.glGetProgramInfoLog(programObject));
            GLES30.glDeleteProgram(programObject);
            return;
        }

        mProgramObject = programObject;
    }

    public void draw(float[] mvpMatrix) {
        GLES30.glUseProgram(mProgramObject);

        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramObject, "uMVPMatrix");
        Cube2Renderer.checkGlError("glGetUniformLocation");

        mColorHandle = GLES30.glGetUniformLocation(mProgramObject, "vColor");

        // Применение преобразования проекции и представления
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        Cube2Renderer.checkGlError("glUniformMatrix4fv");

        int VERTEX_POS_INDX = 0;
        mVertices.position(VERTEX_POS_INDX);

        GLES30.glVertexAttribPointer(VERTEX_POS_INDX, 3, GLES30.GL_FLOAT,
                false, 0, mVertices);
        GLES30.glEnableVertexAttribArray(VERTEX_POS_INDX);

        // отрисовка куба
        int startPos = 0;
        int verticesPerface = 6;

        // фронтальная сторона
        GLES30.glUniform4fv(mColorHandle, 1, colorblue, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        // задняя сторона
        GLES30.glUniform4fv(mColorHandle, 1, colorcyan, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        // левая сторона
        GLES30.glUniform4fv(mColorHandle, 1, colorred, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        // правая сторона
        GLES30.glUniform4fv(mColorHandle, 1, colorgray, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        // верхняя
        GLES30.glUniform4fv(mColorHandle, 1, colorgreen, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
        startPos += verticesPerface;

        // нижняя сторона
        GLES30.glUniform4fv(mColorHandle, 1, coloryellow, 0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerface);
    }
}
