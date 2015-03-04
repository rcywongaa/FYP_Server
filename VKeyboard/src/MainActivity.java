import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
 
/*
 * TODO: Fix initialization interfaces
 * TODO: Handle unlikely finger initiations
 * 
 * Procedures:
 * 1: Initialize forward position of all fingers
 * 2: Initialize J, K, L, ; positions (right hand)
 * 
 * NOTE: Size of raw data = (3 Axis * 3 Sensors * SENSORCOUNT * 32bit float) 
 * NOTE: Keyboard.FINGERNO (excludes palm) != Hand.FINGERNO (includes palm)
 */

public class MainActivity 
{
	public static final int INITTIME = 3000; //ms
	public static final int SENSORCOUNT = 5;
	public static final int READRATE = 1; //ms
	final static ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
	private static SerialReader reader = null;
	private static ReadRunnable readRunner = null;
	private static Renderer renderer = null;
	private static Keyboard keyboard = null;
	
    public static void main(String[] args) 
    {
    	reader = new SerialReader();
    	renderer = new Renderer();
    	readRunner = new ReadRunnable();
        reader.initialize();
        
        JFrame handFrame = new JFrame( "Hand Model" );
        handFrame.getContentPane().add( renderer.getCanvas());
        handFrame.setSize( handFrame.getContentPane().getPreferredSize() );
        handFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
            	reader.close();
                System.exit(0);
            }
        });
        handFrame.setVisible( true );
        
        System.out.println("Initializing forward yaw...");
        while (renderer.getHand().isInit() == false){
        	float[] data = getData();
			renderer.getHand().initRadAngles(Hand.Side.RIGHT, data);
			try {
				Thread.sleep(READRATE);
			} catch (InterruptedException e) {
				System.out.println("initRadAngles() sleep fail!");
				e.printStackTrace();
			}
        }
        
        System.out.println("Initializing 'J', 'K', 'L', ';' positions...");
        while (keyboard == null || keyboard.isInit() == false){
        	float[] data = getData();
        	renderer.getHand().setRadAngles(data);
        	keyboard = new Keyboard(renderer.getHand().getXYCoord());
        	try {
				Thread.sleep(READRATE);
			} catch (InterruptedException e) {
				System.out.println("Keyboard() sleep fail!");
				e.printStackTrace();
			}
        }
        System.out.println("Initialization complete!");

        SCHEDULER.scheduleAtFixedRate(readRunner, 1000, READRATE, TimeUnit.MILLISECONDS);

        JFrame keyboardFrame = new JFrame("Keyboard View");
        keyboardFrame.setUndecorated(true);
		keyboardFrame.getContentPane().add(keyboard.getView());
		keyboardFrame.pack();
        keyboardFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
        keyboardFrame.setResizable(false);
        keyboardFrame.setVisible(true);
    }
    
    private static void processData(float[] data){
    	if (renderer.getHand().isInit() == false){
			renderer.getHand().initRadAngles(Hand.Side.RIGHT, data);
		}
		else {
			renderer.getHand().setRadAngles(data);
			if (keyboard == null || keyboard.isInit() == false){
				keyboard = new Keyboard(renderer.getHand().getXYCoord());
			}
			else {
				float[][] fingerPos = new float[Keyboard.FINGERNO][2];
				for (int i = Keyboard.THUMB; i < Keyboard.FINGERNO; i++){
					fingerPos[i] = renderer.getHand().getXYCoord(i + 1); //0: Palm, 1: Thumb, ...
				}
				keyboard.getView().setPos(fingerPos);
			}
		}
    }
    
    public static float[] getData(){
    	float[] data = new float[reader.DATALENGTH];
    	while (!reader.isReady());
    	System.arraycopy(reader.read(), 0, data, 0, data.length);
    	return data;
    }
    
    private static class ReadRunnable implements Runnable {
    	public void run(){
			if (reader.isReady()){
				float[] data = new float[reader.DATALENGTH];
				System.arraycopy(reader.read(), 0, data, 0, data.length);
				renderer.getHand().setRadAngles(data);
				float[][] fingerPos = new float[Keyboard.FINGERNO][2];
				for (int i = Keyboard.THUMB; i < Keyboard.FINGERNO; i++){
					fingerPos[i] = renderer.getHand().getXYCoord(i + 1); //0: Palm, 1: Thumb, ...
				}
				keyboard.getView().setPos(fingerPos);
			}
			//else System.out.println("Redundant reads...");
		}
    }
}