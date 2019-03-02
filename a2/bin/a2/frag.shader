#version 430

in vec2 tc;
flat in int linenumber;
out vec4 color;

uniform int isLine;
uniform mat4 view_matrix;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout (binding=0) uniform sampler2D s;

void main(void)
{	
	if(isLine == 1){
		if (linenumber == 0){
			color = vec4(1, 0, 0, 1);
		}
		else if (linenumber == 1){
			color = vec4(0, 1, 0, 1);
		}
		else{ color = vec4(0, 0, 1, 1); }
	}
	else { 
		color = texture(s,tc);
	}
}