#version 430

uniform float s_all;
uniform float xinc, yinc;
uniform int	inColor;
out vec4 myColor;

void main(void)
{ 
  if (gl_VertexID == 0){
  	gl_Position = vec4( .25*s_all+xinc,-0.25*s_all+yinc, 0.0*s_all, 1.0); // Bottom right vertex
  	if (inColor != 0) {
  		myColor = vec4(1.0, 0, 0, 1);
  	}
  	else {
  		myColor = vec4(1.0,1.0,1.0,1);
  	}
  }
  else if (gl_VertexID == 1){
  	gl_Position = vec4(-0.25*s_all+xinc,-0.25*s_all+yinc, 0.0*s_all, 1.0); // Bottom left vertex
  	if (inColor != 0) {
  		myColor = vec4(0, 1.0, 0, 1);
  	}
  	else {
  		myColor = vec4(1.0,1.0,1.0,1);
  	}
  }
  else {
  	gl_Position = vec4( 0*s_all+xinc, 0.25*s_all+yinc, 0.0*s_all, 1.0); // Top vertex
  	if (inColor != 0){
  		myColor = vec4(0, 0, 1.0, 1);
  	}
  	else {
  		myColor = vec4(1.0,1.0,1.0,1);
  	}
  }
}