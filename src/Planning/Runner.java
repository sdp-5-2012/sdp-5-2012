package Planning;

import java.util.ArrayList;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import Planning.Move;
import GUI.MainGui;
import JavaVision.*;
import Strategy.*;

public class Runner extends Thread {
	public Position bla = new Position(0,0);
	public Position gotoBall = new Position(0,0);
	public ArrayList<Position> waypoints = new ArrayList<Position>();
	Position ballOffsetPosition;

	// Objects
	private static Ball ball;
	private static Robot nxt;
	private static Robot otherRobot;
	private static Robot blueRobot;
	private static Robot yellowRobot;
	private static WorldState state;
	private ControlGUI control;
	private static Runner instance = null;
	private boolean usingSimulator = false;
	private Vision vision;
	private PathPlanner planner;
	private static MainGui gui;
	private Strategy s;
	private boolean stopFlag = false;

	// game flags
	private boolean teamYellow = false;
	private boolean attackLeft = false;
	private boolean isPenaltyAttack = false;
	private boolean isPenaltyDefend = false;
	private boolean isMainPitch = false;
	private String constantsLocation;
	private boolean isScore = false;
	private boolean extremeStrip = false;
	private int currentCamera = 0;

	// Positions
	private Position pitchCentre = null;
	private Position ourGoal = null;
	private Position theirGoal = null;
	private Position leftGoalMain = new Position(40, 250);
	private Position leftGoalSide = new Position(62, 233);
	private Position rightGoalMain = new Position(608, 242);
	private Position rightGoalSide = new Position(567, 238);
	private Position centreMain = new Position(284, 246);
	private Position centreSide = new Position(253, 236);

	private int topY = 0;
	private int lowY = 0;
	private int leftX = 0;
	private int rightX = 0;

	private int mainTopY = 80;
	private int mainLowY = 392;
	private int mainLeftX = 40;
	private int mainRightX = 608;

	private int sideTopY = 92;
	private int sideLowY = 369;
	private int sideLeftX = 62;
	private int sideRightX = 567;

	private static final int DEFAULT_SPEED = 45; // used for move_forward method in Robot
	private static final int EACH_WHEEL_SPEED = 900; // used for each_wheel_speed method in Robot

	public static void main(String args[]) {
		instance = new Runner();
	}

	/**
	 * start the planning thread
	 */
	public Runner() {
		start();
	}

