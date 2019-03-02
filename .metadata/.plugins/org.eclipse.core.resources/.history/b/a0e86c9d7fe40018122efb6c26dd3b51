package a2;

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
import graphicslib3D.shape.Sphere;

import java.lang.Math.*;

public class Starter extends JFrame implements GLEventListener, MouseWheelListener
{
	private GLCanvas myCanvas;
	private int rendering_program;
	private GLSLUtils util = new GLSLUtils();
	
	String path = "";
	
	private int vao[] = new int[1];
	private int vbo[] = new int[50];
	private Camera camera = new Camera();
	private Vector3D U = new Vector3D(1,0,0);
	private Vector3D V = new Vector3D(0,1,0);
	private Vector3D N = new Vector3D(0,0,1);
	private float amount = 5f; // Amount to rotate by
	private float tAmt = 1.0f; // Amount to translate by
	
	private boolean displayAxes = true;
	
	Sphere mySphere = new Sphere();
	
	private int sunTexture;
	private Texture joglSunTexture;
	
	private int skyboxTexture;
	private Texture joglSkyboxTexture;
	
	private int moonTexture;
	private Texture joglMoonTexture;
	
	private int d8Texture;
	private Texture jogld8Texture;
	
	private int secondPlanetTexture;
	private Texture joglSecondPlanetTexture;
	
	private int checkTexture;
	private Texture joglCheckTexture;
	
	private MatrixStack mvStack = new MatrixStack(20);
	private int ms = 0; // matrix size for pops
	
	// Buttons for UI
	private JButton resetButton;

