uniform mat4 u_MVPMatrix;		// model/view/projection matrix.
uniform mat4 u_MVMatrix;		// model/view matrix.

attribute vec4 a_Position;		// Per-vertex position information we will pass in.
attribute vec3 a_Normal;		// Per-vertex normal information we will pass in.
attribute vec2 a_TexCoordinate; // Per-vertex texture coordinate information we will pass in.

varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_TexCoordinate;

void main()
{
	// преобразование вершин
	v_Position = vec3(u_MVMatrix * a_Position);

	// передача координат текстуры
	v_TexCoordinate = a_TexCoordinate;
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));

	gl_Position = u_MVPMatrix * a_Position;
}