	/**
	 * Planning thread which begins planning loop
	 */
	public void run() {
		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();
		ballOffsetPosition = new Position(0,0);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGui();
			}
		});

		// Planning sleeps until GUI notifies
		synchronized (instance) {
			try {
				instance.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		getUserOptions();
		setPositionInformation();
		planner = new PathPlanner(attackLeft);

		try {
			mainLoop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to set centres and goals based on pitch choice and shooting
	 * direction
	 */
	private void setPositionInformation() {

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
			if (isMainPitch) {
				ourGoal = leftGoalMain;
				theirGoal = rightGoalMain;
			} else {
				ourGoal = leftGoalSide;
				theirGoal = rightGoalSide;
			}
		}
	}

	private void getUserOptions() {
		teamYellow = gui.getTeam();
		attackLeft = gui.getAttackLeft();
		isPenaltyAttack = gui.getIsPenaltyAttack();
		isPenaltyDefend = gui.getIsPenaltyDefend();
		isMainPitch = gui.getIsMainPitch();
	}

	private void createAndShowGui() {
		// Set the the control gui
		gui = new MainGui(instance);
		gui.setSize(625, 400);
		gui.setLocation(500, 500);
		gui.setTitle("N.U.K.E Control Panel");
		gui.setResizable(false);
		gui.setVisible(true);
	}

	/**
	 * Method to initiate the vision
	 */
	public void startVision() {
		/**
		 * Creates the control GUI, and initialises the image processing.
		 * 
		 * @param args
		 *            Program arguments. Not used.
		 */
		// Get constants location from gui
		constantsLocation = gui.getConstantsFileLocation();
		WorldState worldState = new WorldState();
		ThresholdsState thresholdsState = new ThresholdsState();

		/* Default to main pitch. */
		PitchConstants pitchConstants = new PitchConstants(constantsLocation);

		control = new ControlGUI(thresholdsState, worldState, pitchConstants);
		control.initGUI();

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
					videoStandard, compressionQuality, worldState,
					thresholdsState, pitchConstants, currentCamera);

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void mainLoop() throws InterruptedException {

		bla.setCoors(100, 100);
		getPitchInfo(false);

		// initiate strategy thread
		s = new Strategy(instance);
		Thread strategy = new Thread(s);
		strategy.start();
		modeHundred();
		
		while (true) {
			if(!stopFlag) {		

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
				case(6):
					modeSix();
				break;
				default:
					modeZero();
					break;

				}
				Thread.sleep(1000);
			} else {
				nxt.stop();
				waitForNewInput();
			}
		}
	}

	/**
	 * Method stops robot and waits for new input from GUI
	 */
	private void waitForNewInput() {
		// Planning sleeps until Gui notifies
		synchronized (instance) {
			try {
				instance.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		getUserOptions();
		setPositionInformation();
		setRobotColour();
	}

	//	ModeStart
	private void modeHundred() {
		if(attackLeft){
			double boo = Math.random();
			Position positionNearBall = new Position (0,0); 
			Position onWall = new Position (0,0);
			if(boo > 0.5){

				//			Gets the robot to either just right or just left of the ball 
				getPitchInfo(false);
				int positionNearBallX = ball.getCoors().getX()+10;
				int positionNearBallY = ball.getCoors().getY()+10;

				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt, positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while((Move.getDist(nxt, positionNearBall) > 5)) {
					if (!stopFlag) {
						break;
					}
					nxt.moveForward(DEFAULT_SPEED);
				}
				nxt.stop();

				//			Get angle to the point on the wall (in line with the mid point between the
				//			ball and the centre of goal)

				int halfDist = Move.getDist(nxt, theirGoal) /2;
				int onWallX = ball.getCoors().getX() - halfDist;
				onWall.setCoors(onWallX, topY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();			
				nxt.stop();

			} else {
				//				Gets the robot to either just right or just left of the ball 			
				getPitchInfo(false);
				int positionNearBallX = ball.getCoors().getX()-10;
				int positionNearBallY = ball.getCoors().getY()-10;
				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt, positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while((Move.getDist(nxt, positionNearBall) > 5)) {
					if (!stopFlag) {
						break;
					}
					nxt.moveForward(DEFAULT_SPEED);
				}
				nxt.stop();

				//				Get angle to the point on the wall (in line with the mid point between the
				//				ball and the centre of goal)

				int halfDist = Move.getDist(nxt, theirGoal) /2;
				int onWallX = ball.getCoors().getX() - halfDist;
				onWall.setCoors(onWallX, lowY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();			
				nxt.stop();
			}
		} else {
			double boo = Math.random();
			Position positionNearBall = new Position (0,0); 
			Position onWall = new Position (0,0);
			if(boo > 0.5){

				//			Gets the robot to either just right or just left of the ball 
				getPitchInfo(false);
				int positionNearBallX = ball.getCoors().getX()+10;
				int positionNearBallY = ball.getCoors().getY()+10;

				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt, positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while((Move.getDist(nxt, positionNearBall) > 5)) {
					if (!stopFlag) {
						break;
					}
					nxt.moveForward(DEFAULT_SPEED);
				}
				nxt.stop();

				//			Get angle to the point on the wall (in line with the mid point between the
				//			ball and the centre of goal)

				int halfDist = Move.getDist(nxt, theirGoal) /2;
				int onWallX = ball.getCoors().getX() + halfDist;
				onWall.setCoors(onWallX, topY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();			
				nxt.stop();

			} else {
				//				Gets the robot to either just right or just left of the ball 			
				getPitchInfo(false);
				int positionNearBallX = ball.getCoors().getX()+10;
				int positionNearBallY = ball.getCoors().getY()-10;
				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt, positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while((Move.getDist(nxt, positionNearBall) > 5)) {
					if (!stopFlag) {
						break;
					}
					nxt.moveForward(DEFAULT_SPEED);
				}
				nxt.stop();

				//				Get angle to the point on the wall (in line with the mid point between the
				//				ball and the centre of goal)

				int halfDist = Move.getDist(nxt, theirGoal) /2;
				int onWallX = ball.getCoors().getX() + halfDist;
				onWall.setCoors(onWallX, lowY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();			
				nxt.stop();
			}
		}
	}
			

			


	

	/**
	 * Mode 0: Default "go to ball, aim, kick"
	 * @throws InterruptedException
	 */
	private void modeZero() throws InterruptedException {
		System.out.println("MODE ZERO");
		while (!stopFlag && s.getCurrentMode() == 0) {
			getPitchInfo(false);
			if (isScore) {
				nxt.stop();	
				break;
			}
			int angle = 0;
			int dist;

			angle = getAverageAngle();

			dist = Move.getDist(nxt, ballOffsetPosition);

			// Initial robot rotation
			nxt.rotateRobot(angle);
			System.out.println("Initial angle " + angle);

			nxt.moveForward(DEFAULT_SPEED);

			//			Thread.sleep(1000);
			//			amIMoving();			

			while(!stopFlag && dist > 40 && s.getCurrentMode() == 0) { // dist in pixels

				getPitchInfo(false);
//				vision.drawPos(ballOffsetPosition);
				dist = Move.getDist(nxt, ballOffsetPosition);
				int n = Move.getAngleToPosition(nxt, ballOffsetPosition);
				//			System.out.println("angle to offset ball: " + n);

				if((Math.abs(n) > 20)) {
					nxt.rotateRobot(n);
					getPitchInfo(false);
					dist = Move.getDist(nxt, ballOffsetPosition);

					nxt.moveForward(DEFAULT_SPEED);
					//					Thread.sleep(1000);
					//					amIMoving();
				}
			}

			if (!stopFlag) {
				nxt.stop();
				//	dist = Move.getDist(nxt, ball);

				if (!extremeStrip) {
					Position goalLocation = new Position(0, 0);
					if (attackLeft) {
						goalLocation = leftGoalSide;
					} else {
						goalLocation = rightGoalSide;
					}
					angle = Move.getAngleToPosition(nxt, goalLocation);
				} else {
					angle = Move.getAngleToPosition(nxt, ball.getCoors());
				}
				getPitchInfo(false);
				if (isScore) {
					nxt.stop();	
					break;
				}

				nxt.rotateRobot(angle);
				nxt.moveForward(DEFAULT_SPEED);
				//				Thread.sleep(1000);
				//
				//				amIMoving();
				try {
					Thread.sleep(1100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				nxt.kick();
				nxt.stop();

				//	System.out.println("Ball coordinates!!" + ball.getCoors().getX());
			}
			getPitchInfo(false);
			if (isScore) {
				nxt.stop();	
				break;
			}
		}
	}

	/**
	 * Mode 1: kick wildly (at an angle - off the wall?)
	 * @throws InterruptedException
	 */
	private void modeOne() {
		System.out.println("Change to mode 1");
		while(!stopFlag && s.getCurrentMode() == 1) {
			int dist = (int) Position.sqrdEuclidDist(nxt.getCoors().getX(), nxt.getCoors().getY(), theirGoal.getX(), theirGoal.getY());
			int angle;
			Position wallPoint = new Position(dist/2, 80);

			angle = Move.getAngleToPosition(nxt, wallPoint);

			nxt.rotateRobot(angle);
			nxt.kick();
		}
	}

	/**
	 * Mode 2: dribble towards enemy half
	 */
	private void modeTwo() {
		System.out.println("Change to mode 2");
		while(!stopFlag && s.getCurrentMode() == 2) {

			getPitchInfo(false);

			int angle = Move.getAngleToPosition(nxt, theirGoal);
			nxt.rotateRobot(angle);

			while((nxt.getCoors().getX() < pitchCentre.getX()) && s.getCurrentMode() == 2) {
				nxt.moveForward(50);
				//				amIMoving();
			}
			nxt.kick();
		}
	}

	/**
	 * Mode 3: retreat to own goal, defend
	 * @throws InterruptedException
	 */
	private void modeThree() throws InterruptedException {
		System.out.println("Change to mode 3");
		ModeThreeLoop:
			while(!stopFlag && s.getCurrentMode() == 3) {

				Position inFrontOfGoal = new Position(0,0);
				Position rotatePoint = new Position(0,0);
				if (attackLeft) {
					inFrontOfGoal.setCoors(ourGoal.getX() - 60,ourGoal.getY());
				} else {
					inFrontOfGoal.setCoors(ourGoal.getX() + 60, ourGoal.getY());
				}

				int angle = Move.getAngleToPosition(nxt, inFrontOfGoal);

				while((Move.getDist(nxt, inFrontOfGoal) > 5)) {

					if (s.getCurrentMode() != 3 || !stopFlag) {
						break ModeThreeLoop;
					}

					//checks if the ball is closer to our goal than us AND we're far enough from the ball to
					//not score an own goal
					while((!stopFlag && (Move.getDist(nxt, ball.getCoors()))>20) && ((Move.getDist(nxt, ourGoal)>(Position.sqrdEuclidDist(ball.getCoors().getX(), ball.getCoors().getY(), ourGoal.getX(), ourGoal.getY()))))) {
						nxt.rotateRobot(angle);
						nxt.moveForward(50);
					}

					while(!stopFlag && (Move.getDist(nxt, ball.getCoors()))<20){
						if (Math.abs(Move.getAngleToPosition(nxt, ball.getCoors())) < 15)
						nxt.rotateRobot(90);
						nxt.moveForward(DEFAULT_SPEED);
						Thread.sleep(1000);
					}
				}
				nxt.stop();

				rotatePoint.setCoors(nxt.getCoors().getX(), nxt.getCoors().getY()-50);
				angle = Move.getAngleToPosition(nxt, rotatePoint);
				nxt.rotateRobot(angle);

				nxt.moveForward(DEFAULT_SPEED);
				Thread.sleep(1000);
				nxt.stop();
				nxt.moveBackward(DEFAULT_SPEED);
				Thread.sleep(2000);
				nxt.stop();
				nxt.moveForward(DEFAULT_SPEED);
				Thread.sleep(1000);
			}
	}

	/**
	 * Method 4: attack hard
	 */
	private void modeFour() {
		System.out.println("MODE FOUR");
		ModeFourLoop:
			while(!stopFlag && s.getCurrentMode() == 4) {

				// determine point ahead of enemy in direction of ball
				int pointAheadX = ball.getCoors().getX() + (ball.getCoors().getX() - otherRobot.getCoors().getX());
				int pointAheadY = ball.getCoors().getY() + (ball.getCoors().getY() - otherRobot.getCoors().getY());
				Position pointAheadOfEnemy = new Position(pointAheadX, pointAheadY);
				Position ballPositionAheadOfEnemy = new Position(0,0);
				ballPositionAheadOfEnemy.setCoors(pointAheadOfEnemy.getX(), pointAheadOfEnemy.getY());

				getPitchInfo(false);

				int angle = Move.getAngleToPosition(nxt, ballPositionAheadOfEnemy);

				nxt.rotateRobot(angle);
				while((Move.getDist(nxt, ballPositionAheadOfEnemy) > 5)) {
					if (s.getCurrentMode() != 4 || !stopFlag) {
						break ModeFourLoop;
					}
					nxt.moveForward(50);
				}
				nxt.stop();
			}
		nxt.stop();
	}

	/**
	 * Mode Five: Avoid enemy robot (uses Path planner)
	 */
	private void modeFive() {	
		System.out.println("MODE FIVE");
		while (!stopFlag && s.getCurrentMode() == 5 && Move.getDist(nxt, ball.getCoors()) > 50){

			getPitchInfo(true);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int avgAngle = getAverageAngle();

			nxt.setAngle(avgAngle);

			getPitchInfo(false);
			int angleToBall = Move.getAngleToPosition(nxt, gotoBall);

			//			int dist = Move.getDist(nxt, gotoBall);

			nxt.rotateRobot(angleToBall);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//		 while(true){
			//		 Vision.plotPoints(goals);
			//		 }
			nxt.moveForward(DEFAULT_SPEED);
			while (!stopFlag && s.getCurrentMode() == 5 && Move.getDist(nxt, gotoBall) > 15) { 
				getPitchInfo(false);
//				Vision.plotPoints(waypoints);
				//				dist = Move.getDist(nxt, gotoBall);
				int n = Move.getAngleToPosition(nxt, gotoBall);

				if ((Math.abs(n) > 20)) {
					nxt.rotateRobot(n);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					getPitchInfo(false);
					//					dist = Move.getDist(nxt, gotoBall);
					nxt.moveForward(DEFAULT_SPEED);
				}
			}
		}
	}

	/**
	 *  Mode Six: kicker up and move back (we may be hiding the ball)
	 */
	private void modeSix() {
		System.out.println("MODE SIX");
		while(!stopFlag && s.getCurrentMode() == 6) {
			nxt.rotateRobot(Move.getAngleToPosition(nxt, theirGoal));
			nxt.moveForward(DEFAULT_SPEED);
			nxt.kick();
			nxt.stop();
			getPitchInfo(false);
		}
	}

	private void penaltyDefend() throws InterruptedException {
		getPitchInfo(false);
		Position ballInitial = new Position(ball.getCoors().getX(), ball.getCoors().getY());
		int dist = 0;
		int difference;
		long time = System.currentTimeMillis();
		penaltyLoop:
			while(!stopFlag) {
				while(!stopFlag && System.currentTimeMillis() - time < 30000) {
					getPitchInfo(false);
					dist = (int) Position.sqrdEuclidDist(ball.getCoors().getX(), ball.getCoors().getY(), ballInitial.getX(), ballInitial.getY());
					if (dist > 10) {
						difference = ballInitial.getY() - ball.getCoors().getY();
						if (Math.abs(difference) > 5 ) {
							if (difference > 0) { 
								nxt.moveForward(DEFAULT_SPEED);
								Thread.sleep(1000);
								nxt.stop();
							} else if(difference < 0) { 
								nxt.moveBackward(DEFAULT_SPEED);
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
		double weird = Math.random();
		int angle;		  		

		if(weird > 0.5){
			angle = 20;
		} else {
			angle = -5;
		}
		System.out.println("Angle for penalty: " + angle);
		nxt.rotateRobot(angle);
		nxt.kick();
	}


//	private void amIMoving() {
//		System.out.println("ARE WE MOVING?");
//		nxt.askIfStuck();
//		if(nxt.isStuck()){
//			System.out.println("STTTUUUUCCCKKK");
//			nxt.backOffBitch();
//
//		}
//	}

	private int getAverageAngle() {
		int angle = 0;
		int[] prevResults = new int[10];
		ArrayList<Integer> goodAngles = new ArrayList<Integer>();

		// Sum 10 angles from Vision
		for (int i = 1; i <= 10; i++) {
			getPitchInfo(false);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int m = (int) Runner.nxt.getAngle();

			prevResults[i - 1] = m;

		}

		// Remove outliers

		goodAngles = removeVal(prevResults);
		for (int j = 0; j < goodAngles.size(); j++) {
			angle += goodAngles.get(j);
		}

		// Get the average angle

		angle /= goodAngles.size();
		System.out.println("return from avg angle " + angle);
		return angle;
	}

	/*
	 * Compare the first element with all other elements. If difference > 40
	 * then place in 2nd array Compare lengths of arrays, bigger array has more
	 * similar elements Repeat the comparison step on the bigger array to make
	 * sure that the array only contains similar elements
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

		for (int i = 1; i < nums.length; i++) {
			if ((nums[i] - nums[0] >= -20) && (nums[i] - nums[0] <= 20)) {
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
	private void getPitchInfo(boolean findPath) {

		// Get pitch information from vision
		int i = 0;
		state = vision.getWorldState();
		// while not receiving correct info from Vision

		while (state.getBlueX() == 0) {
			state = vision.getWorldState();
			i++;
		}

		ball.setCoors(new Position(state.getBallX(), state.getBallY()));

		gotoBall.setCoors(bla.getX(), bla.getY());
		extremeStrip = false;
		// set the coordinates of offset ball based on location of ball

		int ballsX = ball.getCoors().getX();
		int ballsY = ball.getCoors().getY();

		if (isMainPitch) {
			if (attackLeft) {
				// Upper Right Box
				if (ballsX > 510 && ballsY < 164) {
					ballOffsetPosition.setCoors(556, 190);
					// Lower Right Box
				} else if (ballsX > 510 && ballsY > 328) {
					ballOffsetPosition.setCoors(556, 303);
					// Top strip (extreme)
				} else if (ballsX <= 142) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY  + 30);
					extremeStrip = true;
					// Top strip (normal)
				} else if (ballsX <= 184) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY - 20);
					// Mid-Upper Strip
				} else if (ballsX <= 224) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY - 10);
					// Middle Strip
				} else if (ballsX <= 265) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY);
					// Mid-Lower Strip
				} else if (ballsX <= 305) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY + 10);
					// Bottom strip (normal)
				} else if (ballsX <= 345) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY + 20);
					// Bottom strip (extreme)
				} else if (ballsX > 345) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY - 30);
				}
			} else if (!attackLeft) {
				// Upper left box
				if (ballsX < 111 && ballsY < 164) {
					ballOffsetPosition.setCoors(75, 190);
					// Lower left box
				} else if (ballsX < 110 && ballsY > 328) {
					ballOffsetPosition.setCoors(75,303);
					// Top strip (extreme)
				} else if (ballsX <= 142) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY  + 30);
					extremeStrip = true;
					// Top strip (normal)
				} else if (ballsX <= 184) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY - 20);
					// Mid-Upper Strip
				} else if (ballsX <= 224) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY - 10);
					// Middle Strip
				} else if (ballsX <= 265) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY);
					// Mid-Lower Strip
				} else if (ballsX <= 305) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY + 10);
					// Bottom strip (normal)
				} else if (ballsX <= 345) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY + 20);
					// Bottom strip (extreme)
				} else if (ballsX > 345) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY - 30);
				}
			}
		} else if (!isMainPitch) {
			if (attackLeft) {
				// Upper Right Box
				if (ballsX > 491 && ballsY < 162) {
					ballOffsetPosition.setCoors(524, 185);
					// Lower Right Box
				} else if (ballsX > 490 && ballsY > 310) {
					ballOffsetPosition.setCoors(525, 288);
					// Top strip (extreme)
				} else if (ballsX <= 146) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY  + 30);
					extremeStrip = true;
					// Top strip (normal)
				} else if (ballsX <= 184) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY - 20);
					// Mid-Upper Strip
				} else if (ballsX <= 221) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY - 10);
					// Middle Strip
				} else if (ballsX <= 258) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY);
					// Mid-Lower Strip
				} else if (ballsX <= 295) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY + 10);
					// Bottom strip (normal)
				} else if (ballsX <= 331) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY + 20);
					// Bottom strip (extreme)
				} else if (ballsX > 331) {
					ballOffsetPosition.setCoors(ballsX + 50, ballsY - 30);
				}
			} else if (!attackLeft) {
				// Upper left box
				if (ballsX < 128 && ballsY < 163) {
					ballOffsetPosition.setCoors(100, 184);
					// Lower left box
				} else if (ballsX < 129 && ballsY > 310) {
					ballOffsetPosition.setCoors(105,288);
					// Top strip (extreme)
				} else if (ballsX <= 146) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY  + 30);
					extremeStrip = true;
					// Top strip (normal)
				} else if (ballsX <= 184) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY - 20);
					// Mid-Upper Strip
				} else if (ballsX <= 221) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY - 10);
					// Middle Strip
				} else if (ballsX <= 258) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY);
					// Mid-Lower Strip
				} else if (ballsX <= 295) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY + 10);
					// Bottom strip (normal)
				} else if (ballsX <= 331) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY + 20);
					// Bottom strip (extreme)
				} else if (ballsX > 331) {
					ballOffsetPosition.setCoors(ballsX - 50, ballsY - 30);
				}
			}
		}

		if (ball.getCoors().getX() < 70 || ball.getCoors().getX() > 560) {
			isScore = true;
		} else {
			isScore = false;
		}
		if (teamYellow) {
			nxt.setAngle(state.getYellowOrientation());
			nxt.setCoors(new Position(state.getYellowX(), state.getYellowY()));

			otherRobot.setAngle(state.getBlueOrientation());
			otherRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));

		} else {
			nxt.setAngle(state.getBlueOrientation());
			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));

			otherRobot.setAngle(state.getYellowOrientation());
			otherRobot.setCoors(new Position(state.getYellowX(), state
					.getYellowY()));

		}
		gui.setCoordinateLabels(nxt.getCoors(), otherRobot.getCoors(), ball.getCoors());

		if(findPath){
			waypoints = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(), otherRobot.getCoors());

			for (int s = 0; s < waypoints.size(); s++) {
				int distBetweenWaypoint = Move.getDist(nxt, waypoints.get(s));
				if(distBetweenWaypoint < 40) waypoints.remove(s);
			}
			bla.setCoors(waypoints.get(0).getX(),waypoints.get(0).getY());
			gotoBall.setCoors(waypoints.get(0).getX(),waypoints.get(0).getY());
		}
	}

	//	public ArrayList<Ball> getPath() {
	//		waypoints = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(),	otherRobot.getCoors());
	//		for (int f = 0; f < waypoints.size(); f++) {
	//			Ball a = new Ball();
	//			a.setCoors(waypoints.get(f));
	//			goals.add(a);
	//		}
	//		System.out.println("PLANNER IN BALLS: "
	//				+ goals.get(goals.size() - 1).getCoors().getX() + " "
	//				+ goals.get(goals.size() - 1).getCoors().getY());
	//		return goals;
	//	}

	public void setRobotColour() {
		if (teamYellow) {
			nxt = yellowRobot;
			otherRobot = blueRobot;

		} else {
			nxt = blueRobot;
			otherRobot = yellowRobot;
		}
	}

	public void setTeamYellow(boolean team) {
		teamYellow = team;
	}

	public Robot getOurRobot() {
		return nxt;
	}

	public Robot getTheirRobot() {
		return otherRobot;
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

	public void setStopFlag(boolean flag) {
		stopFlag = flag;
	}
	
	public void setCurrentCamera(int camera) {
		currentCamera = camera;
	}
}
