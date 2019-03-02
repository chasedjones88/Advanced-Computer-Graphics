#version 430

layout (location = 0) in vec3 position;

flat out int index;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;


void main(void) {
    gl_Position = proj_matrix * mv_matrix * vec4(position, 1.0);
    index = gl_VertexID;
}

