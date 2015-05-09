
public class ConnectionCreator {
	/*
	 * Serial Port: COM6, COM14
	 * USB Port: COM11 (unusable)
	 */
	private static final String R_PORT = "COM6";
	private static final String L_PORT = "COM15";
	private static final String R_BT = "RIGHTHAND";
	private static final String L_BT = "LEFTHAND";
	private static final String R_BTURL = "btspp://301408290406:1;authenticate=false;encrypt=false;master=false";
	private static final String L_BTURL = "btspp://301408290404:1;authenticate=false;encrypt=false;master=false";
	//RIGHTHAND btspp://301408290406:1;authenticate=false;encrypt=false;master=false
	//LEFTHAND btspp://301408290404:1;authenticate=false;encrypt=false;master=false
	private static CommReader reader[] = null;
	private static boolean ready = false;
	
	public synchronized static void createConnection(){
		if (reader != null){
			System.err.println("Connection already created!");
			return;
		}
    	if (MainActivity.HASLEFT){
    		if (MainActivity.HASBLUETOOTH){
    			reader = new BTReader[2];
    			reader[KeyboardCreator.RIGHT] = new BTReader(R_BT, R_BTURL);
    			reader[KeyboardCreator.LEFT] = new BTReader(L_BT, L_BTURL);
    		} else {
    			reader = new SerialReader[2];
    			reader[KeyboardCreator.RIGHT] = new SerialReader(R_PORT);
    			reader[KeyboardCreator.LEFT] = new SerialReader(L_PORT);
    		}
       		reader[KeyboardCreator.RIGHT].initialize();
   			reader[KeyboardCreator.LEFT].initialize();
    	} else {
    		reader = (MainActivity.HASBLUETOOTH ? new BTReader[1] : new SerialReader[1]);
    		reader[KeyboardCreator.RIGHT] = (MainActivity.HASBLUETOOTH ? new BTReader(R_BT, R_BTURL) : new SerialReader(R_PORT));
    		reader[KeyboardCreator.RIGHT].initialize();
    	}
    	ready = true;
	}
	
	public synchronized static CommReader[] getReader(){
		if (reader != null)
			return reader;
		else {
			System.err.println("No Connection!");
			return null;
		}
	}
	
	public synchronized static void closeConnection(){
    	for (int i = 0; reader != null && i < reader.length; i++)
    		reader[i].close();
	}
	
	public synchronized static boolean isReady(){
		return ready;
	}
	
}
