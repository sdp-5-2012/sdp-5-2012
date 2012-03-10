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

public class Arc extends Thread { // implements ActionListener {

	// Objects

	public static Ball newBall;
	
	public static Ball ball;

	public static Robot nxt;

	static Robot blueRobot;

	static Robot yellowRobot;
	static Robot otherRobot;

	static WorldState state;

	static Arc instance = null;

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

	public static final int DEFAULT_SPEED = 35; // used for move_forward method
	// in Robot

	public static final int EACH_WHEEL_SPEED = 900; // used for each_wheel_speed
	// method in Robot

	public static void main(String args[]) {

		instance = new Arc();

	}

	/**
	 * 
	 * 
	 * 
	 * Instantiate objects and start the planning thread
	 */

	public Arc() {

		blueRobot = new Robot();

		yellowRobot = new Robot();
		
		otherRobot = new Robot();

		ball = new Ball();
		newBall = new Ball();

		start();

	}

	/**
	 * 
	 * Planning thread which begins planning loop
	 */

	public void run() {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {


			}

		});

		// Planning sleeps until Gui notifies

		synchronized (instance) {
		}

		if (teamYellow) {

			nxt = yellowRobot;
			otherRobot = blueRobot;

		} else {

			nxt = blueRobot;
			otherRobot = yellowRobot;
		}

		startVision();

		// start communications with our robot

		nxt.startCommunications();

		mainLoop();

	}

	
