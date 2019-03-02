package a3;

import graphicslib3D.*;

public class Camera {
	private Point3D localLoc = new Point3D(0,0,0);
	
	private Vector3D U = new Vector3D(1,0,0);
	private Vector3D V = new Vector3D(0,1,0);
	private Vector3D N = new Vector3D(0,0,1);
	
	public void Camera() {
	}
	
	public void setCameraPos(float x, float y, float z) {
		localLoc = new Point3D(x,y,z);
	}
	
	public void setCameraRot(Vector3D[] rotVectors) {
		U = rotVectors[0];
		V = rotVectors[1];
		N = rotVectors[2];
	}
	public void pan(float amt) {
		Matrix3D v_rot = new Matrix3D();
		v_rot.rotate(amt, V);
		U= U.mult(v_rot);
		N= N.mult(v_rot);
	}
	
	public void pitch(float amt) {
		Matrix3D v_rot = new Matrix3D();
		v_rot.rotate(amt, U);
		V= V.mult(v_rot);
		N= N.mult(v_rot);
	}
	
	public void roll(float amt) {
		Matrix3D v_rot = new Matrix3D();
		v_rot.rotate(amt, N);
		V= V.mult(v_rot);
		U= U.mult(v_rot);
	}
	
	public void translateX(float amt) {
		Point3D U_mov = new Point3D(U.normalize());
		U_mov = U_mov.mult(amt);
		localLoc = localLoc.add(U_mov);
	}
	public void translateY(float amt) {
		Point3D V_mov = new Point3D(V.normalize());
		V_mov = V_mov.mult(amt);
		localLoc = localLoc.add(V_mov);
	}
	public void translateZ(float amt) {
		Point3D Z_mov = new Point3D(N.normalize());
		Z_mov = Z_mov.mult(-amt);
		localLoc = localLoc.add(Z_mov);
	}
	
	public Matrix3D computeView() {
		Matrix3D trans = new Matrix3D();
		trans.setToIdentity();
		
		Matrix3D rotate = new Matrix3D();
		rotate.setToIdentity();
		
		trans.setCol(3, new Vector3D(-localLoc.getX(), -localLoc.getY(), -localLoc.getZ(), 1));
		rotate.setRow(0, U);
		rotate.setRow(1, V);
		rotate.setRow(2, N);
		
		rotate.concatenate(trans);
		return rotate;
	}
	
	public Point3D getLoc() { return localLoc; }
	public float getX() {return (float) localLoc.getX();}
	public float getY() {return (float) localLoc.getY();}
	public float getZ() {return (float) localLoc.getZ();}
}
