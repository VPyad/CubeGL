precision mediump float;

uniform vec3 u_LightPos;       	// позиция источника света
uniform sampler2D u_Texture;    // текстура

varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_TexCoordinate;

void main()
{
	// затухнание света
    float distance = length(u_LightPos - v_Position);

	// получение направление света
    vec3 lightVector = normalize(u_LightPos - v_Position);

    float diffuse = max(dot(v_Normal, lightVector), 0.0);

    diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance)));

    diffuse = diffuse + 0.7;

	// получение окончатлеьного цвета
    gl_FragColor = (diffuse * texture2D(u_Texture, v_TexCoordinate));
  }

