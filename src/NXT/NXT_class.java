package NXT;

import java.io.InputStream;
import java.io.OutputStream;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.robotics.navigation.DifferentialPilot;

/**
 * Code that runs on the NXT brick
 */
public class NXT_class implements Runnable{

	// class variables

	private static InputStream is;
	private static OutputStream os;
	private static DifferentialPilot pilot;
	private static volatile boolean blocking = false;
	private static volatile boolean kicking = false;

	// constants for the pilot class
	private static final float TRACK_WIDTH = (float) 12.0; // Secondary table
	private static final float WHEEL_DIAMETER = (float) 8.0;

	// NXT Opcodes
	private final static int DO_NOTHING = 0X00;
	private final static int FORWARDS = 0X01;
	private final static int BACKWARDS= 0x02;
	private final static int STOP = 0X03;
	private final static int KICK = 0X04;
	private final static int QUIT = 0X05;
	private final static int FORWARDS_TRAVEL = 0X06;
	private final static int TRAVEL_BACKWARDS_SLIGHTLY = 0X07;
	private final static int TRAVEL_ARC = 0X08;
	private final static int ARC = 0X09;
	private final static int ROTATE = 0X0A;
	private final static int SET_WHEEL_SPEED = 0X0B;
	private final static int STEER = 0X0C;
	private final static int EACH_WHEEL_SPEED = 0X0D;

