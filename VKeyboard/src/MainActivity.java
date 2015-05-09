
/*
 * TODO: Only max tap energy counts as tap
 */

public class MainActivity {
	
	/************************* IMPORTANT SETTINGS ************************/
	
	public static final boolean HASMOUSE = true;
	public static final boolean HASLEFT = false;
	public static final boolean HASBLUETOOTH = false;  //Remember to disconnect BT
	public static final boolean DEBUG = true;
	
	/************************ GLOBAL VARIABLES **************************/
	
	public static final int SENSORCOUNT = 6;
	public final static int DATALENGTH = SENSORCOUNT * 4 - 1 + 2;  //SensorCount * (3 angles + 1 tap) - palm tap + 2 displacement
	public final static int KEYBOARDMODE = 1;
	public final static int MOUSEMODE = 2;

	/********************************************************************/
	
	public static void main(String[] args){
		KeyboardCreator keyboard = new KeyboardCreator();
		ConnectionCreator.createConnection();
		while (!ConnectionCreator.isReady());
		keyboard.create(ConnectionCreator.getReader());
//		for (int i = 0; i < 5; i++){
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			keyboard.destroy();
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
}
