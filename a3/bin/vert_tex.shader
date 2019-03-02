#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
layout (location = 2) in vec3 vertNormal;

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;
out vec2 tc; // texture coord
flat out int linenumber; // for drawing axes
out vec4 shadow_coord;

layout (binding=0) uniform sampler2D s;

struct PositionalLight{
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material{
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform int isLine; // for drawing axes

// input matrices
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform int materialNum;



void main(void)
{	
	shadow_coord = shadowMVP2*vec4(vertPos, 1.0);

	varyingVertPos = (mv_matrix *vec4(position, 1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	varyingNormal = (norm_matrix*vec4(vertNormal, 1.0)).xyz;
	
	varyingHalfVector =
		normalize(normalize(varyingLightDir) + normalize(-varyingVertPos)).xyz;

	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
	tc = tex_coord;
	if(gl_VertexID == 0 || gl_VertexID == 1) { linenumber = 0;}
	else if(gl_VertexID == 2 || gl_VertexID == 3) { linenumber = 1;}
	else { linenumber = 2; }
}