	public static void main(String[] args) throws Exception {

		DifferentialPilot pilot = new DifferentialPilot(WHEEL_DIAMETER,
				TRACK_WIDTH, Motor.B, Motor.C, false);

		// start the sensor thread
		new Thread(new NXT_class(pilot)).start();

		// set initial pilot variables to produce maximum speed
		pilot.setTravelSpeed(pilot.getMaxTravelSpeed());

		while (true) {
			try {

				// wait for a connection and open streams
				pilot.stop();
				LCD.clear();
				LCD.drawString("Waiting...", 0, 2);
				LCD.drawString("Please connect", 0, 3);
				NXTConnection connection = Bluetooth.waitForConnection();
				is = connection.openInputStream();
				os = connection.openOutputStream();
				
				// Robot is ready when 'Connected!' appears on screen
				LCD.clear();
				LCD.drawString("Connected!", 0, 2);

				// begin reading commands
				int n = DO_NOTHING;

				while (n != QUIT) {

					// get the next command from the inputstream
					byte[] byteBuffer = new byte[4];
					is.read(byteBuffer);

					int opcode = byteBuffer[0];
					int param0 = byteBuffer[1];
					short param1 = bytesToShort(byteBuffer[2],byteBuffer[3]);

					if (blocking) {
						os.write('o');
						os.flush();
						continue;
					}
					if (opcode > 0)
						LCD.drawString("opcode = " + opcode, 0, 2);
					switch (opcode) {

					case FORWARDS:
						int speedForward = param0;
						LCD.clear();
						LCD.drawString("move forwards", 0, 2);
						LCD.refresh();
						pilot.setTravelSpeed(speedForward);
						pilot.forward();
						break;

					case FORWARDS_TRAVEL:
						int travelDistance= param1;
						LCD.clear();
						LCD.drawString("move forwards whith speed", 0, 2);
						LCD.refresh();
						pilot.travel(travelDistance, true);
						break;

					case TRAVEL_BACKWARDS_SLIGHTLY:	
						LCD.clear();
						LCD.drawString("travel back a little bit", 0, 2);
						LCD.refresh();
						pilot.travel(-10);
						break;

					case BACKWARDS:
						int speedBackward = param0;
						LCD.clear();
						LCD.drawString("move backwards", 0, 2);
						LCD.refresh();
						pilot.backward();
						pilot.setTravelSpeed(speedBackward);
						break;

					case ROTATE:	
						short rotateAngle = param1;

						LCD.clear();
						LCD.drawString("start rotate", 0, 2);
						LCD.drawString("angle = " + rotateAngle, 0, 3);

						pilot.setRotateSpeed(pilot.getRotateMaxSpeed()/5);
						pilot.rotate(rotateAngle, true);
						while(true) {
							if(pilot.isMoving() == false) {
								os.write('f');
								os.flush();
								break;
							}
						}
						break;

					case SET_WHEEL_SPEED:
						int speed = param0;
						pilot.setTravelSpeed(speed);
						break;

					case STEER:
						int turnRate = param0;
						int angle = param1;
						if(angle >360){
							angle = -(angle-360);
						}
						pilot.steer(turnRate, angle);
						break;

					case TRAVEL_ARC:
						int radius = param0;
						short distance = param1;
						pilot.travelArc(radius, distance, true);
						break;

					case ARC:
						int arcradius = param0;
						short arcangle = param1;
						pilot.arc(arcradius, arcangle, true);
						break;
					
					case EACH_WHEEL_SPEED:
						LCD.drawString("each wheel speed", 0, 2);
						int leftspeed = param0;
						int rightspeed = (int)param1;
						
						Motor.B.setSpeed(Math.abs(leftspeed));
						Motor.C.setSpeed(Math.abs(rightspeed));

						if (leftspeed >=0){
							Motor.B.forward();
						}else{
							Motor.B.backward();
						}

						if (rightspeed >=0){
							Motor.C.forward();
						}else{
							Motor.C.backward();
						}
						break;


					case STOP:
						LCD.clear();
						LCD.drawString("stop", 0, 2);
						LCD.refresh();
						pilot.stop();
						break;

					case KICK:
						LCD.clear();
						LCD.drawString("kick!!!", 0, 2);
						Thread Kick_thread = new Thread() {
							public void run() {
								Motor.A.setSpeed(900);
								Motor.A.rotate(60, true);
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
								Motor.A.rotate(-60, true);
								try {
									Thread.sleep(300);
								} catch (InterruptedException e) {
								}
								kicking = false;

							}
						};
						if (!kicking) {
							kicking = true;
							Kick_thread.start();
						}
						break;

					case QUIT: // close connection
						// Sound.twoBeeps();
						break;

					}

					// respond to say command was acted on
					os.write('o');
					os.flush();

				}

				// close streams and connection
				is.close();
				os.close();
				Thread.sleep(100); // wait for data to drain
				LCD.clear();
				LCD.drawString("closing", 0, 2);
				LCD.refresh();
				connection.close();
				LCD.clear();

			} catch (Exception e) {
				LCD.clear();
				LCD.drawString("EXCEPTION!", 0, 2);
			}
		}
	}

	/**
	 * Returns a short from two bytes
	 */
	public static short bytesToShort(byte a, byte b) {
		return (short)(((a & 0xFF) << 8) | (b & 0xFF));
	}

	/**
	 * The constructor accepts a reference to the pilot object, which is set in
	 * main(), so that pilot is accessible within the sensor thread.
	 */
	public NXT_class(DifferentialPilot pilot) {
		this.pilot = pilot;
	}

	/**
	 * Sensor thread: if a touch sensor is pushed then move back a little and
	 * inform the PC what has happened
	 */
	public void run() {

		boolean reacting = false;
		TouchSensor touchA = new TouchSensor(SensorPort.S1);
		TouchSensor touchB = new TouchSensor(SensorPort.S2);

		while (true) {
			try {

				if (!reacting && (touchA.isPressed() || touchB.isPressed())) {

					// flag sensor hit as being dealt with and save the speed
					// we were going before the collision occurred
					reacting = true;

					pilot.stop();
					// once touch sensor is pressed,the robot moves back a bit
					pilot.travel(-10);
					// let the PC know that the sensors were hit
					os.write('r');
					os.flush();

				} else if (reacting
						&& !(touchA.isPressed() || touchB.isPressed())) {
					reacting = false;
				}

				Thread.sleep(50);

			} catch (Exception ex) {
			}

		}
	}
}