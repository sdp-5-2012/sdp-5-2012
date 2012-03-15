package Communications;

import java.io.IOException;

public interface CommunicationInterface {
	public void sendToRobot(int command);
	public int recieveFromRobot();
	public void openConnection() throws IOException;
	public void closeConnection();
}