	public Starter()
	{	
		setTitle("Chase Jones - Assignment #2");
		setSize(800, 600);
		myCanvas = new GLCanvas(); // Create frame
		myCanvas.addGLEventListener(this);
		
		// Layout
		JPanel topPanel = new JPanel();
		getContentPane().add(topPanel, BorderLayout.NORTH);
		getContentPane().add(myCanvas, BorderLayout.CENTER);
		
		////////////
		// UI Buttons
		////////////
		resetButton = new JButton("Reset");
		topPanel.add(resetButton);
		CustomCommand resetCommand = new CustomCommand("Reset", "Resets triangle", this);
		resetButton.setAction(resetCommand);
		
		///////////
		// Listeners
		///////////
		
		// Mouse
		this.addMouseWheelListener(this);
		
		///////////
		// Keys
		///////////
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
							{"SPACE", "Displays the world axes", "axis"}};
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
		///////////
		///////////
		
		
		///////////
		// Set Active Pane
		///////////
		setVisible(true);
		FPSAnimator animator = new FPSAnimator(myCanvas, 50);
		animator.start();
	}
	
	// Handler for all command elements, switches based on name of command
	public void CommandUpdate(String name) {
		switch(name) { // 
			case "w":
				camera.translateZ(-tAmt);
				break;
			case "s":
				camera.translateZ(tAmt);
				break;
			case "a":
				camera.translateX(-tAmt);
				break;
			case "d":
				camera.translateX(tAmt);
				break;
			case "q":
				camera.translateY(tAmt);
				break;
			case "e":
				camera.translateY(-tAmt);
				break;
				
			case "UP":
				camera.rotate(amount, U);
				break;
			case "DOWN":
				camera.rotate(-amount,U);
				break;
			case "LEFT":
				camera.rotate(amount, V);
				break;
			case "RIGHT":
				camera.rotate(-amount,V);
				break;
			case "NUMPAD1":
				camera.rotate(amount, N);
				break;
			case "NUMPAD3":
				camera.rotate(-amount,N);
				break;
				
			case "SPACE":
				displayAxes = !displayAxes;
				break;
		
			case "Reset": // Reset position to center and stop moving
				camera.setCameraPos(20, 20, 20);
				camera.setCameraRot(new Vector3D(0,0,0));
				camera.rotate(-35, U);
				camera.rotate(45, V);
				System.out.println("Returning Camera to starting orientation.");
				break;
		}
	}
	
	@Override
	public void display(GLAutoDrawable arg0) 
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// Set Background buffer color
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		float bkg[] = {0.0f, 0.0f, 0.0f, 1.0f};
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glUseProgram(rendering_program);
		
		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int view_loc = gl.glGetUniformLocation(rendering_program, "view_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");
		
		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		Matrix3D pMat = perspective(60.0f, aspect, 0.1f, 1000.0f);
		
		double amt = (double)(System.currentTimeMillis())/1000.0;

		////////////
		// Set World center
		pushMatrix();
		mvStack.translate(0,0,0);
		
		/////////////
		// Get matrices to manipulate vert shader
		
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(view_loc, 1, false, camera.computeView().getFloatValues(), 0);
		
		////////////
		// Draw World axes
		
		int offset_color = gl.glGetUniformLocation(rendering_program, "isLine");
		gl.glProgramUniform1i(rendering_program, offset_color, 1);
		
		if(displayAxes) {
			gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);

			gl.glDrawArrays(GL_LINES, 0, 6);
		}
		
		gl.glProgramUniform1i(rendering_program, offset_color, 0);

		/////////////
		// First element = sun
		pushMatrix();
		mvStack.scale(2, 2, 2);
		pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/10.0,0.0,1.0,0.0);
		gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, sunTexture);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		
		gl.glEnable(GL_DEPTH_TEST);
		int numVerts = mySphere.getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		popMatrix(); popMatrix();
		
		// ==== Second element = Skybox
		pushMatrix();
		mvStack.scale(100,100,100);
		gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		gl.glVertexAttribPointer(1,2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, skyboxTexture);
		

		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		popMatrix();
		
		// ==== Third element = d8 revolving sun
		
		pushMatrix();
		mvStack.translate(Math.sin(amt/3)*10.0f, 0.0f, Math.cos(amt/3)*8.0f);
		pushMatrix();
		mvStack.rotate(22f,0, 0, 1);
		pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/10.0, 0.0,1.0,0.0);
		pushMatrix();
		mvStack.scale(.5, .75, .5);
		
		gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(1,2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, d8Texture);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 24);
		popMatrix(); popMatrix(); popMatrix();
		
		// ==== Fourth Element = cube orbiting d8
		pushMatrix();
		mvStack.scale(.25, .25, .25);
		pushMatrix();
		mvStack.translate(0, Math.sin(amt/4)*8.0, Math.cos(amt/4)*8.0);
		pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/10.0, 0.0,0.0,1.0);
		
		gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1,2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, moonTexture);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		popMatrix();popMatrix();popMatrix();popMatrix();
		
		// ==== Fifth Element = sphere revolving sun
		pushMatrix();
		mvStack.rotate(-35, 1.0, 1.0, 0);
		pushMatrix();
		mvStack.translate(Math.sin(amt/10)*20.0f, 0.0f, Math.cos(amt/10)*30.0f);
		pushMatrix();
		mvStack.scale(.5, .5, .5);
		pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/8.0, 1, 0, 1);
		
		gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, secondPlanetTexture);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		popMatrix();
		
		// ==== Sixth Element = sphere revolving fifth element
		pushMatrix();
		mvStack.rotate(90, 0, 0, 1);
		pushMatrix();
		mvStack.translate(Math.cos(amt/4)*3.0f, 0f, Math.sin(amt/4)*3.0f);
		pushMatrix();
		mvStack.scale(.25, .25, .25);
		pushMatrix();
		mvStack.rotate((System.currentTimeMillis())/10.0, 0, 1, 0);
		
		gl.glUniformMatrix4fv(mv_loc, 1,  false, mvStack.peek().getFloatValues(), 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, checkTexture);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
		popMatrix();
		 
		// Pop all matrices
		while(ms > 0) {
			popMatrix();
		}
		
	}
	
	private void pushMatrix() {
		mvStack.pushMatrix();
		ms++;
	}
	private void popMatrix() {
		mvStack.popMatrix();
		ms--;
	}
	
	@Override
	public void init(GLAutoDrawable arg0) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		System.out.println("OpenGL Ver: "+gl.glGetString(GL_VERSION)); // Print version of OpenGL
		camera.setCameraPos(20, 20, 20);
		camera.rotate(-35, U);
		camera.rotate(45, V);
		setupVertices();
		
		
		joglSunTexture = loadTexture(path + "a2/sun.jpg");
		sunTexture = joglSunTexture.getTextureObject();
		
		joglSkyboxTexture = loadTexture(path + "a2/8k_stars.jpg");
		skyboxTexture = joglSkyboxTexture.getTextureObject();
		
		jogld8Texture = loadTexture(path + "a2/d8.jpg");
		d8Texture = jogld8Texture.getTextureObject();
		
		joglMoonTexture = loadTexture(path + "a2/8k_moon.jpg");
		moonTexture = joglMoonTexture.getTextureObject();
		
		joglSecondPlanetTexture = loadTexture(path + "a2/8k_mars.jpg");
		secondPlanetTexture = joglSecondPlanetTexture.getTextureObject();
		
		joglCheckTexture = loadTexture(path + "a2/checkered.jpg");
		checkTexture = joglCheckTexture.getTextureObject();
	}
	
	private void setupVertices() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		////////////////
		// Sun Sphere
		Vertex3D[] vertices = mySphere.getVertices();
		int[] indices = mySphere.getIndices();
		
		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++) {
			pvalues[i*3] = (float) (vertices[indices[i]]).getX();
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(11, vbo, 0);	// Change the first arg for each element added to scene
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
		
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
		
		//////////////
		// Planet #1 Moon
		Cube cube = new Cube(); // creates outward facing cube

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer moonBuf = Buffers.newDirectFloatBuffer(cube.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, moonBuf.limit()*4, moonBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer moonTextureBuf = Buffers.newDirectFloatBuffer(cube.getTexVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, moonTextureBuf.limit()*4, moonTextureBuf, GL_STATIC_DRAW);
		
		/////////////
		// Axes
		
		Axes axes = new Axes();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer axesBuf = Buffers.newDirectFloatBuffer(axes.getVerts());
		gl.glBufferData(GL_ARRAY_BUFFER, axesBuf.limit()*4, axesBuf, GL_STATIC_DRAW);
	}
	
	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		// Read shaders from files
		String currentDir = System.getProperty("user.dir");
		if(!currentDir.contains("src"))
			path = "src/";
			
		String vshaderSource[] = util.readShaderSource(path + "a2/vert.shader");
		String fshaderSource[] = util.readShaderSource(path + "a2/frag.shader");

		// Create vertex shader
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glCompileShader(vShader);
		
		// Check for compilation errors
		checkOpenGLError();  // can use returned boolean if desired
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] == 1)
		{	System.out.println("vertex compilation success");
		} else
		{	System.out.println("vertex compilation failed");
			printShaderLog(vShader);
		}
		
		// Create fragment shader
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
		gl.glCompileShader(fShader);
		
		// Check for compilation errors
		checkOpenGLError();  // can use returned boolean if desired
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] == 1)
		{	System.out.println("fragment compilation success");
		} else
		{	System.out.println("fragment compilation failed");
			printShaderLog(fShader);
		}
		
		// create OpenGL program with attached shaders
		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		
		// Check for link errors
		checkOpenGLError();
		gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] == 1)
		{	System.out.println("linking succeeded");
		} else
		{	System.out.println("linking failed");
			printProgramLog(vfprogram);
		}
		
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		return vfprogram;
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
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

}
