import javax.media.opengl.GL2;

import com.jogamp.opengl.math.Matrix4;

/*
 * TODO: Fix thumb (get thumb position)
 * TODO: GUI for showing finger tip position on keyboard
 * TODO: Whole keyboard
 * TODO: Tap detection
 * 
 * NOTE: Roll is forced to 0
 * NOTE: Remember to apply rotations in order of ROLL, PITCH, YAW!
 * NOTE: From sensor, Yaw in clockwise +ve, Pitch upward +ve
 * NOTE: Matrix4.rotate() uses radians while glRotatef() uses degrees
 * NOTE: Final arrays are NOT immutable
 * NOTE: drawHand() and updateMat() are called at different rates
 */

public class Hand {
	private static final float THRESH = 0.2f; //Initialization threshold
	public static final float SCALE = 0.25f; //scales all dimensions
	public static final float YAWSCALE = 1.3f;
	private static final float[] ZERO3 = {0.0f, 0.0f, 0.0f};
	//Hard-coded angle offsets, probably a bad idea...
	private static final float[] ANGLEOFFSETS = {0.0f, 0.0f, 0.0f};
	
	//Right hand, in centimeters
	//P = Palm, T = Thumb, I = Index, M = Middle, R = Ring, L = Little
	private static final float[] THUMBWEIGHTS = {1.3f, -0.3f, -0.3f};
	private static final float JOINTLENGTH = 0.1f * SCALE;
	private static final float HANDHEIGHT = 2.0f * SCALE;
	public static final float FINGERWIDTH = 2.0f * SCALE;
	private static final float[] PBASE = {0.0f * SCALE, 0.0f * SCALE, 0.0f * SCALE};
	private static final float[] PLENGTH = {11.0f * SCALE, 0.0f, 0.0f};
	private static final float PWIDTH = 10.0f * SCALE;
	//Lengths: 0 = proximal, 1 = intermediate, 2 = distal
	//Bases: 0 = x, 1 = y, 2 = z
	private static final float[] TBASE = {PWIDTH / 2 - 1.0f * SCALE, 0.0f, 0.0f}; //Note that PALMWIDTH is already scaled
	private static final float[] TLENGTH = {6.0f * SCALE, 4.0f * SCALE, 4.0f * SCALE};
	private static final float[] IBASE = {4.0f * SCALE, 0.0f, PLENGTH[0] + JOINTLENGTH};
	private static final float[] ILENGTH = {5.0f * SCALE, 2.5f * SCALE, 3.0f * SCALE};
	private static final float[] MBASE = {1.25f * SCALE, 0.0f, PLENGTH[0] + JOINTLENGTH};
	private static final float[] MLENGTH = {5.0f * SCALE, 3.0f * SCALE, 3.0f * SCALE};
	private static final float[] RBASE = {-1.25f * SCALE, 0.0f, PLENGTH[0] + JOINTLENGTH};
	private static final float[] RLENGTH = {5.0f * SCALE, 2.5f * SCALE, 3.0f * SCALE};
	private static final float[] LBASE = {-4.0f * SCALE, 0.0f, PLENGTH[0] + JOINTLENGTH};
	private static final float[] LLENGTH = {3.0f * SCALE, 2.0f * SCALE, 3.0f * SCALE};
	
	public static final int ROLL = 0;
	public static final int PITCH = 1;
	public static final int YAW = 2;
	public static final int PALM = 0;
	public static final int THUMB = 1;
	public static final int INDEX = 2; 
	public static final int MIDDLE = 3;
	public static final int RING = 4;
	public static final int LITTLE = 5;
	public static final int FINGERNO = 6;
	private static final float[][] LENGTHS = {PLENGTH, TLENGTH, ILENGTH, MLENGTH, RLENGTH, LLENGTH};
	private static final float[][] BASES = {PBASE, TBASE, IBASE, MBASE, RBASE, LBASE};
	private static final float[] WIDTHS = {PWIDTH, FINGERWIDTH, FINGERWIDTH, FINGERWIDTH, FINGERWIDTH, FINGERWIDTH};
	