/**
	 * 
	 * 
	 * 
	 * Method to initiate the vision
	 */

	private void startVision() {

		/**
		 * 
		 * 
		 * 
		 * Creates the control
		 * 
		 * GUI, and initialises the image processing.
		 * 
		 * 
		 * 
		 * 
		 * 
		 * @param args
		 *            Program arguments. Not used.
		 */

		WorldState worldState = new WorldState();

		ThresholdsState thresholdsState = new ThresholdsState();

		/* Default to main pitch. */

		PitchConstants pitchConstants = new PitchConstants(1);

		/* Default values for the main vision window. */

		String videoDevice = "/dev/video0";

		int width = 640;

		int height = 480;

		int channel = 0;

		int videoStandard = V4L4JConstants.STANDARD_PAL;

		int compressionQuality = 80;

		try {

			/* Create a new Vision object to serve the main vision window. */

			vision = new Vision(videoDevice, width, height, channel,
					videoStandard,

					compressionQuality, worldState, thresholdsState,
					pitchConstants);

			/* Create the Control GUI for threshold setting/etc. */

			thresholdsGUI = new ControlGUI(thresholdsState, worldState,
					pitchConstants);

			thresholdsGUI.initGUI();

		} catch (V4L4JException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	private void mainLoop() {
		//nxt.moveForward(50);
		//ArrayList<Position> goals = new ArrayList<Position>();

		getPitchInfo();
		//System.out.println(ourNXT.x );

		//goals = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(), otherRobot.getCoors());
//		System.out.println("OUR NXT: " + ourNXT);
//		System.out.println("OTHER NXT: " + otherNXT);
//		System.out.println("BALL: " + ballPoint);
//		System.out.println(goals);
//		Position goal = new Position(0,0);
//		Ball goalBall = new Ball();
		int angle = 0;
		int[] prevResults = new int[10];
		float m;
		int dist;
		int radius;
		
		ArrayList<Integer> goodAngles = new ArrayList<Integer>();
		
		//for(int x = 1; x<goals.size();x++){
			
//			goal = goals.get(goals.size()-1);
			
			//System.out.println("CURRENTLY AT: "+ ourNXT.x + " , " + ourNXT.y);

//			goalBall.setCoors(new Position(goal.getX(), goal.getY()));
//			System.out.println("GOING OT: "+goalBall.getCoors().getX() + " , " + goalBall.getCoors().getY());

			// Sum 10 angles from Vision

			for (int i = 1; i <= 10; i++) {

				getPitchInfo();

				try {

					Thread.sleep(50);

				} catch (InterruptedException e) {

					e.printStackTrace();

				}

				m = nxt.angle;
				System.out.println("M!!!!!!! " + m);

				if (i < 11) {

					prevResults[i - 1] = (int) m;

				}

			}

			// Remove outliers

			goodAngles = removeVal(prevResults);

			for (int j = 0; j < goodAngles.size(); j++) {

				angle += goodAngles.get(j);

			}

			// Get the average angle and distance
			
			angle /= goodAngles.size();
			System.out.println("ANGLE FROM VISION: " + angle);

			dist = Move.getDist(nxt, ball);

			angle = Move.getAngleToBall(nxt, ball);
			
			radius = (int) (dist/(2*Math.sin(angle)));
			
			nxt.travelArcRobot(radius, dist);
			nxt.stop();
			// Initial robot rotation
//			System.out.println("ANGLE TO GOAL: " + angle);
//			System.out.println("DISTANCE TO GOAL " + dist);

//			nxt.rotateRobot(angle);
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			nxt.moveForward(20);
//			int counter = 0;
//			
//			while(dist > 30) { // dist in pixels
//				
//				if  (counter==10) {
//					
//					counter=0;
//					
//					getPitchInfo();
//					dist = Move.getDist(nxt, newBall);
//					int n = Move.getAngleToBall(nxt, newBall);
//					
//					System.out.println("THIS IS THE DISTANCE: " +dist);
//					System.out.println("THIS IS THE ANGLE: " + n);
//					
//					if((Math.abs(n) > 20)) {
//						System.out.println("MAINLOOP: WITHIN IF STATEMENT: ANGLE TO BALL: " + n + " " + dist);
//						nxt.rotateRobot(n);
//						getPitchInfo();
//						dist = Move.getDist(nxt, newBall);
//						int n2 = Move.getAngleToBall(nxt, newBall);
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//						System.out.println("END OF IF ANGLE: " + n2);
//						nxt.moveForward(20);
//						
//					}
//				}
//				counter++;
//			}
//			
//			nxt.stop();	
//			dist = Move.getDist(nxt, ball);
//			
//			angle = Move.getAngleToBall(nxt, ball);
//			System.out.println("Angle to the ball" + angle);
//			nxt.rotateRobot(angle);
//			nxt.moveForward(20);
//			try {
//				Thread.sleep(1500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			nxt.kick();
//			nxt.stop();
		}
	


	static ArrayList<Integer> removeVal(int[] nums) {

		ArrayList<Integer> array1 = new ArrayList<Integer>();

		ArrayList<Integer> array2 = new ArrayList<Integer>();

		ArrayList<Integer> array3 = new ArrayList<Integer>();
		array1.add(nums[0]);

		/*
		 * 
		 * if i-th element in the array differs by more than 40 from the first
		 * 
		 * 
		 * 
		 * element, add to 2nd array instead of 1st array
		 */

		for (int i = 1; i < nums.length; i++) {

			if ((nums[i] - nums[0] >= -20) && (nums[i] - nums[0] <= 20)) {

				array1.add(nums[i]);

			} else {

				array2.add(nums[i]);

			}

		}

		/*
		 * 
		 * 
		 * 
		 * Check which is bigger, set bigger one to array1
		 */

		if (array2.size() > array1.size()) {

			array1 = (ArrayList<Integer>) array2.clone();

		}

		array2.clear();

		array2.add(array1.get(0));

		/*
		 * 
		 * 
		 * 
		 * same as previous for loop, but compare elements in the newly made
		 * array
		 */

		for (int i = 1; i < array1.size(); i++) {

			if ((array1.get(i) - array1.get(0) >= -20)
					&& (array1.get(i) - array1.get(0) <= 20)) {

				array2.add(array1.get(i));

			} else {

				array3.add(array1.get(i));

			}

		}

		/*
		 * 
		 * return larger array
		 */

		if (array3.size() > array2.size()) {

			return array3;

		} else {

			return array2;

		}

	}

	/**
	 * 
	 * Get the most recent information from vision
	 */

	public void getPitchInfo() {

		// Get pitch information from vision
		int i=0;
		state = vision.getWorldState();
		while(state.getBlueX()==0){
			state = vision.getWorldState();
			i++;
			System.out.println(i);
		}
		//System.out.println(i);

		// System.out.println("______________new pitch info_______________________");

		ball.setCoors(new Position(state.getBallX(), state.getBallY()));
		System.out.println(ball.getCoors().getX() + " " + ball.getCoors().getY());
		
		if(ball.getCoors().getX()<280){
			if(ball.getCoors().getY() <215){
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()-50));
			} else if (ball.getCoors().getY() >265){
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()+50));
			}	else if (215<= ball.getCoors().getY() && ball.getCoors().getY() <= 265 ){
					newBall.setCoors(new Position(ball.getCoors().getX()+80, ball.getCoors().getY()-25));
				}
				else {
					newBall.setCoors(new Position(ball.getCoors().getX()+80, ball.getCoors().getY()+25));
				}
			} else if(ball.getCoors().getX()>280){
			if(ball.getCoors().getY() <215){
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()-50));
			} else if (ball.getCoors().getY() >265){
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()+50));
			} else if (215<= ball.getCoors().getY() && ball.getCoors().getY() <= 265 ){
				newBall.setCoors(new Position(ball.getCoors().getX()-80, ball.getCoors().getY()-25));
			}
			else {
				newBall.setCoors(new Position(ball.getCoors().getX()-80, ball.getCoors().getY()+25));
			}
		}
		
		
		System.out.println(newBall.getCoors().getX() + " " + newBall.getCoors().getY());
