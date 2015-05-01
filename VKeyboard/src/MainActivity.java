
public class MainActivity {
	
	/************************* IMPORTANT SETTINGS ************************/
	
	public static final boolean HASMOUSE = true;
	public static final boolean HASLEFT = true;
	public static final boolean HASBLUETOOTH = false;  //Remember to disconnect BT
	public static final boolean DEBUG = true;

	/********************************************************************/

	
	public static void main(String[] args){
		KeyboardCreator keyboard = new KeyboardCreator();
		keyboard.create(ConnectionCreator.createConnection());
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		keyboard.destroy();
	}
}
