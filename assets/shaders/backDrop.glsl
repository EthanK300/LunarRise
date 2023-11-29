#type vertex
#version 460
layout (location=0) in vec3 aPos;
layout (location=1) in vec4 aColor;
layout (location=2) in vec2 aTexCoords;


uniform mat4 uProjection;
uniform mat4 uView;

out vec4 fColor;
out vec2 fTexCoords;


void main()
{
    fColor = aColor;
    fTexCoords = aTexCoords;


    gl_Position = vec4(aPos, 1.0f);
}

#type fragment
#version 460

in vec4 fColor;
in vec2 fTexCoords;

uniform float uTime;
uniform sampler2D TEX_SAMPLER;

out vec4 color;

void main()
{
    color = texture(TEX_SAMPLER, fTexCoords);
}