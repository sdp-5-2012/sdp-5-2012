package Planning;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import Planning.Move;
import GUI.MainGui;
import JavaVision.*;
import Strategy.*;

public class Intercept extends Thread {
	public Position bla = new Position(0,0);
	public Position gotoBall = new Position(0,0);
//	public ArrayList<Position> waypoints = new ArrayList<Position>();
	public ArrayList<Position> waypoints = new ArrayList<Position>();
	Position ballOffsetPosition;
	
	// Objects
	public static Ball ball;
	public static Robot nxt;
	public static Robot otherRobot;
	static Robot blueRobot;
	static Robot yellowRobot;
	static WorldState state;
	ControlGUI control;
	static Intercept instance = null;
	boolean usingSimulator = false;
	Vision vision;
	PathPlanner planner;
	static MainGui gui;
	Strategy s;
	boolean stopFlag = false;
	
	Point ourNXT = new Point();
	Point otherNXT = new Point();
	Point ballPoint = new Point();

	// game flags
	boolean teamYellow = false;
	boolean attackLeft = false;
	boolean isPenaltyAttack = false;
	boolean isPenaltyDefend = false;
	boolean applyClicked = false;
	boolean isMainPitch = false;
	String constantsLocation;
	boolean initialVision = false;
	boolean isScore = false;
	boolean extremeStrip = false;

	// Positions
	Position pitchCentre = null;
	Position ourGoal = null;
	Position theirGoal = null;
	Position leftGoalMain = new Position(40, 250);
	Position leftGoalSide = new Position(62, 233);
	Position rightGoalMain = new Position(608, 242);
	Position rightGoalSide = new Position(567, 238);
	Position centreMain = new Position(284, 246);
	Position centreSide = new Position(253, 236);

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

	Position dest = new Position(0, 0);

	public static final int DEFAULT_SPEED = 35; // used for move_forward method
	// in Robot
	public static final int EACH_WHEEL_SPEED = 900; // used for each_wheel_speed
	// method in Robot
	public static void main(String args[]) {
		instance = new Intercept();
	}

	/**
	 * Instantiate objects and start the planning thread
	 */

	public Intercept() {
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
		gui.setSize(600, 400);
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
					thresholdsState, pitchConstants);

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void mainLoop() throws InterruptedException {
		getPitchInfo(false);
//		Position oldBallCoors = Move.modifyPositionCoors(nxt, new Position(ball.getCoors().getX(), ball.getCoors().getY()));
		Position oldBallCoors = new Position(ball.getCoors().getX(), ball.getCoors().getY());
		int angle = getAverageAngle();
		Thread.sleep(1500);
		getPitchInfo(false);
		
//		Position newBallCoors = Move.modifyPositionCoors(nxt, new Position(ball.getCoors().getX(), ball.getCoors().getY()));
		Position newBallCoors =new Position(ball.getCoors().getX(), ball.getCoors().getY());
		Line line = new Line(oldBallCoors, newBallCoors);
		Position intercept = new Position(newBallCoors.getX() + 30, line.getYfromX(newBallCoors.getX() + 30));
		
		line.printEquation();
		System.out.println("intercept X");
		System.out.println("1 " + line.getP1().getX() + " " + line.getP1().getY());
		System.out.println("2 " + line.getP2().getX() + " " + line.getP2().getY());
		vision.drawPos(intercept);
		vision.drawLine(line);

		nxt.rotateRobot(Move.getAngleToPosition(nxt, intercept));
		while(Move.getDist(nxt, intercept) > 30) {
			getPitchInfo(false);
			nxt.moveForward(50);
			vision.drawLine(line);
			vision.drawPos(intercept);
		}
		nxt.stop();
		
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

	public void applyChanges() {
		// Planning sleeps until GUI notifies
		getUserOptions();
		setPositionInformation();
		setRobotColour();
	}

	
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

			int m = (int) Intercept.nxt.getAngle();

			prevResults[i - 1] = m;

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
	public void getPitchInfo(boolean findPath) {

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
		if(attackLeft){
			// Extreme top Strip attacking left
			if(ball.getCoors().getY() <130){
				// when moving to this one, no rotation to goal required
				ballOffsetPosition.setCoors(ball.getCoors().getX()+50, ball.getCoors().getY()+30);
				extremeStrip = true;
				// Normal top strip 
			} else 	if(ball.getCoors().getY() <187){
				ballOffsetPosition.setCoors(ball.getCoors().getX()+50, ball.getCoors().getY()-30);
				// Extreme bottom strip
			} else if (ball.getCoors().getY() >350){
				// when moving to this one, no rotation to goal required
				ballOffsetPosition.setCoors(ball.getCoors().getX()+50, ball.getCoors().getY()-30);
				extremeStrip = true;
				// Normal bottom strip
			} else if (ball.getCoors().getY() >281){
				ballOffsetPosition.setCoors(ball.getCoors().getX()+50, ball.getCoors().getY()+30);
				// Upper Middle strip
			} else if (187<= ball.getCoors().getY() && ball.getCoors().getY() <= 280 ){
				ballOffsetPosition.setCoors(ball.getCoors().getX()+50, ball.getCoors().getY()-5);
			}
			// Lower Middle strip
			else {
				ballOffsetPosition.setCoors(ball.getCoors().getX()+50, ball.getCoors().getY()+5);
			}
		} else {
			// Extreme top Strip attacking right
			if(ball.getCoors().getY() <130){
				// when moving to this one, no rotation to goal required
				ballOffsetPosition.setCoors(ball.getCoors().getX()-50, ball.getCoors().getY()+30);
				extremeStrip = true;
			} else if(ball.getCoors().getY() <187){
				ballOffsetPosition.setCoors(ball.getCoors().getX()-50, ball.getCoors().getY()-30);
				// Extreme bottom strip
			} else if (ball.getCoors().getY() >350){
				// when moving to this one, no rotation to goal required
				ballOffsetPosition.setCoors(ball.getCoors().getX()-50, ball.getCoors().getY()-30);
				extremeStrip = true;
				// Normal bottom strip
			} else if (ball.getCoors().getY() >281){
				ballOffsetPosition.setCoors(ball.getCoors().getX()-50, ball.getCoors().getY()+30);
				// Upper Middle strip
			} else if (187<= ball.getCoors().getY() && ball.getCoors().getY() <= 229 ){
				ballOffsetPosition.setCoors(ball.getCoors().getX()-50, ball.getCoors().getY()-5);
			}
			// Lower Middle Strip
			else {
				ballOffsetPosition.setCoors(ball.getCoors().getX()-50, ball.getCoors().getY()+5);
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