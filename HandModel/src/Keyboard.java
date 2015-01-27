import com.jogamp.opengl.math.VectorUtil;

public class Keyboard {
	private boolean isInit = false;
	private final float BORDER = Hand.FINGERWIDTH / Hand.SCALE;
	private final int YKEY = 
			0;
	private final int UKEY = 
			1;
	private final int HKEY = 
			2;
	private final int JKEY =
			3;
	private final int NKEY =
			4;
	private final int MKEY = 
			5;
	private final int KEYNO = 6;
	private char[] keyChar = new char[KEYNO];
	/*
	 * Positions relative to J key (0, 0)
	 * In centimeters
	 */
	private float[][] keyPos = new float[KEYNO][2];
	private final float[] JREL = {0, 0};
	private final float[] UREL = {-0.5f, BORDER};
	private final float[] MREL = {0.5f, -BORDER};
	private final float[] HREL = {-BORDER, 0};
	private final float[] YREL = {UREL[0] - BORDER, UREL[1]};
	private final float[] NREL = {MREL[0] - BORDER, MREL[1]};
	
	public Keyboard(){
		System.arraycopy(YREL, 0, keyPos[YKEY], 0, keyPos[YKEY].length);
		keyChar[YKEY] = 'Y';
		System.arraycopy(UREL, 0, keyPos[UKEY], 0, keyPos[UKEY].length);
		keyChar[UKEY] = 'U';
		System.arraycopy(HREL, 0, keyPos[HKEY], 0, keyPos[HKEY].length);
		keyChar[HKEY] = 'H';
		System.arraycopy(JREL, 0, keyPos[JKEY], 0, keyPos[JKEY].length);
		keyChar[JKEY] = 'J';
		System.arraycopy(NREL, 0, keyPos[NKEY], 0, keyPos[NKEY].length);
		keyChar[NKEY] = 'N';
		System.arraycopy(MREL, 0, keyPos[MKEY], 0, keyPos[MKEY].length);
		keyChar[MKEY] = 'M';
	}
	
	public void initJPos(float[] jPos){
		assert jPos.length == 2  : "initJPos() bad input!";
		assert isInit == false : "Re-Initiation!";
		for (int i = 0; i < KEYNO; i++){
			VectorUtil.addVec2(keyPos[i], keyPos[i], jPos);
		}
		isInit = true;
	}
	
	public char getKey(float[] pos){
		assert isInit == true : "Not initialized yet!";
		assert pos.length == 2 : "getKey() bad input!";
		for (int i = 0; i < KEYNO; i++){
			if (Math.abs(keyPos[i][0] - pos[0]) < BORDER && Math.abs(keyPos[i][1] - pos[1]) < BORDER ){
				System.out.println(keyChar[i]);
				return keyChar[i];
			}
		}
		//System.out.println('?');
		return '?';
	}
	
	public boolean isInit(){
		return isInit;
	}
}