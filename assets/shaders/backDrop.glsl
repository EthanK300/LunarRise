#type vertex
#version 460

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;

out vec2 TexCoord;

void main()
{
    gl_Position = vec4(position, 1.0);
    TexCoord = texCoord;
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