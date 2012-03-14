package Planning;
import JavaVision.*;
import java.io.IOException;
import java.util.LinkedList;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;
import Communications.BluetoothCommunication;

public class Robot extends ObjectDetails {


	private boolean isConnected = false;
	private boolean keepConnected = true;
	public boolean askingToReset = false;

	public boolean moving = true;

	private final static int DO_NOTHING = 0X00;
	private final static int FORWARDS = 0X01;
	private final static int BACKWARDS=0x02;
	private final static int STOP = 0X03;
	private final static int KICK = 0X04;
	private final static int QUIT = 0X05;
	private final static int FORWARDS_TRAVEL=0X06;
	private final static int TRAVEL_BACKWARDS_SLIGHTLY=0X07;
	private final static int TRAVEL_ARC=0X08;
	private final static int ACCELERATE=0X09;

	private final static int ROTATE = 0X0A;
	private final static int SET_WHEEL_SPEED=0X0B;
	private final static int STEER =0X0C;
	private final static int EACH_WHEEL_SPEED = 0x0D;

	private LinkedList<byte[]> commandList = new LinkedList<byte[]>();
	private BluetoothCommunication comms;
	private lejos.pc.comm.NXTComm nxtComm;
	private NXTInfo info = new NXTInfo(NXTCommFactory.BLUETOOTH, "NXT",
	"00:16:53:08:DA:0F");	

	private void connectToRobot() throws IOException {
		comms = new BluetoothCommunication(nxtComm, info);
		comms.openConnection();
		setConnected(true);
	}

	public boolean startCommunications() {

		// start up the connection
		try {
			connectToRobot();
		}
		catch (IOException ex) {
			System.err.println("Robot Connection Failed: ");
			System.err.println(ex.toString());
			return false;
		}

		new Thread(new Runnable() {

			public void run() {

				// send data when necessary
				while (keepConnected) {
					if (commandList.isEmpty()) {
						//sendToRobot(DO_NOTHING);
					} else {
						//sendToRobot(commandList.remove);


						receiveFromRobot();

						//						 Tools.rest(10);
					}
					//					 disconnect when we're done
					//					if (isConnected) {
					//						System.out.println("Connection? "+isConnected());
					//						disconnectFromRobot();
					//						System.out.println("Connection? "+isConnected());
					//					}
				}
			}
		}).start();
		return true;
	}

	/**
	 * Disconnect from the NXT
	 */
	public void disconnectFromRobot() {
		try {
			comms.closeConnection();
			nxtComm.close();
			setConnected(false);
		} catch (Exception e) {
			System.err.println("Error Disconnecting from NXT");
			System.err.println(e.toString());
		}
	}

	/**
	 * Stops the connection with the Robot
	 */
	public void stopCommunications() {
		keepConnected = false;
	}

	/**
	 * Add a command to the queue to be sent to the robot
	 */
	public void addCommand(byte[] command) {

		while (commandList.size() > 3) {
			commandList.remove();
			System.out.println("<");
		}
		commandList.offer(command);
		sendToRobot(command);
	}

	/**
	 * Clear the queue of commands to be sent to the robot
	 */
	public void clearAllCommands() {
		commandList.clear();
	}

	/**
	 * Sends a command to the robot
	 */
	public void sendToRobot(byte[] command) {
		//		System.out.println("SENT "+command+" TO ROBOT");
		comms.sendToRobot(command);
	}

	/**
	 * Receive an integer from the robot
	 */
	public int receiveFromRobot() {

		int response = comms.recieveFromRobot();

		if (response == 'r') {
			askingToReset = true;
			//			 clearAllCommands();
			//			 System.out.println("STACK CLEARED");
		} else if (response == 'f') {
			// Robot has finished moving
			System.out.println("RESPONSE: FINISHED ROTATION!");
			moving = false;
		}

		return response;

	}

	/**
	 * Commands the robot to move forward
	 */
	public void moveForward(int speed) {
		moving = true;
		byte[] command = {FORWARDS,(byte)speed,0x00,0x00};
		addCommand(command);
		//	System.out.println("move forward");
	}

// 	public void accelerateRobot(int acceleration) {
// 		moving = true;
// 		int command = ACCELERATE | (acceleration <<16 );
// 		addCommand(command);
// 		System.out.println("accerelate");
// 	}


	/**
	 * Commands the robot to move  backward
	 */
	public void moveBackward(int speed) {
		moving = true;
		byte[] command = {BACKWARDS,(byte)speed,0x00,0x00};
		addCommand(command);
		System.out.println("move backward");
	}

	/**
	 * Commands the robot to move backward slightly
	 */
	public void backwardsSlightly(){
		byte[] command = {TRAVEL_BACKWARDS_SLIGHTLY,0x00,0x00,0x00};
		addCommand(command);
		System.out.println("move backward a little bit");
	}

	/**
	 * Commands the robot to rotate a given angle
	 */
	public void rotateRobot(int angle) {
		moving = true;
		short angle2 = (short)angle;
		byte[] command = {ROTATE,0x00,(byte)(angle2 >> 8),(byte)angle2};
		System.out.println(command[0] + ", " + command[1] + ", " + command[2] + ", " + command[3]);

		addCommand(command);
		System.out.println("rotate");

		while(true) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (moving == false) {
				System.out.println("IS MOVING (ROBOT) " + moving);
				break;
			}
		}
	}

	/**
	 * Commands the robot to travel along an arc
	 */
	public void travelArcRobot(int radius, int distance) {
		moving = true;
		byte[] command = {TRAVEL_ARC,(byte)radius,(byte)(distance >> 8),(byte)distance};
		addCommand(command);
		System.out.println("travel along arc");

	}

	public void set_wheel_speed(int speed){
		System.out.println("set wheel speed");
		byte[] command = {SET_WHEEL_SPEED,(byte)speed,0x00,0x00};
		addCommand(command);
	}



// 	public void steer(int turnRate, int angle){
// 		int command = STEER|(turnRate << 8)
// 		| (turnRate << 8);
// 		System.out.println("start steer");
// 		addCommand(command);
// 
// 	}

	public void each_wheel_speed(int speedL, int speedR){
		moving = true;
		byte[] command = {EACH_WHEEL_SPEED,(byte)speedL,(byte)(speedR >> 8),(byte)speedR};
		addCommand(command);
	}


	/**
	 * Commands the robot to stop where it is
	 */
	public void stop() {
		moving = false;
		byte[] command = {STOP,0x00,0x00,0x00};
		addCommand(command);
	}

	/**
	 * Commands the robot to kick
	 */
	public void kick() {
		System.out.println("kick");
		byte[] command = {KICK,0x00,0x00,0x00};
		addCommand(command);
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public boolean isMoving() {
		return moving;
	}

}