	Side side;
	private float[][] angles = new float[6][3];
	private float[][] initAngles = new float[6][3];
	private boolean isInit = false;
	//Note that creating an array of objects does NOT call the constructors
	private Matrix4[] mat = new Matrix4[FINGERNO]; //Keeps track of each finger's transform matrix
	
	static enum Side {
		LEFT, 
		RIGHT
	}

	public boolean isInit(){
		return isInit;
	}
	
	//Converts angles from radians to degrees
	//DO NOT CHANGE ORDER (follows sensor ordering: roll, pitch, yaw)
	public void setRadAngles(float[] angles){
		assert angles.length % 3 == 0 : "setRadAngles() bad input!";
		angles = alignAxis(angles);
		for (int i = 0; i < angles.length; i++){
			angles[i] = radToDeg(angles[i]) + ANGLEOFFSETS[i % 3];
			this.angles[i / 3][i % 3] = angles[i];
		}
		//System.out.println(angles[0] + " " + angles[1] + " " + angles[2]);
		fixYaw();
		updateMat();
	}
	
	public void initRadAngles(Side side, float[] angles){
		assert isInit == false : "Re-initiation";
		assert angles.length % 3 == 0 : "initRadAngles() bad input!";
		this.side = side;
		isInit = true;
		angles = alignAxis(angles);
		for (int i = 0; i < angles.length; i++){
			angles[i] = radToDeg(angles[i]) + ANGLEOFFSETS[i % 3];
			if (Math.abs(angles[i] - initAngles[i / 3][i % 3]) > THRESH){
				isInit = false;
			}
			initAngles[i / 3][i % 3] = angles[i];
		}	
	}
	
	/*
	 * Ensures that the resultant angles follow OpenGL coordinates
	 * Device (x, y, z) = Device (Away from pins, right, Into board)
	 * OpenGL (x, y, z) = (Right of screen, up, Out of screen)
	 * Device (x, y, z) = Screen (towards, left, down
	 */
	private float[] alignAxis(float[] angles){
		for (int i = 0; i < angles.length; i++){
			switch (i % 3){
			case 0:
				break;
			case 1:
				angles[i] = -angles[i];
				break;
			case 2:
				angles[i] = -angles[i];
				break;
			}
		}
		return angles;
	}
	
	/*
	 * Fix 0 Yaw to point towards +z-axis
	 * Amplifies yaw changes
	 */
	private void fixYaw(){
		for (int finger = 0; finger < FINGERNO; finger++){
			angles[finger][2] = YAWSCALE * (angles[finger][2] - initAngles[finger][2]);
		}
	}
	
	public void drawHand(GL2 gl)
	{
		gl.glPushMatrix();
				
		if (side == Side.RIGHT){
			//draw palm
			drawPalm(gl);

			gl.glPushMatrix();
			drawThumb(gl);
			gl.glPopMatrix();
			
			gl.glPushMatrix();
			drawFinger(gl, INDEX);
			gl.glPopMatrix();
			
			gl.glPushMatrix();
			drawFinger(gl, MIDDLE);
			gl.glPopMatrix();
			
			gl.glPushMatrix();
			drawFinger(gl, RING);
			gl.glPopMatrix();
			
			gl.glPushMatrix();
			//drawFinger(gl, LITTLE);
			gl.glPopMatrix();
		}
		
		gl.glPopMatrix();
	}
	
