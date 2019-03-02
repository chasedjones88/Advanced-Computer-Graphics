package a1;

public class Vec3 {
	private float x,y,z; 
	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void transform(float xinc, float yinc, float zinc) {
		this.x = this.x+xinc;
		this.y = this.y+yinc;
		this.z = this.z+zinc;
	}
	
	public float x() {return x;}
	public void x(float x) {this.x=x;}
	public float y() {return y;}
	public void y(float y) {this.y=y;}
	public float z() {return z;}
	public void z(float z) {this.z=z;}
}
