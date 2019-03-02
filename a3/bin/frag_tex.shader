#version 430

in vec2 tc;
flat in int linenumber;
out vec4 fragColor;

in vec4 shadow_coord;
in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;

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

uniform int isLine;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform int materialNum;
layout (binding=0) uniform sampler2DShadow shTex;
layout (binding=1) uniform sampler2D s;

void main(void)
{	
	/*
	if(isLine == 1){
		if (linenumber == 0){
			fragColor = vec4(1, 0, 0, 1);
		}
		else if (linenumber == 1){
			fragColor = vec4(0, 1, 0, 1);
		}
		else{ fragColor = vec4(0, 0, 1, 1); }
	}
	else {*/ 
		float in Shadow = textureProj(shTex, shadow_coord);
		vec4 lightColor = vec4((globalAmbient * material.ambient + light.ambient*material.ambient).xyz, 1.0f);
	
		vec3 L = normalize(varyingLightDir);
		vec3 N = normalize(varyingNormal);
		vec3 V = normalize(-varyingVertPos);
		
		float cosTheta = dot(L,N);
		
		vec3 H = varyingHalfVector;	
		
		float cosPhi = dot(H,N);
		
		vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta, 0.0);
		vec3 specular = light.specular.xyz * material.specular.xyz
					* pow(max(cosPhi,0.0), material.shininess*3);
		
		if(inShadow!= 0.0){
			lightColor += vec4(diffuse + specular,1.0);
		}
		fragColor = 0.5 * texture(s, tc) + 0.5 * lightColor;
	//}
}