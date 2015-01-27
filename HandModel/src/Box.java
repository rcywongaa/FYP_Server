import javax.media.opengl.GL2;

/*
 * Note: Creates a unit box centered at x, y, z
 */
public class Box {
	public static void drawBox(GL2 gl, float x, float y, float z){
		//Draw front face
		gl.glColor3f(0, 0, 1);
		gl.glNormal3f(0, 0, 1);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(0.5f + x, 0.5f + y, 0.5f + z);
		gl.glVertex3f(-0.5f + x, 0.5f + y, 0.5f + z);
		gl.glVertex3f(-0.5f + x, -0.5f + y, 0.5f + z);
		gl.glVertex3f(0.5f + x, -0.5f + y, 0.5f + z);
		gl.glEnd();
		//Draw back face
		gl.glColor3f(0, 0, 0.5f);
		gl.glNormal3f(0, 0, -1);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(0.5f + x, 0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, 0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, -0.5f + y, -0.5f + z);
		gl.glVertex3f(0.5f + x, -0.5f + y, -0.5f + z);
		gl.glEnd();
		//Draw left face
		gl.glColor3f(0.5f, 0, 0);
		gl.glNormal3f(-1, 0, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(-0.5f + x, 0.5f + y, 0.5f + z);
		gl.glVertex3f(-0.5f + x, 0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, -0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, -0.5f + y, 0.5f + z);
		gl.glEnd();
		//Draw right face
		gl.glColor3f(1, 0, 0);
		gl.glNormal3f(1, 0, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(0.5f + x, 0.5f + y, 0.5f + z);
		gl.glVertex3f(0.5f + x, 0.5f + y, -0.5f + z);
		gl.glVertex3f(0.5f + x, -0.5f + y, -0.5f + z);
		gl.glVertex3f(0.5f + x, -0.5f + y, 0.5f + z);
		gl.glEnd();
		//Draw top face
		gl.glColor3f(0, 1, 0);
		gl.glNormal3f(0, 1, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(0.5f + x, 0.5f + y, 0.5f + z);
		gl.glVertex3f(0.5f + x, 0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, 0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, 0.5f + y, 0.5f + z);
		gl.glEnd();
		//Draw bottom face
		gl.glColor3f(0, 0.5f, 0);
		gl.glNormal3f(0, -1, 0);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(0.5f + x, -0.5f + y, 0.5f + z);
		gl.glVertex3f(0.5f + x, -0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, -0.5f + y, -0.5f + z);
		gl.glVertex3f(-0.5f + x, -0.5f + y, 0.5f + z);
		gl.glEnd();
	}
	
	//Creates a box centered at 0, 0, 0.5
	public static void drawDefaultBox(GL2 gl){
		drawBox(gl, 0, 0, 0.5f);
	}
}
