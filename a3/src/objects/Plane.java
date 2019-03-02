package objects;

public class Plane {
	private float [] vertices = {
			-1f, 0f, -1f, 1f, 0f, -1f, 1f, 0f, 1f,
			1f, 0f, 1f, -1f, 0f, 1f, -1f, 0f, -1f
			};
	private float [] texture_verts = {
			0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f
			};
	private float[] normals = {
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
			};
	
	public Plane() {}
	public float[] getVerts() {return vertices;}
	public float[] getTexVerts() {return texture_verts;}
	public float[] getNormals() {return normals;}
	public static int getNumVertices() {return 6;}
}
