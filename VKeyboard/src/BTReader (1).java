import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BTReader {
	StreamConnection connection;
	private BufferedReader input;
	private OutputStream output;
	public void initialize(){
		System.out.println("Initializing BT...");
    	UUID uuid = new UUID("1101", true);
    	String connectionString = "btspp://localhost:" + uuid +";name=Sample SPP Server";
    	StreamConnectionNotifier streamConnNotifier;
		try {
			System.out.println("Opening connection...");
			streamConnNotifier = (StreamConnectionNotifier)Connector.open( connectionString );
			connection=streamConnNotifier.acceptAndOpen();
		} catch (IOException e) {
			System.out.println("Connector.open() OR streamConnNotifier.acceptAndOpen() failed!");
			e.printStackTrace();
		}
		try {
			System.out.println("Opening input stream...");
			input = new BufferedReader(new InputStreamReader(connection.openInputStream()));
		} catch (IOException e) {
			System.out.println("openInputStream() failed!");
			e.printStackTrace();
		}
		try {
			output = connection.openOutputStream();
		} catch (IOException e) {
			System.out.println("openOutputStream() failed!");
			e.printStackTrace();
		}
	}
	
}
