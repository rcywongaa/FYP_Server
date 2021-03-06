import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Vector;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/*
 * NOTE:
 * Module acts as server!
 * Code taken from
 * http://www.jsr82.com/jsr-82-sample-spp-server-and-client/
 * 
 * Library: http://bluecove.org/bluecove/apidocs/overview-summary.html
 * 
 * It should technically be possible to access BT connection using virtual serial port... (undesirable)
 * Possible reasons of failure:
 * Insufficient permission (http://stackoverflow.com/questions/15450419/java-rxtx-code-to-connect-to-rfcomm0-is-not-working)
 * Incorrect port number (http://windows.microsoft.com/en-hk/windows/choose-com-port-bluetooth#1TC=windows-7)
 * BT stack (driver) problem
 * 
 */
public class BTReader extends CommReader implements DiscoveryListener{
	private static Object lock = new Object();
	DiscoveryAgent agent;
	StreamConnection streamConnection;
	//RIGHTHAND btspp://301408290406:1;authenticate=false;encrypt=false;master=false
	//LEFTHAND btspp://301408290404:1;authenticate=false;encrypt=false;master=false
	String connectionURL = null; 
	LocalDevice localDevice;
	RemoteDevice remoteDevice;
	private static Vector<RemoteDevice> vecDevices = new Vector<RemoteDevice>();
	
	private float[] data = new float[MainActivity.DATALENGTH]; //3 values per sensor
	private String btName;
	Thread updateThread;
	

	public BTReader(String name, String url) {
		try {
			localDevice = LocalDevice.getLocalDevice();
			System.out.println("Address: " + localDevice.getBluetoothAddress());
			System.out.println("Name: " + localDevice.getFriendlyName());
			agent = localDevice.getDiscoveryAgent();
			btName = name;
			connectionURL = url;
		} catch (BluetoothStateException e) {
			System.out.println("BTReader() failed!");
			e.printStackTrace();
		}
	}

	public void initialize() {
		System.out.println("Initializing BT...");
		
		if (connectionURL != null){
			Vector<RemoteDevice> cachedDevices = new Vector<RemoteDevice>();
			cachedDevices.addAll(Arrays.asList(agent.retrieveDevices(DiscoveryAgent.CACHED)));
			for (int i = 0; i < cachedDevices.size(); i++){
				RemoteDevice dev = cachedDevices.elementAt(i);
				try {
					if (dev.getFriendlyName(false).equals(btName)){
						remoteDevice = dev;
						break;
					}
				} catch (IOException e2) {
					System.err.println("Cannot get cached devices!");
					e2.printStackTrace();
				}
			}
			if (remoteDevice == null){
				System.err.println("Cannot find device - " + btName + " in cache!");
				connectionURL = null;
			}
		}
		if (connectionURL == null) {		
			try {
				agent.startInquiry(DiscoveryAgent.GIAC, this);
			} catch (BluetoothStateException e2) {
				System.err.println("startInquiry() failed!");
				e2.printStackTrace();
			}
	
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
			// print all devices in vecDevices
			int deviceCount = vecDevices.size();
	
			if (deviceCount <= 0) {
				System.out.println("No Devices Found .");
				System.exit(0);
			} else {
				for (int i = 0; i < deviceCount; i++) {
					RemoteDevice dev = vecDevices.elementAt(i);
					try {
						if (dev.getFriendlyName(false).equals(btName)){
							remoteDevice = dev;
							break;
						}
					} catch (IOException e) {
						System.err.println("RemoteDevice.getFriendlyName() error!");
						e.printStackTrace();
						continue;
					}
				}
				if (remoteDevice == null){
					System.err.println("Cannot find device - " + btName + "!");
					System.exit(0);
				}
			}
			
			UUID[] uuidSet = new UUID[1];
			uuidSet[0] = new UUID("1101", true);
			
			try {
				System.out.println("Searching for service...");
				agent.searchServices(null, uuidSet, remoteDevice, this);
			} catch (BluetoothStateException e1) {
				System.err.println("searchServices() failed!");
				e1.printStackTrace();
			}
			
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (connectionURL == null) {
				System.out.println("Device does not support Simple SPP Service.");
				System.exit(0);
			}
		}
		
		try {
			streamConnection = (StreamConnection) Connector.open(connectionURL);
			System.out.println("Connection URL: " + connectionURL);
			input = new BufferedReader(new InputStreamReader(streamConnection.openInputStream()));
			output = streamConnection.openOutputStream();
		} catch (IOException e1) {
			System.err.println("Connector.open() failed!");
			e1.printStackTrace();
		}

		updateThread = new Thread(new Runnable(){
			public void run(){
				while (true){
					try {
						while (!input.ready());
					} catch (IOException e1) {
						continue;
					}
					try {
						String in = input.readLine();
						String[] strData = in.split(",");
						for (int i = 0; i < strData.length; i++){
							try {
								data[i] = Float.valueOf(strData[i]);
								if (dataSemaphore.tryAcquire()){
									isReady = true;
									dataSet.update(data);
									dataSemaphore.release();
									if (updateSemaphore.availablePermits() < 1)
										updateSemaphore.release();
								}
							} catch (Exception e){
								isReady = false;
								System.err.println("Error parsing: " + in);
							}													
						}
					} catch (Exception e) {
						isReady = false;
						System.err.println("READ ERROR!");
					}
				}
			}
		});
		updateThread.start();
	}
	
	public void close(){
		try {
			streamConnection.close();
		} catch (IOException e) {
			System.err.println("Error closing connection!");
			e.printStackTrace();
		}
	}
			
	/********************** Methods of DiscoveryListener **********************/
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		try {
			System.out.println("Device Discovered: "
					+ btDevice.getFriendlyName(false));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!vecDevices.contains(btDevice)) {
			vecDevices.addElement(btDevice);
		}
	}

	// implement this method since services are not being discovered
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		System.out.println("Service Discovered: " + servRecord.length);
		if (servRecord != null && servRecord.length > 0) {
			connectionURL = servRecord[0].getConnectionURL(0, false);
		}
		synchronized (lock) {
			lock.notify();
		}
	}

	// implement this method since services are not being discovered
	public void serviceSearchCompleted(int transID, int respCode) {
		System.out.println("Service Search Complete!");
		synchronized (lock) {
			lock.notify();
		}
	}

	public void inquiryCompleted(int discType) {
		System.out.println("Inquiry Complete!");
		synchronized (lock) {
			lock.notify();
		}
	}
}
