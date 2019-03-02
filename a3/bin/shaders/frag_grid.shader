#version 430 core

flat in int index;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

void main(void) {
    color = vec4(.5, .5, .5, 1);
}