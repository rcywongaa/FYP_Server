import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
 
public class MainActivity 
{
	static SerialReader reader;
	static Renderer renderer;
	static Keyboard keyboard;
	final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public static void main(String[] args) 
    {
    	reader = new SerialReader();
    	renderer = new Renderer();
    	keyboard = new Keyboard();
    	 
        JFrame frame = new JFrame( "Hello World" );
        frame.getContentPane().add( renderer.getCanvas());
 
        // shutdown the program on windows close event
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
            	reader.close();
                System.exit(0);
            }
        });
 
        frame.setSize( frame.getContentPane().getPreferredSize() );
        frame.setVisible( true );
        
        reader.initialize();
        
        scheduleReader();
        
    }
    
    public static void scheduleReader() {
    	final Runnable readRunner = new Runnable(){
    		public void run(){
    			if (reader.isReady()){
    				float[] data = reader.read();
    				//System.out.println("Data: " + data[6] + " " + data[7] + " " + data[8]);
    				if (renderer.getHand().isInit() == false){
    					renderer.getHand().initRadAngles(Hand.Side.RIGHT, data);
    				}
    				else {
    					renderer.getHand().setRadAngles(data);
    					if (keyboard.isInit() == false){
    						keyboard.initJPos(renderer.getHand().getXYCoord(Hand.INDEX));
    					}
    					else {
    						keyboard.getKey(renderer.getHand().getXYCoord(Hand.INDEX));
    					}
    				}
    			}
    			//System.out.println("Periodic Event");
    		}
    	};
    	scheduler.scheduleAtFixedRate(readRunner, 1000, 1, TimeUnit.MILLISECONDS);
    }
}