	//TODO:
	private void drawThumb(GL2 gl){
		//System.out.println(angles[2]);
		gl.glTranslatef(TBASE[0], TBASE[1], TBASE[2]);
		//Undo palm rotations
		gl.glRotatef(-angles[PALM][0], 0, 0, 1);
		gl.glRotatef(-angles[PALM][1], 1, 0, 0);
		gl.glRotatef(-angles[PALM][2], 0, 1, 0);
		
		gl.glRotatef(angles[THUMB][2], 0, 1, 0); //Angle weights should be applied to yaw for thumb
		gl.glRotatef(angles[THUMB][1], 1, 0, 0);
		gl.glRotatef(angles[THUMB][0], 0, 0, 1);
		gl.glPushMatrix();
		gl.glScalef(FINGERWIDTH, HANDHEIGHT, TLENGTH[0]);
		Box.drawDefaultBox(gl);
		gl.glPopMatrix();
		gl.glTranslatef(0, 0, TLENGTH[0] + JOINTLENGTH);
		gl.glPushMatrix();
		gl.glScalef(FINGERWIDTH, HANDHEIGHT, TLENGTH[1]);
		Box.drawDefaultBox(gl);
		gl.glPopMatrix();
		gl.glTranslatef(0, 0, TLENGTH[1] + JOINTLENGTH);
		gl.glPushMatrix();
		gl.glScalef(FINGERWIDTH, HANDHEIGHT, TLENGTH[2]);
		Box.drawDefaultBox(gl);
		gl.glPopMatrix();
	}
		
	/*
	 * NOTE: drawPalm applies roll while drawFinger does NOT
	 * NOTE: drawPalm does NOT apply ANGLEWEIGHTS
	 */
	private void drawPalm(GL2 gl){
		gl.glTranslatef(PBASE[0], PBASE[1], PBASE[2]);
		gl.glRotatef(angles[PALM][2], 0, 1, 0);
		gl.glRotatef(angles[PALM][1], 1, 0, 0);
		gl.glRotatef(angles[PALM][0], 0, 0, 1);
		gl.glPushMatrix();
		gl.glScalef(PWIDTH, HANDHEIGHT, PLENGTH[0]);
		Box.drawDefaultBox(gl);
		gl.glPopMatrix();
	}
	
	/*
	 *  NOTE: Matrix4.rotate uses radians!
	 */
	private void drawFinger(GL2 gl, int finger){
		//assert offset.length == 3 : "drawFinger() bad input!";
		assert finger < FINGERNO : "drawFinger() bad input!";
		assert finger != THUMB : "Use drawThumb() function!";
		assert finger != PALM : "Use drawPalm() function!";
		
		gl.glTranslatef(BASES[finger][0], BASES[finger][1], BASES[finger][2]);
		//Undo palm rotations
		gl.glRotatef(-angles[PALM][0], 0, 0, 1);
		gl.glRotatef(-angles[PALM][1], 1, 0, 0);
		gl.glRotatef(-angles[PALM][2], 0, 1, 0);
		
		gl.glRotatef(angles[finger][2], 0, 1, 0);
		gl.glRotatef(angles[finger][1] * getAngleWeight(finger, 0), 1, 0, 0);
		gl.glRotatef(angles[finger][0], 0, 0, 1); //Individual finger roll is unlikely
		gl.glPushMatrix();
		gl.glScalef(WIDTHS[finger], HANDHEIGHT, LENGTHS[finger][0]);
		Box.drawDefaultBox(gl);
		gl.glPopMatrix();
		
		if (LENGTHS[finger][1] == 0.0f) return;
		
		gl.glTranslatef(0, 0, LENGTHS[finger][0] + JOINTLENGTH);
		gl.glRotatef(angles[finger][1] * getAngleWeight(finger, 1), 1, 0, 0);
		gl.glPushMatrix();
		gl.glScalef(WIDTHS[finger], HANDHEIGHT, LENGTHS[finger][1]);
		Box.drawDefaultBox(gl);
		gl.glPopMatrix();
		
		if (LENGTHS[finger][2] == 0.0f) return;
		
		gl.glTranslatef(0, 0, LENGTHS[finger][1] + JOINTLENGTH);
		gl.glRotatef(angles[finger][1] * getAngleWeight(finger, 2), 1, 0, 0);
		gl.glPushMatrix();
		gl.glScalef(WIDTHS[finger], HANDHEIGHT, LENGTHS[finger][2]);
		Box.drawDefaultBox(gl);
		gl.glPopMatrix();
	}
	
