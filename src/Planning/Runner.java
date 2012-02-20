package Planning;

import java.util.ArrayList;
import java.util.Date;
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

	// game flags
	boolean teamYellow = false;
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

		nxt.startCommunications();
		//nxt.rotateRobot(90);
		mainLoop();
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

		for(int i = 1; i <= 20; i++) {
			getPitchInfo();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int m = move.getAngleToBall(nxt, ball);
			System.out.println("m: " + m);

			if (i < 11) {
				prevResults[i-1] = m;
				System.out.println("VALUE IN PREVRESULT: " + prevResults[i-1]);
			}
		}

		mode = removeVal(prevResults);

		for (int j = 0; j < mode.size(); j++) {
			angle += mode.get(j);
		}

		angle /= mode.size();

		System.out.println("First angle(avg) calculated: " + (angle));

		int dist = move.getDist(nxt, ball);

		nxt.rotateRobot(angle);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nxt.moveForward(20);
		int counter = 0;

		while(dist > 30) { // dist in pixels

			//	System.out.println("COUNTER IS: " + counter);
			//			try {
			//				Thread.sleep(1000);
			//			} catch (InterruptedException e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
			if  (counter==15) {

				counter=0;

				getPitchInfo();
				dist = move.getDist(nxt, ball);
				int n = move.getAngleToBall(nxt, ball);

				System.out.println("THIS IS THE DISTANCE: " +dist);
				System.out.println("THIS IS THE ANGLE: " + n);

				if((Math.abs(n) > 20)) {
					System.out.println("MAINLOOP: WITHIN IF STATEMENT: ANGLE TO BALL: " + n + " " + dist);
					nxt.rotateRobot(n);
					getPitchInfo();
					dist = move.getDist(nxt, ball);
					int n2 = move.getAngleToBall(nxt, ball);
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					System.out.println("END OF IF ANGLE: " + n2);
					nxt.moveForward(20);

				}
			}
			counter++;
		}

		nxt.stop();	
	}


	/*
	 * Compare the first element with all other elements.
	 * If difference > 40 then place in 2nd array
	 * Compare lengths of arrays, bigger array has more similar elements
	 * Repeat the comparison step on the bigger array
	 * to make sure that the array only contains similar elements
	 */

	private ArrayList<Integer> removeVal(int[] nums) {

		ArrayList<Integer> array1 = new ArrayList<Integer>();
		ArrayList<Integer> array2 = new ArrayList<Integer>();
		ArrayList<Integer> array3 = new ArrayList<Integer>(); 

		array1.add(nums[0]);
		/*
		 * if i-th element in the array differs by more than 40 from the first
		 * element, add to 2nd array instead of 1st array
		 */

		for (int i = 1; i < nums.length; i++ ) {
			if ((nums[i]-nums[0] >= -20) && (nums[i]-nums[0] <= 20)) {
				array1.add(nums[i]);
			} else {
				array2.add(nums[i]);
			}
		}

		/*
		 * Check which is bigger, set bigger one to array1
		 */

		if (array2.size() > array1.size()) {
			array1 = (ArrayList<Integer>) array2.clone();
		}

		array2.clear();
		array2.add(array1.get(0));

		/*
		 * same as previous for loop, but compare elements in the newly made array
		 */

		for (int i = 1; i < array1.size(); i++) {
			if ((array1.get(i)-array1.get(0) >= -20) && (array1.get(i)-array1.get(0) <= 20)) {
				array2.add(array1.get(i));
			} else {
				array3.add(array1.get(i));
			}
		}

		/*
		 * return larger array
		 */

		if (array3.size() > array2.size()) {
			return array3;
		} else {
			return array2;
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
