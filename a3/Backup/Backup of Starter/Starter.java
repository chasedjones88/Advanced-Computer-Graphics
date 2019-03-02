package a3;

import materials.*;
import objects.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import java.awt.BorderLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.nio.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.common.nio.Buffers;

import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import graphicslib3D.*;
import graphicslib3D.light.AmbientLight;
import graphicslib3D.light.PositionalLight;
import graphicslib3D.shape.Sphere;
import graphicslib3D.shape.Torus;

import java.lang.Math.*;

public class Starter extends JFrame implements GLEventListener, MouseWheelListener
{
	private int SPHERE_VERTS = 0, SPHERE_TEXTURES = 1, SPHERE_NORMALS = 1,
				SKYBOX_VERTS = 3, SKYBOX_TEXTURES = 4,
				D8_VERTS = 5, D8_TEXTURES = 6, D8_NORMALS = 7,
				PLANE_VERTS = 8, PLANE_TEXTURES = 9, PLANE_NORMALS = 10,
				AXES_VERTS = 11,
				SHUTTLE_VERTS = 12, SHUTTLE_TEXTURES = 13, SHUTTLE_NORMALS = 14;
	
	private GLCanvas myCanvas;
	private String[] vShaderSource, vTexShaderSource, fTexShaderSource, vPointShaderSource, fPointShaderSource, vLineShaderSource, fLineShaderSource;
	private int shadow_rendering_program, passTwo_rendering_program, point_rendering_program, axes_rendering_program;
	private int vao[] = new int[1];
	private int vbo[] = new int[50];
	private int mv_location, proj_location, vertexLoc, n_location;
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	//////////////////
	// Primatives
	private float tAmt = 1f, rAmt = 45f, deltaTime;
	private long currentTime, prevTime;
	
	//////////////////
	// Objects
	Camera camera = new Camera();

	//////////////////
	// location of Sphere, Shuttle, D8, Plane, Camera (starting location) and light
	private Point3D shuttleLoc = new Point3D(0,1,2);
	private Point3D d8Loc = new Point3D(2,1,0);
	private Point3D planeLoc = new Point3D(0,0,0);
	private Point3D cameraStartLoc = new Point3D(5,2,5);
	private Point3D lightLoc = new Point3D(5, 1, 0);
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	//////////////////
	// Lights
	private float [] globalAmbient = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	
	//////////////////
	// Shadows 
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();
	
	//////////////////
	// Display toggles
	private boolean displayAxes = true,
					togglePosLight = true;
	
	//////////////////
	// Shapes
	ImportedModel myShuttle = new ImportedModel("../shuttle.obj");
	Plane myPlane = new Plane();
	D8 myD8 = new D8();
	private int numShuttleVertices;
	
	//////////////////
	// Textures
	private int thisTexture,
				shuttleTexture, 
				skyboxTexture, 
				goldTexture, 
				d8Texture, 
				checkTexture;
	private Texture joglShuttleTexture, 
					joglSkyboxTexture, 
					joglGoldTexture, 
					jogld8Texture, 
					joglCheckTexture;
	
	//////////////////
	// Materials
	private Material thisMaterial;
	private Material sphereMaterial = Material.GOLD;
	private Material shuttleMaterial = new Material();
	private Material d8Material = new Material();
	private Material planeMaterial = new Material();
	private Material skyboxMaterial = new Material() ;
	
	//////////////////
	// Buttons for UI
	private JButton resetButton, lightResetButton;

