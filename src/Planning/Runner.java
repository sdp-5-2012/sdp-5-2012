package Planning;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import lejos.nxt.Motor;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import Planning.Move;
import JavaVision.*;

public class Runner extends Thread {

	// Objects
	public static Ball ball;
	public static Robot nxt;
	static WorldState state;
	static Runner instance = null;
	static Move move;
	static Robot blueRobot;
	static Robot yellowRobot;
	boolean usingSimulator = false;
	private static ControlGUI thresholdsGUI;
	Vision vision;
//test
	// game flags
	boolean teamYellow = true;
	public static final int DEFAULT_SPEED = 35;		// used for move_forward method in Robot
	public static final int EACH_WHEEL_SPEED = 900; // used for each_wheel_speed method in Robot

	public static void main(String args[]) {
		instance = new Runner();

	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public Runner() {
		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();
		move = new Move();

		start();
	}

	/**
	 * Planning thread which begins planning loop
	 */
	public void run() {		
		if (teamYellow) {
			nxt = yellowRobot;
		} else {
			nxt = blueRobot;
		}

		startVision();

		// start communications with our robot
	//	nxt.startCommunications();
	//	mainLoop();

	}

	/**
	 * Method to initiate the vision
	 */
	private void startVision() {	    
		/**
		 * Creates the control
		 * GUI, and initialises the image processing.
		 * 
		 * @param args        Program arguments. Not used.
		 */
		WorldState worldState = new WorldState();
		ThresholdsState thresholdsState = new ThresholdsState();

		/* Default to main pitch. */
		PitchConstants pitchConstants = new PitchConstants(0);

		/* Default values for the main vision window. */
		String videoDevice = "/dev/video0";
		int width = 640;
		int height = 480;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 80;

		try {
			/* Create a new Vision object to serve the main vision window. */
			vision = new Vision(videoDevice, width, height, channel, videoStandard,
					compressionQuality, worldState, thresholdsState, pitchConstants);

			/* Create the Control GUI for threshold setting/etc. */
			thresholdsGUI = new ControlGUI(thresholdsState, worldState, pitchConstants);
			thresholdsGUI.initGUI();

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void mainLoop() {
		int angle = 0;
		int[] prevResults = new int[10];
		ArrayList<Integer> mode = new ArrayList<Integer>();

		for(int i = 1; i <= 10; i++) {
			getPitchInfo();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int m = move.getAngleToBall(nxt, ball);
			//			System.out.println("m: " + m);
			if (i < 11) {
				prevResults[i-1] = m;
				//				System.out.println("VALUE IN PREVRESULT: " + prevResults[i-1]);
			}
		}

		mode = findMode(prevResults);

		for (int j = 0; j < mode.size(); j++) {
			angle += mode.get(j);
		}

		angle /= mode.size();

		int dist = move.getDist(nxt, ball);
		System.out.println("First angle(avg) calculated: " + (angle));

		nxt.rotateRobot(angle);
		nxt.moveForward(25);

		while(dist > 100) { // dist in pixels
			Date d = new Date();
			long time = d.getTime();


			//			try {
			//				Thread.sleep(1000);
			//			} catch (InterruptedException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
			if  ( d.getTime() - time > 500) {
				getPitchInfo();
				dist = move.getDist(nxt, ball);
				int n = move.getAngleToBall(nxt, ball);
				if((n > 10) || (n<-10)) {
					System.out.println("MAINLOOP: WITHIN IF STATEMENT: ANGLE TO BALL: " + n);
					nxt.rotateRobot(n);
					nxt.moveForward(25);
				}
			}
		}

		nxt.stop();	
	}


	private ArrayList<Integer> findMode(int[] nums) {

		ArrayList<Integer> array1 = new ArrayList<Integer>();
		ArrayList<Integer> array2 = new ArrayList<Integer>();


		array1.add(nums[0]);

		for (int i = 1; i < nums.length; i++) {
			if (array1.get(0) - nums[i] <= 40) {
				array1.add(nums[i]);

				//			System.out.println("array1: " + nums[i]);
			} else {
				array2.add(nums[i]);
				//				System.out.println("array2: " + nums[i]);
			}
		}


		if (array2.size() > array1.size()) {
			return array2;
		} else {
			return array1;
		}

	}

	/**
	 * Get the most recent information from vision
	 */
	public void getPitchInfo() {

		// Get pitch information from vision
		state = vision.getWorldState();
		System.out.println("______________new pitch info_______________________");

		ball.setCoors(new Position(state.getBallX(), state.getBallY()));	
		//		System.out.println(ball.coors.getX()+ " " + ball.coors.getY());

		//		blueRobot.setAngle(state.getBlueOrientation());
		//		blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));

		//
		//		yellowRobot.setAngle(state.getYellowOrientation());
		//		yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));

		if(teamYellow) {
			nxt.setAngle(state.getYellowOrientation());
			nxt.setCoors(new Position(state.getYellowX(), state.getYellowY()));
			System.out.println("Y: " + Math.toDegrees(yellowRobot.angle));

			blueRobot.setAngle(state.getBlueOrientation());
			blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
			//			System.out.println(blueRobot.coors.getX() + " " + blueRobot.coors.getY() +" "+ blueRobot.angle);
		} else {
			nxt.setAngle(state.getBlueOrientation());
			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));
			//			System.out.println("B: " + yellowRobot.coors.getX() + " " + yellowRobot.coors.getY() +" "+ yellowRobot.angle);

			//			System.out.println(blueRobot.coors.getX() + " " + blueRobot.coors.getY() +" "+ blueRobot.angle);
			yellowRobot.setAngle(state.getYellowOrientation());
			yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
		}

	}
}