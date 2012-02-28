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
import Strategy.*;


public class Runner extends Thread { //implements ActionListener {

	// Objects
	public static Ball ball;
	public static Robot nxt;
	public static Robot enemy;
	static Robot blueRobot;
	static Robot yellowRobot;	
	static WorldState state;
	static Runner instance = null;
	boolean usingSimulator = false;
	private static ControlGUI thresholdsGUI;
	Vision vision;
	PathPlanner planner = new PathPlanner();
	MainGui gui;
	Strategy s;

	Point ourNXT = new Point();
	Point otherNXT = new Point();
	Point ballPoint = new Point();

	// game flags
	boolean teamYellow = true;
	boolean attackLeft = false;
	//	int mode = 0;
	boolean isPenaltyAttack = false;
	boolean isPenaltyDefend = false;
	boolean applyClicked = false;
	boolean isMainPitch = false;
	String constantsLocation;

	//Positions
	Position pitchCentre = null;
	Position ourGoal = null;
	Position theirGoal = null;
	Position leftGoalMain = new Position(40,250);
	Position leftGoalSide = new Position(62,233);
	Position rightGoalMain = new Position(608,242);
	Position rightGoalSide = new Position(567,238);
	Position centreMain = new Position(284,246);
	Position centreSide = new Position(253,236);
	
	int topY = 0;
	int lowY = 0;
	int leftX = 0;
	int rightX = 0;
	
	int mainTopY = 80;
	int mainLowY = 392;
	int mainLeftX = 40;
	int mainRightX = 608;
	
	int sideTopY = 92;
	int sideLowY = 369;
	int sideLeftX = 62;
	int sideRightX = 567;

	Position dest = new Position(0,0);



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

		// Set centres and goals based on pitch choice and shooting direction
		if (isMainPitch) {
			pitchCentre = centreMain;
			topY = mainTopY;
			lowY = mainLowY;
			leftX = mainLeftX;
			rightX = mainRightX;
		} else {
			pitchCentre = centreSide;
			topY = sideTopY;
			lowY = sideLowY;
			leftX = sideLeftX;
			rightX = sideRightX;
		}

		if (attackLeft) {
			if (isMainPitch) {
				ourGoal = rightGoalMain;
				theirGoal = leftGoalMain;
			} else {
				ourGoal = rightGoalSide;
				theirGoal = leftGoalSide;
			}
		} else {
			if(isMainPitch) {
				ourGoal = leftGoalMain;
				theirGoal = rightGoalMain;
			} else {
				ourGoal = leftGoalSide;
				theirGoal = rightGoalSide;
			}
		}

		if (teamYellow) {
			nxt = yellowRobot;
			enemy = blueRobot;

		} else {
			nxt = blueRobot;
			enemy = yellowRobot;
		}

		startVision();

		// start communications with our robot
		nxt.startCommunications();

