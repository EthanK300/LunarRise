#type vertex
#version 460

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;

out vec2 TexCoord;

void main()
{
    gl_Position = vec4(aPos, 1.0);
    TexCoord = vec2(aTexCoord.x, aTexCoord.y);
}
#type fragment
#version 460

in vec2 TexCoord;

uniform sampler2D image;

out vec4 color;

void main()
{
    color = texture(image, TexCoord);
}