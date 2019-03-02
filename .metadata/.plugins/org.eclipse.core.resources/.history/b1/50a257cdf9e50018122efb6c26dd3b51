#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
out vec2 tc;
flat out int linenumber;

uniform int isLine;
uniform mat4 view_matrix;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout (binding=0) uniform sampler2D s;

void main(void)
{	gl_Position = proj_matrix * view_matrix * mv_matrix * vec4(position,1.0);
	tc = tex_coord;
	if(gl_VertexID == 0 || gl_VertexID == 1) { linenumber = 0;}
	else if(gl_VertexID == 2 || gl_VertexID == 3) { linenumber = 1;}
	else { linenumber = 2; }
}