	//////////////////
	// Starter
	public Starter()
	{	
		//////////////////
		// Window
		setTitle("Chase Jones - Assignment #3");
		setSize(1400, 1000);
		myCanvas = new GLCanvas(); // Create frame
		myCanvas.addGLEventListener(this);
		
		//////////////////
		// Layout
		JPanel topPanel = new JPanel();
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(myCanvas, BorderLayout.CENTER);
		
		////////////
		// UI Buttons
		resetButton = new JButton("Reset");
		topPanel.add(resetButton);
		CustomCommand resetCommand = new CustomCommand("Reset", "Resets camera", this);
		resetButton.setAction(resetCommand);
		
		lightResetButton = new JButton("Reset light");
		topPanel.add(lightResetButton);
		CustomCommand resetLightCommand = new CustomCommand("ResetLight", "Resets light", this);
		lightResetButton.setAction(resetLightCommand);
		
		///////////
		// Listeners
		///////////
		
		//////////////////
		// Mouse
		this.addMouseWheelListener(this);
		
		///////////
		// Keys
		String [][] keys = {{"w", "Moves the Camera forward", "foward"},
							{"a", "Moves the Camera to the right", "right"},
							{"s", "Moves the camera backward", "back"},
							{"d", "Moves the camera to the left", "left"},
							{"q", "Moves the camera up", "up"},
							{"e", "Moves the camera down", "down"},
							{"UP", "Pitches the camera up", "pUP"},
							{"DOWN", "Pitches the camera down", "pDOWN"},
							{"LEFT", "Pans the camera to the left", "pLEFT"},
							{"RIGHT", "Pans the camera to the right", "pRIGHT"},
							{"NUMPAD1", "Rolls the camera to the left", "rLEFT"},
							{"NUMPAD3", "Rolls the camera to the right", "rRIGHT"},
							{"SPACE", "Displays the world axes", "axis"},
							{"o", "Moves the light forward", "lfoward"},
							{"k", "Moves the light backward", "lback"},
							{"j", "Move the light left", "lleft"},
							{"l", "Move the light right", "lright"},
							{"p", "Move the light down", "ldown"},
							{"i", "Move the light up", "lup"},
							{"t", "Toggles positional light", "tpositional"}};
		CustomCommand [] cc = new CustomCommand[keys.length]; 
		
		// Make a command and link it to the key for every key entry in keys[]
		for(int i = 0; i < keys.length; i++) {
			// c-key
			CustomCommand cCommand = new CustomCommand(keys[i][0], keys[i][1], this); // create command
			cc[i] = cCommand;
			JComponent contentPane = (JComponent) this.getContentPane();
			// get input map for content pane
			int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
			InputMap imap = contentPane.getInputMap(mapName);
			// create keystroke
			KeyStroke stroke;
			if(keys[i][0].length() == 1)
				stroke = KeyStroke.getKeyStroke(keys[i][0].charAt(0));
			else
				stroke = KeyStroke.getKeyStroke(keys[i][0]);
			// put keystroke in inputmap with label "color"
			imap.put(stroke, keys[i][2]);
			// get actionmap for content pane and put command into action map with label "color"
			ActionMap amap = contentPane.getActionMap();
			amap.put(keys[i][2], cc[i]);
			this.requestFocus();
		}
		// End Keys
		//////////////////
		
		
		//////////////////
		// Set Active Pane
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 26);
		animator.start();
	}
	
	// Handler for all command elements, switches based on name of command
	public void CommandUpdate(String name) {
		switch(name) { // 
			case "w":	// Move forward
				camera.translateZ(tAmt*deltaTime);
				break;
			case "s":   // Move backward
				camera.translateZ(-tAmt*deltaTime);
				break;
			case "a":	// Move left
				camera.translateX(-tAmt*deltaTime);
				break;
			case "d":	// Move right
				camera.translateX(tAmt*deltaTime);
				break;
			case "q":	// Move up
				camera.translateY(tAmt*deltaTime);
				break;
			case "e":	// Move down
				camera.translateY(-tAmt*deltaTime);
				break;
				
			case "UP":	// Pitch up
				camera.pitch(rAmt*deltaTime);
				break;
			case "DOWN":// Pitch down
				camera.pitch(-rAmt*deltaTime);
				break;
			case "LEFT":// Pan left
				camera.pan(rAmt*deltaTime);
				break;
			case "RIGHT":// Pan Right
				camera.pan(-rAmt*deltaTime);
				break;
			case "NUMPAD1":// Roll left
				camera.roll(rAmt*deltaTime);
				break;
			case "NUMPAD3":// Roll right
				camera.roll(-rAmt*deltaTime);
				break;
				
			case "o": // move light z+
				lightLoc.setZ(lightLoc.getZ() + tAmt*deltaTime);
				break;
			case "k": // move light z-
				lightLoc.setZ(lightLoc.getZ() - tAmt*deltaTime);
				break;
			case "j": // move light x-
				lightLoc.setX(lightLoc.getX() - tAmt*deltaTime);
				break;
			case "l": // move light x+
				lightLoc.setX(lightLoc.getX() + tAmt*deltaTime);
				break;
			case "p": // move light y-
				lightLoc.setY(lightLoc.getY() - tAmt*deltaTime);
				break;
			case "i": // move light y+
				lightLoc.setY(lightLoc.getY() + tAmt*deltaTime);
				break;
				
			case "SPACE":
				displayAxes = !displayAxes;
				break;
			case "t":
				togglePosLight = !togglePosLight;
				break;
				
			//////////////////
			// Reset camera view to default
			case "Reset":
				camera.setCameraPos(cameraStartLoc);
				camera.setCameraRot(new Vector3D[] {new Vector3D(1,0,0), new Vector3D(0,1,0), new Vector3D(0,0,1)});
				camera.pan(45);
				System.out.println("Returning Camera to starting orientation.");
				break;
			case "ResetLight":
				lightLoc = new Point3D(1,0,1);
				System.out.println("Returning light to 1,1,1");
				break;
		}
	}
	
	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		currentTime = System.currentTimeMillis();
		deltaTime = (float)(currentTime - prevTime)/1000f;
		prevTime = currentTime;

		currentLight.setPosition(lightLoc);
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		float bkg[] = { 0f, 0f, 0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);

		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts

		passOne();

		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);

		gl.glDrawBuffer(GL_FRONT);

		passTwo();
	}
	
	public void passOne()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(shadow_rendering_program);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		///////////////////
		// Move the shuttle
		m_matrix.setToIdentity();
		m_matrix.translate(shuttleLoc.getX(), shuttleLoc.getY(), shuttleLoc.getZ());
		m_matrix.rotate(-135, up);
		m_matrix.scale(25, 25, 25);
		
		// set up shuttle vertices buffers and draw
		drawPassOneObject(SHUTTLE_VERTS, numShuttleVertices);
		
		///////////////////
		// Move the d8
		m_matrix.setToIdentity();
		m_matrix.translate(d8Loc.getX(), d8Loc.getY(), d8Loc.getZ());
		m_matrix.scale(.5, 1, .5);

		// set up d8 vertices buffers and draw
		drawPassOneObject(D8_VERTS, D8.getNumVertices());
		
		///////////////////
		// Move the Plane
		m_matrix.setToIdentity();
		m_matrix.translate(planeLoc.getX(), planeLoc.getY(), planeLoc.getZ());
		m_matrix.scale(15, 1, 15);
		m_matrix.rotate(180, new Vector3D(1,0,0));

		// set up plane vertices buffers and draw
		drawPassOneObject(PLANE_VERTS, D8.getNumVertices());
	}
	
	public void drawPassOneObject(int bufNum, int numOfVertices) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		int shadow_location = gl.glGetUniformLocation(shadow_rendering_program, "shadowMVP");
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[bufNum]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numOfVertices);
	}
	
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();	
	
		gl.glUseProgram(passTwo_rendering_program);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		//  build the VIEW matrix
		v_matrix = camera.computeView();	
		mv_location = gl.glGetUniformLocation(passTwo_rendering_program, "mv_matrix");
		proj_location = gl.glGetUniformLocation(passTwo_rendering_program, "proj_matrix");
		n_location = gl.glGetUniformLocation(passTwo_rendering_program, "normalMat");
		int shadow_location = gl.glGetUniformLocation(passTwo_rendering_program,  "shadowMVP");

		///////////////////
		// draw the shuttle
		thisMaterial = shuttleMaterial;
		thisTexture = shuttleTexture;
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(shuttleLoc.getX(), shuttleLoc.getY(), shuttleLoc.getZ());
		m_matrix.rotate(-135, new Vector3D(0,1,0));
		m_matrix.scale(5, 5, 5);
		
		installLights(passTwo_rendering_program, v_matrix);
		drawPassTwoObject(new int[] {SHUTTLE_VERTS, SHUTTLE_TEXTURES, SHUTTLE_NORMALS}, numShuttleVertices);
		
		///////////////////
		// draw the d8
		thisMaterial = d8Material;
		thisTexture = d8Texture;

		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(d8Loc.getX(), d8Loc.getY(), d8Loc.getZ());
		m_matrix.scale(.5, 1, .5);

		installLights(passTwo_rendering_program, v_matrix);
		drawPassTwoObject(new int[] {D8_VERTS, D8_TEXTURES, D8_NORMALS}, D8.getNumVertices());
		
		///////////////////
		// draw the plane
		thisMaterial = planeMaterial;
		thisTexture = checkTexture;

		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(planeLoc.getX(), planeLoc.getY(), planeLoc.getZ());
		m_matrix.scale(15, 1, 15);
		m_matrix.rotate(180, new Vector3D(1,0,0));

		installLights(passTwo_rendering_program, v_matrix);
		drawPassTwoObject(new int[] {PLANE_VERTS, PLANE_TEXTURES, PLANE_NORMALS}, Plane.getNumVertices());
		
		///////////////////
		// Draw the Point for the light and axes
		if(togglePosLight) {
			gl.glUseProgram(point_rendering_program);
			mv_location = gl.glGetUniformLocation(point_rendering_program, "mv_matrix");
			proj_location = gl.glGetUniformLocation(point_rendering_program, "proj_matrix");

			m_matrix.setToIdentity();
			m_matrix.translate(currentLight.getPosition().getX(), currentLight.getPosition().getY(), currentLight.getPosition().getZ());

			//  build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);

			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);

			gl.glPointSize(5);
			gl.glDrawArrays(GL_POINTS, 0, 1);
		}
        

        if(displayAxes) {
        	// Axes
        	m_matrix.setToIdentity();
        	gl.glUseProgram(axes_rendering_program);
        	mv_location = gl.glGetUniformLocation(axes_rendering_program, "mv_matrix");
        	proj_location = gl.glGetUniformLocation(axes_rendering_program, "proj_matrix");

        	m_matrix.setToIdentity();
        	m_matrix.translate(0, .01, 0);

        	//  build the MODEL-VIEW matrix
        	mv_matrix.setToIdentity();
        	mv_matrix.concatenate(v_matrix);
        	mv_matrix.concatenate(m_matrix);

        	gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
        	gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);

        	gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[AXES_VERTS]);
        	gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        	gl.glEnableVertexAttribArray(0);

        	gl.glDrawArrays(GL_LINES, 0, 6);
        	
        	m_matrix.translate(1, 0, 1);
        	mv_matrix.setToIdentity();
        	mv_matrix.concatenate(v_matrix);
        	mv_matrix.concatenate(m_matrix);
        	gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
        	gl.glDrawArrays(GL_LINES, 0, 6);
        }
	}
	
	public void drawPassTwoObject(int [] loc, int numOfVertices) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int shadow_location = gl.glGetUniformLocation(passTwo_rendering_program,  "shadowMVP");
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);

		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		// set up vertex buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[loc[0]]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up normal buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[loc[2]]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// set up texture buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[loc[1]]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, thisTexture);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numOfVertices);
	}
	
	public Matrix3D lookAt(Point3D eye, Point3D target, Vector3D worldup) {
		Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(worldup)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return look;
	}
	
	private void installLights(int rendering_program, Matrix3D v_matrix)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currentMaterial = thisMaterial;

		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);

		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");

		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		
		if(togglePosLight) {
			gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
			gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
			gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		}
		else {
			gl.glProgramUniform4fv(rendering_program, ambLoc, 1, new float[] {0,0,0,0}, 0);
			gl.glProgramUniform4fv(rendering_program, diffLoc, 1, new float[] {0,0,0,0}, 0);
			gl.glProgramUniform4fv(rendering_program, specLoc, 1, new float[] {0,0,0,0}, 0);
		}
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);

		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}
	
	@Override
	public void init(GLAutoDrawable arg0) 
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		System.out.println("OpenGL Ver: "+gl.glGetString(GL_VERSION)); // Print version of OpenGL
		createShaderPrograms();
		
		currentLight.setQuadraticAtt(5f);
		
		currentTime = System.currentTimeMillis();
		prevTime = currentTime;
		
		// TODO
		//l_matrix.setToIdentity();
		
		camera.setCameraPos(cameraStartLoc);
		camera.setCameraRot(new Vector3D[] {new Vector3D(1,0,0), new Vector3D(0,1,0), new Vector3D(0,0,1)});
		camera.pan(45);
		
		setupVertices();
		setupMaterials();
		setupTextures();
		
		setupShadowBuffers();
		b.setElementAt(0, 0, 0.5f);b.setElementAt(0, 1, 0.0f);
		b.setElementAt(0, 2, 0.0f);b.setElementAt(0, 3, 0.5f);
		b.setElementAt(1, 0, 0.0f);b.setElementAt(1, 1, 0.5f);
		b.setElementAt(1, 2, 0.0f);b.setElementAt(1, 3, 0.5f);
		b.setElementAt(2, 0, 0.0f);b.setElementAt(2, 1, 0.0f);
		b.setElementAt(2, 2, 0.5f);b.setElementAt(2, 3, 0.5f);
		b.setElementAt(3, 0, 0.0f);b.setElementAt(3, 1, 0.0f);
		b.setElementAt(3, 2, 0.0f);b.setElementAt(3, 3, 1.0f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}
	
	public void setupShadowBuffers() 
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();

		gl.glGenFramebuffers(1, shadow_buffer, 0);

		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
					scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
    }
	
	private void setupTextures() {
		joglShuttleTexture = loadTexture("textures/shuttleTex.jpg");
		shuttleTexture = joglShuttleTexture.getTextureObject();
		
		joglSkyboxTexture = loadTexture("textures/cloudSkyBox.jpg");
		skyboxTexture = joglSkyboxTexture.getTextureObject();
		
		joglGoldTexture = loadTexture("textures/gold.jpg");
		goldTexture = joglGoldTexture.getTextureObject();
		
		jogld8Texture = loadTexture("textures/d8.jpg");
		d8Texture = jogld8Texture.getTextureObject();
		
		joglCheckTexture = loadTexture("textures/checkered.jpg");
		checkTexture = joglCheckTexture.getTextureObject();
	}
	
	private void setupMaterials() {
		//Shuttle made of Pewter
		shuttleMaterial.setAmbient(new float[]{.105882f, .058824f, .113725f, 1f});
		shuttleMaterial.setDiffuse(new float[]{.427451f, .470588f, .541176f, 1f});
		shuttleMaterial.setSpecular(new float[]{.333333f, .333333f, .521569f, 1f});
		shuttleMaterial.setShininess(9.84615f);
		
		//d8 made of rubber
		d8Material.setAmbient(new float[]{.02f, .02f, .02f, 1f});
		d8Material.setDiffuse(new float[]{.01f, .01f, .01f, 1f});
		d8Material.setSpecular(new float[]{.4f, .4f, .4f, 1f});
		d8Material.setShininess(10f);
		
		planeMaterial.setAmbient(new float[]{.25f, .25f, .25f, 1f});
		planeMaterial.setDiffuse(new float[]{.4f, .4f, .4f, 1f});
		planeMaterial.setSpecular(new float[]{.774597f, .774597f, .774597f, 1f});
		planeMaterial.setShininess(.6f);
	}
	
	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(15, vbo, 0);	// Change the first arg for each element added to scene
		
		//////////////
		// SKYBOX
		Cube cwCube = new Cube(false); // Creates cube with faces pointing inwards
	
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer skyboxBuf = Buffers.newDirectFloatBuffer(cwCube.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, skyboxBuf.limit()*4, skyboxBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer skyboxTexBuf = Buffers.newDirectFloatBuffer(cwCube.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, skyboxTexBuf.limit()*4, skyboxTexBuf, GL_STATIC_DRAW);

		/////////////
		// d8 
		D8 d8 = new D8();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		FloatBuffer d8Buf = Buffers.newDirectFloatBuffer(d8.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, d8Buf.limit()*4, d8Buf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer d8TextureBuf = Buffers.newDirectFloatBuffer(d8.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, d8TextureBuf.limit()*4, d8TextureBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer d8NorBuf = Buffers.newDirectFloatBuffer(d8.getNormals());
		gl.glBufferData(GL_ARRAY_BUFFER, d8NorBuf.limit()*4, d8NorBuf, GL_STATIC_DRAW);
		
		/////////////
		// Plane
		Plane plane = new Plane();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer planeBuf = Buffers.newDirectFloatBuffer(plane.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, planeBuf.limit()*4, planeBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer planeTextureBuf = Buffers.newDirectFloatBuffer(plane.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, planeTextureBuf.limit()*4, planeTextureBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer planeNorBuf = Buffers.newDirectFloatBuffer(plane.getNormals());
		gl.glBufferData(GL_ARRAY_BUFFER, planeNorBuf.limit()*4, planeNorBuf, GL_STATIC_DRAW);
		
		
		/////////////
		// Axes
		
		Axes axes = new Axes();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer axesBuf = Buffers.newDirectFloatBuffer(axes.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, axesBuf.limit()*4, axesBuf, GL_STATIC_DRAW);
		
		// Shuttle
		Vertex3D[] vertices = myShuttle.getVertices();
		int numObjVertices = myShuttle.getNumVertices();
		
		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices[i]).getX();
			pvalues[i*3+1] = (float) (vertices[i]).getY();
			pvalues[i*3+2] = (float) (vertices[i]).getZ();
			tvalues[i*2]   = (float) (vertices[i]).getS();
			tvalues[i*2+1] = (float) (vertices[i]).getT();
			nvalues[i*3]   = (float) (vertices[i]).getNormalX();
			nvalues[i*3+1] = (float) (vertices[i]).getNormalY();
			nvalues[i*3+2] = (float) (vertices[i]).getNormalZ();
		}
		numShuttleVertices = numObjVertices;
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer monkeyBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, monkeyBuf.limit()*4, monkeyBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer monkeyTextureBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, monkeyTextureBuf.limit()*4, monkeyTextureBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[14]);
		FloatBuffer monkeyNorBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, monkeyNorBuf.limit()*4, monkeyNorBuf, GL_STATIC_DRAW);
	}
	
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		vShaderSource = util.readShaderSource("shaders/vert.shader");
		vTexShaderSource = util.readShaderSource("shaders/vert_tex.shader");
		fTexShaderSource = util.readShaderSource("shaders/frag_tex.shader");
		vPointShaderSource = util.readShaderSource("shaders/vert_point.shader");
		fPointShaderSource = util.readShaderSource("shaders/frag_point.shader");
		vLineShaderSource = util.readShaderSource("shaders/vert_line.shader");
		fLineShaderSource = util.readShaderSource("shaders/frag_line.shader");
		
		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader3 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader4 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int fragmentShader3 = gl.glCreateShader(GL_FRAGMENT_SHADER);
		int fragmentShader4 = gl.glCreateShader(GL_FRAGMENT_SHADER);

		int[] vertCheck = {vertexShader1, vertexShader2, vertexShader3, vertexShader4};
		int[] fragCheck = {fragmentShader2, fragmentShader3, fragmentShader4};
		
		gl.glShaderSource(vertexShader1, vShaderSource.length, vShaderSource, null, 0);
		gl.glShaderSource(vertexShader2, vTexShaderSource.length, vTexShaderSource, null, 0);
		gl.glShaderSource(fragmentShader2, fTexShaderSource.length, fTexShaderSource, null, 0);
		gl.glShaderSource(vertexShader3, vPointShaderSource.length, vPointShaderSource, null, 0);
		gl.glShaderSource(fragmentShader3, fPointShaderSource.length, fPointShaderSource, null, 0);
		gl.glShaderSource(vertexShader4, vLineShaderSource.length, vLineShaderSource, null, 0);
		gl.glShaderSource(fragmentShader4, fLineShaderSource.length, fLineShaderSource, null, 0);

		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);
		gl.glCompileShader(vertexShader3);
		gl.glCompileShader(fragmentShader3);
		gl.glCompileShader(vertexShader4);
		gl.glCompileShader(fragmentShader4);
		
		// Check for compilation errors in vert shaders
		checkOpenGLError();  // can use returned boolean if desired
		for(int i = 0; i < vertCheck.length; i++) {
			gl.glGetShaderiv(vertCheck[i], GL_COMPILE_STATUS, vertCompiled, 0);
			if (vertCompiled[0] == 1)
			{	System.out.println("vertex shader #" +i+ " compilation success");
			} else
			{	System.out.println("vertex shader #" +i+ " compilation failed");
			printShaderLog(vertCheck[i]);
			}
		}
		// Check for compilation errors in frag shaders
		checkOpenGLError();  // can use returned boolean if desired
		for(int i = 0; i < fragCheck.length; i++) {
			gl.glGetShaderiv(fragCheck[i], GL_COMPILE_STATUS, fragCompiled, 0);
			if (fragCompiled[0] == 1)
			{	System.out.println("Fragment shader #" +i+ " compilation success");
			} else
			{	System.out.println("Fragment shader #" +i+ " compilation failed");
			printShaderLog(fragCheck[i]);
			}
		}

		shadow_rendering_program = gl.glCreateProgram();
		passTwo_rendering_program = gl.glCreateProgram();
		point_rendering_program = gl.glCreateProgram();
		axes_rendering_program = gl.glCreateProgram();

		gl.glAttachShader(shadow_rendering_program, vertexShader1);
		gl.glAttachShader(passTwo_rendering_program, vertexShader2);
		gl.glAttachShader(passTwo_rendering_program, fragmentShader2);
		gl.glAttachShader(point_rendering_program, vertexShader3);
		gl.glAttachShader(point_rendering_program, fragmentShader3);
		gl.glAttachShader(axes_rendering_program, vertexShader4);
		gl.glAttachShader(axes_rendering_program, fragmentShader4);

		gl.glLinkProgram(shadow_rendering_program);
		gl.glLinkProgram(passTwo_rendering_program);
		gl.glLinkProgram(point_rendering_program);
		gl.glLinkProgram(axes_rendering_program);
		
		int[] programs = {shadow_rendering_program, passTwo_rendering_program, point_rendering_program, axes_rendering_program};
		
		for(int i=0; i < programs.length; i++) {
			// Check for link errors
			checkOpenGLError();
			gl.glGetProgramiv(programs[i], GL_LINK_STATUS, linked, 0);
			if (linked[0] == 1)
			{	System.out.println("linking succeeded in program #"+i );
			} else
			{	System.out.println("linking failed in program #"+i);
			printProgramLog(programs[i]);
			}
		}
		
	}
	
	public Texture loadTexture(String textureFileName) {
		Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e ) {e.printStackTrace();}
		return tex;
	}
	
	////////////
	// OpenGL methods
	////////////
	private void printShaderLog(int shader)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}
	
	void printProgramLog(int prog)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine length of the program compilation log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}

	// Check if OpenGL has thrown an error;
	// Use after you make a major OpenGL call, like linking or compiling
	private boolean checkOpenGLError() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR)
		{	System.err.println("glError: " + glu.gluErrorString(glErr));
		foundError = true;
		glErr = gl.glGetError();
		}
		return foundError;
	}
	
	// Apply changes in window size to dimensions and placement of objects
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		return r;
	}
	
	// Main
	public static void main(String[] args) 
	{ 
		printVersions(); // Print JOGL and Java versions
		Starter scene = new Starter();
		scene.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	// Prints versions of Java and JOGL
	public static void printVersions() {
		System.out.println("JOGL Ver: "+Package.getPackage("com.jogamp.opengl").toString()
				+"\nJava Ver: "+System.getProperty("java.version"));
	}
	
	// Not implemented yet
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		setupShadowBuffers();
	}
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

}
