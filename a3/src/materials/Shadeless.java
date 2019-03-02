package materials;
import graphicslib3D.*;

public class Shadeless extends Material{
	public Shadeless(){
		this.setAmbient(new float[] {1f,1f,1f,1f});
		this.setDiffuse(new float[] {0f,0f,0f,1f});
		this.setSpecular(new float[] {0f,0f,0f,1f});
		this.setShininess(0.1f);
	}
}