	private synchronized void updateMat(){
		for (int i = 0; i < FINGERNO; i++){
			mat[i] = new Matrix4();
			mat[i].loadIdentity();
		}
		
		for (int finger = 0; finger < FINGERNO; finger++){
			if (finger == THUMB) continue; //Skip thumb here
			//drawPalm() transforms
			mat[finger].translate(PBASE[0], PBASE[1], PBASE[2]);
			mat[finger].rotate(degToRad(angles[PALM][2]), 0, 1, 0);
			mat[finger].rotate(degToRad(angles[PALM][1]), 1, 0, 0);
			mat[finger].rotate(degToRad(angles[PALM][0]), 0, 0, 1);
			
			//drawFinger() transforms
			mat[finger].translate(BASES[finger][0], BASES[finger][1], BASES[finger][2]);
			//Undo palm rotations
			mat[finger].rotate(degToRad(-angles[PALM][0]), 0, 0, 1);
			mat[finger].rotate(degToRad(-angles[PALM][1]), 1, 0, 0);
			mat[finger].rotate(degToRad(-angles[PALM][2]), 0, 1, 0);
			
			mat[finger].rotate(degToRad(angles[finger][2]), 0, 1, 0);
			mat[finger].rotate(degToRad(angles[finger][1] * getAngleWeight(finger, 0)), 1, 0, 0);
			mat[finger].rotate(degToRad(angles[finger][0]), 0, 0, 1); //Individual finger roll is unlikely
			
			mat[finger].translate(0, 0, LENGTHS[finger][0] + JOINTLENGTH);
			mat[finger].rotate(degToRad(angles[finger][1] * getAngleWeight(finger, 1)), 1, 0, 0);
			
			mat[finger].translate(0, 0, LENGTHS[finger][1] + JOINTLENGTH);
			mat[finger].rotate(degToRad(angles[finger][1] * getAngleWeight(finger, 2)), 1, 0, 0);
			mat[finger].scale(WIDTHS[finger], HANDHEIGHT, LENGTHS[finger][2]);
		}
		
	}
	
	private float getAngleWeight(int finger, int joint){
		//private static final float[] ANGLEWEIGHTS = {0.0f, 1.0f, 0.5f};
		//Try y = x^2
		assert finger < FINGERNO : "getAngleWeight() bad input!";
		assert joint < 3 : "getAngleWeight() bad input!";
		//TODO:
		switch (joint){
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			if (angles[finger][1] < 40.0f || angles[finger][1] > 180.0f)
				return 0.0f;
			else if (angles[finger][1] > 80.0f)
				return 0.8f;
			else
				//return (float) Math.pow((angles[finger][1] - 30.0f) / 40.0f, 2.0f);
				return ((angles[finger][1] - 40.0f) / 40.0f) * 0.8f;
				
		}
		System.err.println("getAngleWeight() error...");
		return 0;
	}
	
	/*
	 * Position relative to base of palm (0, 0, 0)
	 * Different from OpenGL coordinates
	 * gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX) does not work as it is affected by camera angle 
	 * NOTE: Returns the SCALED positions
	 */
	public synchronized float[] getPos(int finger){
		float[] pos = {0.0f, 0.0f, 1.0f, 1.0f}; //Matrix4.multVec() takes in a 4-component column-vector
		mat[finger].multVec(pos, pos);
		float[] result = new float[3];
		System.arraycopy(pos, 0, result, 0, result.length);
		//System.out.println(pos[0] + " " + pos[1] + " " + pos[2]);
		return result;
	}
	
	/*
	 * NOTE: Returns the UNSCALED x, y coordinates (right, up +ve)
	 */
	public float[] getXYCoord(int finger){
		float[] coord = new float[2];
		float[] pos = getPos(finger);
		coord[0] = -pos[0] / SCALE;
		coord[1] = pos[2] / SCALE;
		//System.out.println(coord[0] + " " + coord[1]);
		return coord;
	}
	
	public float radToDeg(float rad){
		return (float) (rad  * 180.0f / Math.PI);
	}
	
	public float degToRad(float deg){
		return (float) (deg * Math.PI / 180.0f);
	}
}
