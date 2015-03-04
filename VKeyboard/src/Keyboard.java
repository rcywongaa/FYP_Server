import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.jogamp.opengl.math.VectorUtil;

/*
 */
public class Keyboard {
	public static final int THUMB = 0;
	public static final int INDEX = 1;
	public static final int MIDDLE = 2;
	public static final int RING = 3;
	public static final int LITTLE = 4;
	public static final int FINGERNO = 5;
	private float OFFSETSCALE = 0.4f; //x-offset scale between rows
	private float margin = 0; //Hand.FINGERWIDTH / Hand.SCALE;
	private float offset = 0;
	/*
	 * Positions relative to J key (0, 0)
	 * In centimeters
	 */
	private final char[] KEYSEQ = {
			'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P',
			'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L',
			'Z', 'X', 'C', 'V', 'B', 'N', 'M'
	};
	private final int KEYNO = KEYSEQ.length;
	private float[][] KEYPOS = new float[KEYNO][2];
	private final int JINDEX = 16;
	private final int UINDEX = 6;
	private final int MINDEX = 25;
	
	private HashMap<Character, float[]> keyboard = new HashMap<Character, float[]>();
	private boolean isInit = false;
	private KeyboardView view;
	
	/*
	 * NOTE: Input 2D coords of 6 fingers (including palm)
	 */
	public Keyboard(float[][] initPos){
		assert initPos[0].length == 2 && initPos.length == Hand.FINGERNO : "Keyboard() bad input!";
		if (!initMargin(initPos[Hand.INDEX], initPos[Hand.MIDDLE], initPos[Hand.RING], initPos[Hand.LITTLE])){
			System.err.println("initMargin() failed!");
			return;
		}
		
		offset = OFFSETSCALE * margin;
		for (int i = 0; i < KEYNO; i++){
			if (i < 10){ //First row
				KEYPOS[i][0] = (-offset - (UINDEX - i)) * 2 * margin;
				KEYPOS[i][1] = margin * 2;
			}
			else if (i < 19){ //Second row
				KEYPOS[i][0] = - (JINDEX - i) * 2 * margin;
				KEYPOS[i][1] = 0;
			}
			else {
				KEYPOS[i][0] = (offset - (MINDEX - i)) * 2 * margin;
				KEYPOS[i][1] = -margin * 2;
			}
			keyboard.put(KEYSEQ[i], KEYPOS[i]);
		}
		if (!initJPos(initPos[Hand.INDEX])){
			System.err.println("initJPos() failed!");
			return;
		}
		view = new KeyboardView(); //TODO: Only create view after initialization
		isInit = true;
	}
	
	//FIXME: Change when little finger included
	public boolean initMargin(float[] iPos, float[] mPos, float[] rPos, float[] lPos){
		assert iPos.length == 2 && mPos.length == 2 && 
				rPos.length == 2 && lPos.length == 2 : "initMargin() bad input!";
		assert isInit == false : "Re-Initialization";
		margin = (Math.abs(iPos[0] - mPos[0]) + Math.abs(mPos[0] - rPos[0])) / (2 * 2);
		return true;
	}
	
	//FIXME:
	public boolean initJPos(float[] jPos){
		assert jPos.length == 2  : "initJPos() bad input!";
		assert isInit == false : "Re-Initialization!";
		
		for (Character c : keyboard.keySet()){
			keyboard.put(c, VectorUtil.addVec2(new float[2], keyboard.get(c), jPos)); 
		}
		return true;
	}
	
	public char getKey(float[] pos){
		assert isInit == true : "Not initialized yet!";
		assert pos.length == 2 : "getKey() bad input!";
		for (Character c : keyboard.keySet()){
			float[] keyPos = keyboard.get(c);
			if (Math.abs(keyPos[0] - pos[0]) < margin && Math.abs(keyPos[1] - pos[1]) < margin ){
				//System.out.println(c);
				return c;
			}
		}
		//System.out.println('?');
		return '?';
	}
	
	public boolean isInit(){
		return isInit;
	}
	
	public KeyboardView getView(){
		return view;
	}
	
	
	/*
	 * NOTE: All units are in pixels here
	 */
	@SuppressWarnings("serial")
	class KeyboardView extends JPanel implements ActionListener{
		private final int REFRATE = 1; //ms
		private final int RADIUS = 10;
		private final int RATIO = 35; //cm to pixel ratio
		private int KEYWIDTH = Math.round(2 * margin * RATIO); //TODO: Update value when MARGIN changes
		private final int[][] KEYPOS = new int[KEYNO][2];

		private int width = 10 * KEYWIDTH;
		private int height = 3 * KEYWIDTH;
		private int[][] pos = new int[FINGERNO][2];
		private Timer timer;
		private ArrayList<Character> currKey = new ArrayList<Character>();

		
		public KeyboardView(){
			for (int i = 0; i < KEYNO; i++){
				if (i < 10){ //First row
					KEYPOS[i][0] = i * KEYWIDTH;
					KEYPOS[i][1] = 0;
				}
				else if (i < 19){ //Second row
					KEYPOS[i][0] = Math.round((i - 10 + offset) * KEYWIDTH);
					KEYPOS[i][1] = KEYWIDTH * 1;
				}
				else {
					KEYPOS[i][0] = Math.round((i - 19 + 2 * offset) * KEYWIDTH);
					KEYPOS[i][1] = KEYWIDTH * 2;
				}
			}
			setSize(new Dimension(width, height));
			setPreferredSize(new Dimension(width, height));
			setBackground(Color.BLACK);
	        timer = new Timer(REFRATE, this);
	        timer.start();
		}
		
		public void setPos(float[][] pos){
			assert pos.length <= FINGERNO : "setPos() bad input!";
			assert pos[0].length == 2 : "setPos() bad input!";
			
			currKey.clear();
			float[] delta = new float[2];
			float[] jOffset = keyboard.get('J');
			//VectorUtil.scaleVec2(jOffset, keyboard.get('J'), -1);
			for (int i = 0; i < pos.length; i++){
				VectorUtil.subVec2(delta, pos[i], jOffset);
				this.pos[i][0] = Math.round(delta[0] * RATIO) + KEYPOS[JINDEX][0] + KEYWIDTH / 2; //KEYPOS is upper left corner
				this.pos[i][1] = -Math.round(delta[1] * RATIO) + KEYPOS[JINDEX][1] + KEYWIDTH / 2;
				currKey.add(getKey(pos[i]));
			}
		}
		
		public void actionPerformed(ActionEvent e){
			repaint();
		}		
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(new BasicStroke(3.0f));
			for (int i = 0; i < KEYNO; i++){
				char[] c = new char[1];
				c[0] = KEYSEQ[i];
				g2d.setColor(Color.WHITE);
				if (currKey.contains(KEYSEQ[i])){
					g2d.setColor(Color.DARK_GRAY);
					g2d.fillRect(KEYPOS[i][0], KEYPOS[i][1], KEYWIDTH, KEYWIDTH);
				}
				else g2d.drawRect(KEYPOS[i][0], KEYPOS[i][1], KEYWIDTH, KEYWIDTH);
				g2d.setColor(Color.WHITE);				
				g2d.drawChars(c, 0, 1, KEYPOS[i][0] + KEYWIDTH / 4, KEYPOS[i][1] + KEYWIDTH / 4);
			}
			g2d.setColor(Color.GRAY);
			for (int i = 0; i < pos.length; i++)
				g2d.fillOval(pos[i][0] - RADIUS, pos[i][1] - RADIUS, 2 * RADIUS, 2 * RADIUS);
		}
		
	}

}