		try {
			mainLoop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	private void getUserOptions() {
		teamYellow = gui.getTeam();
		attackLeft = gui.getAttackLeft();
		isPenaltyAttack = gui.getIsPenaltyAttack();
		isPenaltyDefend = gui.getIsPenaltyDefend();
		isMainPitch = gui.getIsMainPitch();
		constantsLocation = gui.getConstantsFile();
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
		PitchConstants pitchConstants = new PitchConstants(constantsLocation);

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


	private void mainLoop() throws InterruptedException {
		getPitchInfo();
		s = new Strategy(instance);
		Thread strategy = new Thread(s);

		strategy.start();

		//		int mode = Strategy.whatToDo(nxt, enemy, ball, ourGoal, theirGoal, pitchCentre);		
		while (true) {
			if (isPenaltyAttack) {
				penaltyAttack();
			} else if (isPenaltyDefend) {
				penaltyDefend();
			}
			switch(s.getCurrentMode()) {
			case(0):
				modeZero();	
			break;
			case(1): 
				modeOne();
			break;
			case(2):
				modeTwo();
			break;
			case(3):
				try {
					modeThree();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			case(4):
				modeFour();
			break;
			case(5):
				modeFive();
			break;
			default:
				modeZero();
				break;
				
			}
			Thread.sleep(1000);
		}
	}


	private void penaltyDefend() throws InterruptedException {
		getPitchInfo();
		Position ballInitial = new Position(ball.getCoors().getX(), ball.getCoors().getY());
		int dist = 0;
		int difference;
		long time = System.currentTimeMillis();
		penaltyLoop:
			while(true) {
				while(System.currentTimeMillis() - time < 30000) {
					getPitchInfo();
					dist = (int) Position.sqrdEuclidDist(ball.getCoors().getX(), ball.getCoors().getY(), ballInitial.getX(), ballInitial.getY());
					if (dist > 10) {
						difference = ballInitial.getY() - ball.getCoors().getY();
						if (Math.abs(difference) > 5 ) {
							if (difference > 0) { 
								nxt.moveForward(35);
								Thread.sleep(1000);
								nxt.stop();
							} else if(difference < 0) { 
								nxt.moveBackward(35);
								Thread.sleep(1000);
								nxt.stop();
							} else {
								Thread.sleep(1000);
								break penaltyLoop;
							}

						}
					}
				}
				break;
			}
	}


	private void penaltyAttack() {
		int angle = -35 + (int)(Math.random()*35);
		nxt.rotateRobot(angle);
		nxt.kick();
	}

	private void modeZero() throws InterruptedException {
		System.out.println("Change to mode 0");
		ModeZeroLoop:
			while(s.getCurrentMode() == 0) {
				getPitchInfo();

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

					if (attackLeft) {
						ball.setCoors(new Position(ball.getCoors().getX() + 50, ball.getCoors().getY()));
					} else {
						ball.setCoors(new Position(ball.getCoors().getX() - 50, ball.getCoors().getY()));
					}
					int m = Move.getAngleToBall(nxt, ball);

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

				// Initial robot rotation
				nxt.rotateRobot(angle);
				nxt.moveForward(25);

				int dist = Move.getDist(nxt, ball);

				while(dist > 35) { // dist in pixels
					if (s.getCurrentMode() != 0) {
						break ModeZeroLoop;
					}
					System.out.println("Distance: " + dist);
					getPitchInfo();
					dist = Move.getDist(nxt, ball);
					int n = Move.getAngleToBall(nxt, ball);

					if((Math.abs(n) > 20)) {
						nxt.stop();
						nxt.rotateRobot(n);

						getPitchInfo();

						nxt.moveForward(20);

					} else {
						getPitchInfo();

					}
				}	

				getPitchInfo();
				
				// Check if the ball is close to any of the walls - if it is, just kick it
				if ((ball.getCoors().getX() < (leftX + 50)) || (ball.getCoors().getX() < (rightX - 50))
						|| (ball.getCoors().getY() > (topY + 50)) || (ball.getCoors().getY() < (lowY - 50))) {
					nxt.kick();
				} else {
				
				Ball goal = new Ball();
				goal.setCoors(new Position(theirGoal.getX(), theirGoal.getY()));
				angle = Move.getAngleToBall(nxt, goal);
				// rotate to goal
				nxt.rotateRobot(angle);
				nxt.moveForward(35);
				Thread.sleep(1000);
				nxt.kick();

				nxt.stop();
				
				}
			}

	}

	private void modeOne() {
		System.out.println("Change to mode 1");
		while(s.getCurrentMode() == 1) {
			int dist = (int) Position.sqrdEuclidDist(nxt.getCoors().getX(), nxt.getCoors().getY(), theirGoal.getX(), theirGoal.getY());
			int angle;
			Position wallPoint = new Position(dist/2, 80);

			Ball wallPointBall = new Ball();
			wallPointBall.setCoors(wallPoint);

			angle = Move.getAngleToBall(nxt, wallPointBall);

			nxt.rotateRobot(angle);
			nxt.kick();
		}

	}

	private void modeTwo() {
		System.out.println("Change to mode 2");
		while(s.getCurrentMode() == 2) {

			getPitchInfo();
			Ball goal = new Ball();
			goal.setCoors(theirGoal);

			int angle = Move.getAngleToBall(nxt, goal);
			nxt.rotateRobot(angle);

			while((nxt.getCoors().getX() < pitchCentre.getX()) && s.getCurrentMode() == 2) {
				nxt.moveForward(50);
			}
			nxt.kick();
		}

	}

	private void modeThree() throws InterruptedException {
		System.out.println("Change to mode 3");
		ModeThreeLoop:
			while(s.getCurrentMode() == 3) {

				Ball inFrontOfGoal = new Ball();
				Ball rotatePoint = new Ball();
				if (attackLeft) {
					inFrontOfGoal.setCoors(new Position(ourGoal.getX() - 60, ourGoal.getY()));
				} else {
					inFrontOfGoal.setCoors(new Position(ourGoal.getX() + 60, ourGoal.getY()));
				}

				int angle = Move.getAngleToBall(nxt, inFrontOfGoal);
				nxt.rotateRobot(angle);
				while((Move.getDist(nxt, inFrontOfGoal) > 5)) {
					if (s.getCurrentMode() != 3) {
						break ModeThreeLoop;
					}
					nxt.moveForward(50);
				}
				nxt.stop();

				rotatePoint.setCoors(new Position(nxt.getCoors().getX(), nxt.getCoors().getY()-50));
				angle = Move.getAngleToBall(nxt, rotatePoint);
				nxt.rotateRobot(angle);

				nxt.moveForward(30);
				Thread.sleep(1000);
				nxt.stop();
				nxt.moveBackward(30);
				Thread.sleep(2000);
				nxt.stop();
				nxt.moveForward(30);
				Thread.sleep(1000);
			}
	}

	private void modeFour() {

		ModeFourLoop:
			while(s.getCurrentMode() == 4) {

				// determine point ahead of enemy in direction of ball
				int pointAheadX = ball.getCoors().getX() + (ball.getCoors().getX() - enemy.getCoors().getX());
				int pointAheadY = ball.getCoors().getY() + (ball.getCoors().getY() - enemy.getCoors().getY());
				Position pointAheadOfEnemy = new Position(pointAheadX, pointAheadY);
				Ball ballPointAheadOfEnemy = new Ball();
				ballPointAheadOfEnemy.setCoors(pointAheadOfEnemy);

				getPitchInfo();

				int angle = Move.getAngleToBall(nxt, ballPointAheadOfEnemy);

				nxt.rotateRobot(angle);
				while((Move.getDist(nxt, ballPointAheadOfEnemy) > 5)) {
					if (s.getCurrentMode() != 4) {
						break ModeFourLoop;
					}
					nxt.moveForward(50);
				}
				nxt.stop();

			}
		nxt.stop();

	}

	private void modeFive() {
		while(s.getCurrentMode() == 5) {
			getPitchInfo();
		}
	}

	private boolean isRightOfBall() {
		return (nxt.getCoors().getX() > ball.getCoors().getX());
	}

	private void behindBallLoop() {

	}

	private void frontOfBallLoop() {

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

	public Robot getOurRobot() {
		return nxt;
	}

	public Robot getTheirRobot() {
		return enemy;
	}
	public Ball getBall() {
		return ball;
	}

	public Position getOurGoal() {
		return ourGoal;
	}

	public Position getTheirGoal() {
		return theirGoal;
	}

	public Position getPitchCentre() {
		return pitchCentre;
	}

}