//		
//
//		ballPoint.x = ball.getCoors().getX();
//
//		ballPoint.y = ball.getCoors().getY();
		System.out.println("BALL!!!!"+ball.getCoors().getX() +" "+ ball.getCoors().getY());

		if (teamYellow) {

			nxt.setAngle(state.getYellowOrientation());

			nxt.setCoors(new Position(state.getYellowX(), state.getYellowY()));

//			ourNXT.x = state.getYellowX();
//			//System.out.println("YellowX" + state.getYellowX());
//
//			ourNXT.y = state.getYellowY();

			// System.out.println("Y: " + Math.toDegrees(yellowRobot.angle));

			otherRobot.setAngle(state.getBlueOrientation());

			otherRobot
			.setCoors(new Position(state.getBlueX(), state.getBlueY()));

			
//			otherNXT.x = state.getBlueX();
//
//			otherNXT.y = state.getBlueY();

			// System.out.println(blueRobot.coors.getX() + " " +
			// blueRobot.coors.getY() +" "+ blueRobot.angle);

		} else {

			nxt.setAngle(state.getBlueOrientation());

			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));
//
//			ourNXT.x = state.getBlueX();
//
//			ourNXT.y = state.getBlueY();

			// System.out.println("B: " + yellowRobot.coors.getX() + " " +
			// yellowRobot.coors.getY() +" "+ yellowRobot.angle);

			// System.out.println(blueRobot.coors.getX() + " " +
			// blueRobot.coors.getY() +" "+ blueRobot.angle);

			otherRobot.setAngle(state.getYellowOrientation());

			otherRobot.setCoors(new Position(state.getYellowX(), state
					.getYellowY()));
			//otherRobot.setCoors(new Position(300,300));

//			otherNXT.x = state.getYellowX();
//
//			otherNXT.y = state.getYellowY();

		}

	}

	// @Override

	// public void actionPerformed(ActionEvent arg0) {

	// // Case for colour options

	// if(gui.options.yellowRobotButton.isSelected()) {

	// gui.log.setCurrentColour("Yellow");

	// System.out.println("Yellow");

	// teamYellow = true;

	// } else if(gui.options.blueRobotButton.isSelected()) {

	// gui.log.setCurrentColour("Blue");

	// teamYellow = false;

	// }

	//

	// // Case for attack options

	// if(gui.options.attackLeft.isSelected()) {

	// gui.log.setCurrentAttackGoal("Left");

	// } else if(gui.options.attackRight.isSelected()) {

	// gui.log.setCurrentAttackGoal("Right");

	// }

	//

	// // Case for mode options

	// if(gui.options.penaltyAttack.isSelected()) {

	// gui.log.setCurrentMode("Penalty Attack");

	// } else if(gui.options.penaltyDefend.isSelected()) {

	// gui.log.setCurrentMode("Penalty Defend");

	// } else if (gui.options.normal.isSelected()) {

	// gui.log.setCurrentMode("Normal");

	// }

	// System.out.println("Apply Clicked");

	// applyClicked = true;

	//

	// }

}
