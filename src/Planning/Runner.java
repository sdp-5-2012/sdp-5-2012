package Planning;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import Planning.Move;
import GUI.MainGui;
import JavaVision.*;


public class Runner extends Thread { //implements ActionListener {

	// Objects
	public static Ball ball;
	public static Robot nxt;
	static Robot blueRobot;
	static Robot yellowRobot;	
	static WorldState state;
	static Runner instance = null;
	boolean usingSimulator = false;
	private static ControlGUI thresholdsGUI;
	Vision vision;
	PathPlanner planner = new PathPlanner();
	MainGui gui;

	Point ourNXT = new Point();
	Point otherNXT = new Point();
	Point ballPoint = new Point();

	// game flags
	boolean teamYellow = true;
	boolean attackLeft = false;
	int mode = 0;
	boolean applyClicked = false;

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

		start();
	}

	/**
	 * Planning thread which begins planning loop
	 */
	public void run() {		
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGui();
			}			
		});
		
		// Planning sleeps until Gui notifies
		synchronized (instance) {
			try {
				instance.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		getUserOptions();
	
		if (teamYellow) {
			nxt = yellowRobot;
		} else {
			nxt = blueRobot;
		}

		//	startVision();

		// start communications with our robot
		//	nxt.startCommunications();

		//	mainLoop();	
	}

	private void getUserOptions() {
		teamYellow = gui.getTeam();
		attackLeft = gui.getAttackLeft();
		mode = gui.getMode();		
	}

	private void createAndShowGui() {
		// Set the the control gui
		gui = new MainGui(instance);
		gui.setSize(600,400);
		gui.setLocation(250, 250);
		gui.setTitle("N.U.K.E Control Panel");
		gui.setResizable(true);
		gui.setVisible(true);
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

		Point goal = new Point();
		getPitchInfo();
		goal = planner.getOptimalPath(ourNXT, ballPoint, otherNXT, 0);

		Ball goalBall = new Ball();
		goalBall.setCoors(new Position(goal.x,goal.y));

		int angle = 0;
		int[] prevResults = new int[10];
		ArrayList<Integer> goodAngles = new ArrayList<Integer>();

		// Sum 10 angles from Vision
		for(int i = 1; i <= 10; i++) {
			getPitchInfo();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int m = Move.getAngleToBall(nxt, goalBall);

			if (i < 11) {
				prevResults[i-1] = m;
			}
		}
		// Remove outliers
		goodAngles = removeVal(prevResults);

		for (int j = 0; j < goodAngles.size(); j++) {
			angle += goodAngles.get(j);
		}

		// Get the average angle
		angle /= goodAngles.size();

		System.out.println("First angle(avg) calculated: " + (angle));

		// Initial robot rotation
		nxt.rotateRobot(angle);
		nxt.moveForward(25);

		int dist = Move.getDist(nxt, goalBall);

		while(dist > 35) { // dist in pixels
			System.out.println("Distance: " + dist);
			getPitchInfo();
			dist = Move.getDist(nxt, goalBall);
			int n = Move.getAngleToBall(nxt, goalBall);

			if((Math.abs(n) > 20)) {
				nxt.stop();
				nxt.rotateRobot(n);


				getPitchInfo();

				dist = Move.getDist(nxt, goalBall);

				nxt.moveForward(20);

			} else {
				getPitchInfo();
				dist = Move.getDist(nxt, goalBall);

			}
		}

		nxt.stop();


		//		int angle = 0;
		//		int[] prevResults = new int[10];
		//		ArrayList<Integer> goodAngles = new ArrayList<Integer>();
		//
		//		// Sum 10 angles from Vision
		//		for(int i = 1; i <= 10; i++) {
		//			getPitchInfo();
		//
		//			try {
		//				Thread.sleep(50);
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//
		//			int m = Move.getAngleToBall(nxt, ball);
		//
		//			if (i < 11) {
		//				prevResults[i-1] = m;
		//			}
		//		}
		//		// Remove outliers
		//		goodAngles = removeVal(prevResults);
		//
		//		for (int j = 0; j < goodAngles.size(); j++) {
		//			angle += goodAngles.get(j);
		//		}
		//
		//		// Get the average angle
		//		angle /= goodAngles.size();
		//
		//		System.out.println("First angle(avg) calculated: " + (angle));
		//
		//		int dist = Move.getDist(nxt, ball);
		//
		//		// Initial robot rotation
		//		nxt.rotateRobot(angle);
		//
		//		System.out.println("finished first rotation");
		//
		//		nxt.moveForward(20);
		//
		//		while(dist > 35) { // dist in pixels
		//			System.out.println("Distance: " + dist);
		//				getPitchInfo();
		//				dist = Move.getDist(nxt, ball);
		//				int n = Move.getAngleToBall(nxt, ball);
		//
		//				if((Math.abs(n) > 20)) {
		//					nxt.stop();
		//					nxt.rotateRobot(n);
		//
		//					getPitchInfo();
		//					dist = Move.getDist(nxt, ball);
		//
		//					nxt.moveForward(20);
		//
		//				} else {
		//					getPitchInfo();
		//					dist = Move.getDist(nxt, ball);
		//					
		//				}
		//		}
		//		
		//		getPitchInfo();
		//		nxt.stop();	
		//		nxt.rotateRobot(Move.getAngleToBall(nxt, ball));
		//		nxt.kick();


	}


	/*
	 * Compare the first element with all other elements.
	 * If difference > 40 then place in 2nd array
	 * Compare lengths of arrays, bigger array has more similar elements
	 * Repeat the comparison step on the bigger array
	 * to make sure that the array only contains similar elements
	 */

	static ArrayList<Integer> removeVal(int[] nums) {

		ArrayList<Integer> array1 = new ArrayList<Integer>();
		ArrayList<Integer> array2 = new ArrayList<Integer>();
		ArrayList<Integer> array3 = new ArrayList<Integer>(); array1.add(nums[0]);

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
		//		System.out.println("______________new pitch info_______________________");

		ball.setCoors(new Position(state.getBallX(), state.getBallY()));	
		ballPoint.x = ball.getCoors().getX();
		ballPoint.y = ball.getCoors().getY();

		if(teamYellow) {
			nxt.setAngle(state.getYellowOrientation());
			nxt.setCoors(new Position(state.getYellowX(), state.getYellowY()));

			ourNXT.x  = state.getYellowX();
			ourNXT.y = state.getYellowY();

			//			System.out.println("Y: " + Math.toDegrees(yellowRobot.angle));

			blueRobot.setAngle(state.getBlueOrientation());
			blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
			otherNXT.x = state.getBlueX();
			otherNXT.y = state.getBlueY();
			//			System.out.println(blueRobot.coors.getX() + " " + blueRobot.coors.getY() +" "+ blueRobot.angle);
		} else {
			nxt.setAngle(state.getBlueOrientation());
			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));
			ourNXT.x  = state.getBlueX();
			ourNXT.y = state.getBlueY();
			//			System.out.println("B: " + yellowRobot.coors.getX() + " " + yellowRobot.coors.getY() +" "+ yellowRobot.angle);

			//			System.out.println(blueRobot.coors.getX() + " " + blueRobot.coors.getY() +" "+ blueRobot.angle);
			yellowRobot.setAngle(state.getYellowOrientation());
			yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
			otherNXT.x = state.getYellowX();
			otherNXT.y = state.getYellowY();
		}

	}

//	@Override
//	public void actionPerformed(ActionEvent arg0) {
//		// Case for colour options
//		if(gui.options.yellowRobotButton.isSelected()) {
//			gui.log.setCurrentColour("Yellow");
//			System.out.println("Yellow");
//			teamYellow = true;
//		} else if(gui.options.blueRobotButton.isSelected()) {
//			gui.log.setCurrentColour("Blue");
//			teamYellow = false;
//		} 
//
//		// Case for attack options
//		if(gui.options.attackLeft.isSelected()) {
//			gui.log.setCurrentAttackGoal("Left");
//		} else if(gui.options.attackRight.isSelected()) {
//			gui.log.setCurrentAttackGoal("Right");
//		} 
//
//		// Case for mode options
//		if(gui.options.penaltyAttack.isSelected()) {
//			gui.log.setCurrentMode("Penalty Attack");
//		} else if(gui.options.penaltyDefend.isSelected()) {
//			gui.log.setCurrentMode("Penalty Defend");
//		} else if (gui.options.normal.isSelected()) {
//			gui.log.setCurrentMode("Normal");
//		}
//		System.out.println("Apply Clicked");
//		applyClicked = true;
//		
//	}
}
