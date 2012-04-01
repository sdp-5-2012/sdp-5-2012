package Planning;
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

	private final static int DO_NOTHING = 0x00;
	private final static int FORWARDS = 0x10;
	private final static int BACKWARDS=0x20;
	private final static int STOP = 0x30;
	private final static int KICK = 0x40;
	private final static int QUIT = 0x50;
	private final static int FORWARDS_TRAVEL=0x60;
	private final static int TRAVEL_BACKWARDS_SLIGHTLY=0x70;
	private final static int TRAVEL_ARC=0x80;
	private final static int ARC=0x90;
	private final static int ROTATE = 0xA0;
	private final static int SET_WHEEL_SPEED=0xB0;
	private final static int STEER =0xC0;
	private final static int EACH_WHEEL_SPEED = 0xD0;
	private final static int ROTATE_INTERUPTABLE = 0x0E;

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
//			System.out.println("RESPONSE: FINISHED ROTATION!");
			moving = false;
		}

		return response;

	}

	/**
	 * Commands the robot to move forward
	 */
	public void moveForward(int speed) {
		moving = true;
		byte[] command = {(byte)FORWARDS,0x00,(byte)(speed >> 8),(byte)speed};
		addCommand(command);
		//	System.out.println("move forward");
	}

	/**
	 * Commands the robot to move  backward
	 */
	public void moveBackward(int speed) {
		moving = true;
		byte[] command = {(byte)BACKWARDS,0x00,(byte)(speed >> 8),(byte)speed};
		addCommand(command);
		System.out.println("move backward");
	}

	/**
	 * Commands the robot to stop where it is
	 */
	public void stop() {
		moving = false;
		byte[] command = {(byte)STOP,0x00,0x00,0x00};
		addCommand(command);
	}

	/**
	 * Commands the robot to kick
	 */
	public void kick() {
		System.out.println("kick");
		byte[] command = {(byte)KICK,0x00,0x00,0x00};
		addCommand(command);
	}

	/**
	 * Commands the robot to travel a certain distance
	 */
	public void travel(int distance) {
		moving = true;
		byte[] command = {(byte)FORWARDS_TRAVEL,0x00,(byte)(distance >> 8),(byte)distance};
		addCommand(command);
		System.out.println("travel forwards" + distance);
	}

	/**
	 * Commands the robot to move backward slightly
	 */
	public void backwardsSlightly(){
		byte[] command = {(byte)TRAVEL_BACKWARDS_SLIGHTLY,0x00,0x00,0x00};
		addCommand(command);
		System.out.println("move backward a little bit");
	}

	/**
	 * Commands the robot to travel along an arc
	 */
	public void travelArcRobot(int radius, int distance) {
		moving = true;
			// add 4 top bits from radius to the opcode
		byte opcodeWithParam = (byte)(TRAVEL_ARC | (byte)((((byte)(radius >> 8) << 4) & 0xFF) >>> 4));
		byte[] command = {opcodeWithParam,(byte)radius,(byte)(distance >> 8),(byte)distance};
		addCommand(command);
//		System.out.println("travel along arc");
	}
	
	/**
	 * Commands the robot travel an arc
	 */
	public void arc(int radius, int angle) {
		moving = true;
		byte opcodeWithParam = (byte)(ARC | (byte)((((byte)(radius >> 8) << 4) & 0xFF) >>> 4));
		byte[] command = {opcodeWithParam,(byte)radius,(byte)(angle >> 8),(byte)angle};
		addCommand(command);
//		System.out.println("arc to angle");
	}

	/**
	 * Commands the robot to rotate a given angle
	 */
	public void rotateRobot(int angle) {
		moving = true;
		byte[] command = {(byte)ROTATE,0x00,(byte)(angle >> 8),(byte)angle};
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
	
	void rotateRobotInteruptible(int angle) {
		moving = true;
		byte[] command = {(byte)ROTATE,0x00,(byte)(angle >> 8),(byte)angle};
		addCommand(command);
//		System.out.println("rotateI");

		while(true) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (moving == false) {
//				System.out.println("IS MOVING (ROBOT) " + moving);
				break;
			}
		}
	}

	/**
	 * Set the travel speed of the robot
	 */
	public void set_wheel_speed(int speed){
		System.out.println("set wheel speed");
		byte[] command = {(byte)SET_WHEEL_SPEED,0x00,(byte)(speed >> 8),(byte)speed};
		addCommand(command);
	}

	public void steer(int turnRate, int angle){
		moving = true;
		byte opcodeWithParam = (byte)(STEER | (byte)((((byte)(turnRate >> 8) << 4) & 0xFF) >>> 4));
		byte[] command = {opcodeWithParam,(byte)turnRate,(byte)(angle >> 8),(byte)angle};
		addCommand(command);
		System.out.println("start steer");
		addCommand(command);
	}

	/**
	 * Set the motor speed of each wheel
	 */
	public void each_wheel_speed(int speedL, int speedR){
		moving = true;
		byte opcodeWithParam = (byte)(EACH_WHEEL_SPEED | (byte)((((byte)(speedL >> 8) << 4) & 0xFF) >>> 4));
		byte[] command = {opcodeWithParam,(byte)speedL,(byte)(speedR >> 8),(byte)speedR};
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
