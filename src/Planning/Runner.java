package Planning;

import java.awt.Point;
import java.util.ArrayList;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import Planning.Move;
import GUI.MainGui;
import JavaVision.*;
import Strategy.*;

public class Runner extends Thread { 

	// Objects
	public static Ball ball;
	public static Robot nxt;
	public static Robot otherRobot;
	static Robot blueRobot;
	static Robot yellowRobot;	
	static WorldState state;
	ControlGUI control;
	static Runner instance = null;
	boolean usingSimulator = false;
	Vision vision;
	PathPlanner planner = new PathPlanner();
	static MainGui gui;
	Strategy s;
	boolean stopFlag = false;
	Ball newBall;

	Point ourNXT = new Point();
	Point otherNXT = new Point();
	Point ballPoint = new Point();

	// game flags
	boolean teamYellow = true;
	boolean attackLeft = false;
	boolean isPenaltyAttack = false;
	boolean isPenaltyDefend = false;
	boolean applyClicked = false;
	boolean isMainPitch = false;
	String constantsLocation;
	boolean initialVision = false;
	boolean extremeStrip = false;
	boolean isScore = false;

	// Positions
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
		start();
	}

	/**
	 * Planning thread which begins planning loop
	 */
	public void run() {	

		blueRobot = new Robot();
		yellowRobot = new Robot();

		ball = new Ball();	
		newBall = new Ball();

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

		try {
			mainLoop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Method to set centres and goals based on pitch choice and shooting direction
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
			if(isMainPitch) {
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
		gui.setSize(600,400);
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
		 * Creates the control
		 * GUI, and initialises the image processing.
		 * 
		 * @param args        Program arguments. Not used.
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
			vision = new Vision(videoDevice, width, height, channel, videoStandard,
					compressionQuality, worldState, thresholdsState, pitchConstants);

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void mainLoop() throws InterruptedException {
		//		getPitchInfo();
		//		s = new Strategy(instance);
		//		Thread strategy = new Thread(s);

		//		strategy.start();


		if (!stopFlag) {
			if(gui.isModeAvoid()) {
				modeAvoid();
			} else {
				modeScore();
			}
		} else {
			nxt.stop();
			waitForNewInput();
		}
	}

	/**
	 * Method stops robot and waits for new 
	 * input from GUI
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

	public void applyChanges() {
		// Planning sleeps until GUI notifies
		getUserOptions();
		setPositionInformation();
		setRobotColour();		
	}

	/**
	 * Mode 0: Default "go to ball, aim, kick"
	 * @throws InterruptedException
	 */
	public void modeScore() throws InterruptedException {
		//		ArrayList<Position> goals = new ArrayList<Position>();
		//		goals = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(), otherRobot.getCoors());
		//		Position goal = new Position(0,0);
		//		Ball goalBall = new Ball();		

		while (true) {

			getPitchInfo();
			if (isScore) {
				nxt.stop();	
				break;
			}
			int angle = 0;
			int dist;

			angle = getAverageAngle();

			dist = Move.getDist(nxt, newBall);

			// Initial robot rotation
			nxt.rotateRobot(angle);
			System.out.println("Initial angle " + angle);

			nxt.moveForward(20);

			while(dist > 30 && stopFlag == false) { // dist in pixels
				//			System.out.println("dist to ball: " + dist);
				getPitchInfo();
				vision.drawPos(newBall.getCoors());
				dist = Move.getDist(nxt, newBall);
				int n = Move.getAngleToBall(nxt, newBall);
				//			System.out.println("angle to offset ball: " + n);

				if((Math.abs(n) > 15)) {
					nxt.rotateRobot(n);
					getPitchInfo();
					dist = Move.getDist(nxt, newBall);

					nxt.moveForward(20);
				}
			}

			if (stopFlag == false) {
				nxt.stop();
				//	dist = Move.getDist(nxt, ball);

				if (!extremeStrip) {
					Ball goalLoc = new Ball();
					if (attackLeft) {
						goalLoc.setCoors(leftGoalSide);
					} else {
						goalLoc.setCoors(rightGoalSide);
					}
					angle = Move.getAngleToBall(nxt, goalLoc);
				} else {
					angle = Move.getAngleToBall(nxt, ball);
				}
				getPitchInfo();
				if (isScore) {
					nxt.stop();	
					break;
				}

				nxt.rotateRobot(angle);
				nxt.moveForward(20);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				nxt.kick();
				nxt.stop();

				System.out.println("Ball coordinates!!" + ball.getCoors().getX());
			}
			getPitchInfo();
			if (isScore) {
				nxt.stop();	
				break;
			}

		}

	}

	private void modeAvoid() {
		nxt.moveForward(25);

		getPitchInfo();
		Ball otherRobotBall = new Ball();
		otherRobotBall.setCoors(new Position(otherRobot.getCoors().getX(), otherRobot.getCoors().getY()));
		int differenceX = Math.abs(nxt.getCoors().getX() - otherRobotBall.getCoors().getX());
		int differenceY = Math.abs(nxt.getCoors().getX() - otherRobotBall.getCoors().getX());
		boolean diffX = (differenceX > differenceY) ? true:false; 

		while (Math.abs(Move.getAngleToBall(nxt, otherRobotBall)) < 15 && !stopFlag) {

			getPitchInfo();

			if((Move.getDist(nxt, otherRobotBall) > 75)) {
				nxt.moveForward(25);
			} else {
				nxt.stop();
			}
		}
		//when robot is facing goal with robot obstructing
		if(diffX) {
			if (nxt.getCoors().getX() < otherRobotBall.getCoors().getX()) {
				if (otherRobotBall.getCoors().getY() < 145) {
					nxt.rotateRobot(60);
				} else {
					nxt.rotateRobot(-60);
				}
			}


			else if (nxt.getCoors().getX() > otherRobotBall.getCoors().getX()) {
				if (otherRobotBall.getCoors().getY() < 145) {
					nxt.rotateRobot(-60);
				} else {
					nxt.rotateRobot(60);
				}

			}
		}

		//when robot is facing adjacent wall with robot obstructing
		else if(!diffX) {
			if (nxt.getCoors().getY() < otherRobotBall.getCoors().getY()) {
				if (otherRobotBall.getCoors().getX() < 280) {
					nxt.rotateRobot(60);
				} else {
					nxt.rotateRobot(-60);
				}
			}
			else if (nxt.getCoors().getY() > otherRobotBall.getCoors().getY()) {
				if (otherRobotBall.getCoors().getY() < 280) {
					nxt.rotateRobot(-60);
				} else {
					nxt.rotateRobot(60);
				}
			}

		}

		nxt.moveForward(25);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		nxt.stop();
	}

	private int getAverageAngle() {
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
				newBall.setCoors(new Position(newBall.getCoors().getX(), newBall.getCoors().getY()));
			} else {
				newBall.setCoors(new 
						Position(newBall.getCoors().getX(), newBall.getCoors().getY()));
			}
			int m = Move.getAngleToBall(nxt, newBall);

			if (i < 11) {
				prevResults[i-1] = m;
			}
		}
		// Remove outliers
		goodAngles = removeVal(prevResults);

		for (int j = 0; j < goodAngles.size(); j++) {
			angle += goodAngles.get(j);
			System.out.println(goodAngles.get(j));
		}

		// Get the average angle
		angle /= goodAngles.size();
		System.out.println("return from avg angle " + angle);
		return angle;
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
		int i=0;
		state = vision.getWorldState();
		// while not receiving correct info from Vision
		while(state.getBlueX()==0){
			state = vision.getWorldState();
			i++;
		}

		ball.setCoors(new Position(state.getBallX(), state.getBallY()));

		// set the coordinates of offset ball based on location of ball
		if(attackLeft){
			// Extreme top Strip attacking left
			if(ball.getCoors().getY() <145){
				// when moving to this one, no rotation to goal required
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()+30));
				extremeStrip = true;
				// Normal top strip 
			} else 	if(ball.getCoors().getY() <187){
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()-30));
				// Extreme bottom strip
			} else if (ball.getCoors().getY() >325){
				// when moving to this one, no rotation to goal required
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()-30));
				extremeStrip = true;
				// Normal bottom strip
			} else if (ball.getCoors().getY() >281){
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()+30));
				// Upper Middle strip
			} else if (187<= ball.getCoors().getY() && ball.getCoors().getY() <= 280 ){
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()-15));
			}
			// Lower Middle strip
			else {
				newBall.setCoors(new Position(ball.getCoors().getX()+50, ball.getCoors().getY()+15));
			}
		} else {
			// Extreme top Strip attacking right
			if(ball.getCoors().getY() <145){
				// when moving to this one, no rotation to goal required
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()+30));
				extremeStrip = true;
			} else if(ball.getCoors().getY() <187){
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()-30));
				// Extreme bottom strip
			} else if (ball.getCoors().getY() >325){
				// when moving to this one, no rotation to goal required
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()-30));
				extremeStrip = true;
				// Normal bottom strip
			} else if (ball.getCoors().getY() >281){
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()+30));
				// Upper Middle strip
			} else if (187<= ball.getCoors().getY() && ball.getCoors().getY() <= 229 ){
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()-15));
			}
			// Lower Middle Strip
			else {
				newBall.setCoors(new Position(ball.getCoors().getX()-50, ball.getCoors().getY()+15));
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
			ourNXT.x = state.getYellowX();
			ourNXT.y = state.getYellowY();

			otherRobot.setAngle(state.getBlueOrientation());
			otherRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
			otherNXT.x = state.getBlueX();
			otherNXT.y = state.getBlueY();


		} else {
			nxt.setAngle(state.getBlueOrientation());
			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));

			otherRobot.setAngle(state.getYellowOrientation());
			otherRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
			otherNXT.x = state.getYellowX();
			otherNXT.y = state.getYellowY();
		}

		gui.setCoordinateLabels(nxt.getCoors(), otherRobot.getCoors(), ball.getCoors());
	}

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

}
