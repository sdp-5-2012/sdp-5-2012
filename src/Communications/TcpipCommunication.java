package Communications;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import java.net.*;

public class TcpipCommunication implements CommunicationInterface {

	private OutputStream os;
    private InputStream is;
    DatagramSocket clientSocket = new DatagramSocket();
	InetAddress IPAddress = InetAddress.getByName("localhost"); 

	public int recieveFromRobot() {
		try {
            int rec = is.read();
            return rec;
        } catch (IOException ex) {
            System.out.println("Error receiving from robot");
            System.out.println(ex.toString());
            return -1;
        }
	}

	public void sendToRobot(byte[] command) {
	    try {
	        os.write(command);
	        os.flush();
	    } catch (IOException ex) {
	        System.out.println("Error sending command to robot");
	        System.out.println(ex.toString());
	    }
	}
	
	public void openConnection() throws IOException{
		
        try {

		} catch (NXTCommException e) {
			throw new IOException("Failed to connect " + e.toString());
		}

        os = nxtComm.getOutputStream();
        is = nxtComm.getInputStream();
	}
	
	public void closeConnection()
	{
		try {
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
