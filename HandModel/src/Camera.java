import com.jogamp.opengl.math.VectorUtil;


public class Camera {
	public static final float CAMERADISTANCE = 15.0f; //Be careful of clipping planes
	private float[] position = {0.0f, 0.0f, CAMERADISTANCE};
	private float[] at = {0.0f, 0.0f, 0.0f};
	private float[] up = {0.0f, 1.0f, 0.0f};
	private float[] right = {1.0f, 0.0f, 0.0f};
	
	/*public float[] addPosition(float[] add){
		assert add.length == 3 : "positionAdd of size != 3";
		VectorUtil.addVec3(position, position, add);
		System.out.println("Camera Pos: " + position[0] + " " + position[1] + " " + position[2] + 
				"\nCamera Look: " + at[0] + " " + at[1] + " " + at[2]);
		return position;
	}
	
	public float[] addAt(float[] add){
		assert add.length == 3 : "atAdd of size != 3";
		VectorUtil.addVec3(at, at, add);
		return at;
	}
	
	public float[] addUp(float[] add){
		assert add.length == 3: "upAdd of size != 3";
		VectorUtil.addVec3(up, up, add);
		return at;
	}*/
	
	public void setPosition(float[] val){
		assert val.length == 3: "setPos of size != 3";
		//System.out.println("Previous Pos: " + position[0] + " " + position[1] + " " + position[2]);
		position[0] = val[0];
		position[1] = val[1];
		position[2] = val[2];
		fixOrient();
		//System.out.println("New Pos: " + position[0] + " " + position[1] + " " + position[2]);
	}

	public void setAt(float[] val){
		assert val.length == 3: "upAt of size != 3";
		at[0] = val[0];
		at[1] = val[1];
		at[2] = val[2];
		fixOrient();
	}
	
	public void setUp(float[] val){
		assert val.length == 3: "upSet of size != 3";
		up[0] = val[0];
		up[1] = val[1];
		up[2] = val[2];
	}

	public float[] getPosition(){
		return position;
	}
	
	public float[] getAt(){
		return at;
	}
	
	public float[] getUp(){
		return up;
	}
	
	public float[] getRight(){
		return right;
	}
	
	private void fixOrient(){
		float[] atDir = new float[3];
		float[] vertical = {0.0f, 1.0f, 0.0f};
		VectorUtil.subVec3(atDir, at, position); //atDir = at - position		
		VectorUtil.normalizeVec3(atDir);
		VectorUtil.crossVec3(right, atDir, vertical); //right = atDir x vertical
		
		//FIXME: Bug when atDir and vertical line up

		VectorUtil.normalizeVec3(right);
		VectorUtil.crossVec3(up, right, atDir); // up = right x atDir
		VectorUtil.normalizeVec3(up);
		//System.out.println("New Right: " + right[0] + " " + right[1] + " " + right[2]);
		//System.out.println("New Up: " + up[0] + " " + up[1] + " " + up[2]);
	}
	
	public float[] getLookAt(){
		
		return new float[]{position[0], position[1], position[2], 
				at[0], at[1], at[2], up[0], up[1], up[2]};
	}
}
