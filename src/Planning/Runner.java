package Planning;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import Planning.Move;
import GUI.MainGui;
import JavaVision.*;
import Strategy.*;

public class Runner extends Thread {
	public Position bla = new Position(0, 0);
	public Position gotoBall = new Position(0, 0);
	public ArrayList<Position> test = new ArrayList<Position>();
	Position ballOffsetPosition;
	public ArrayList<Position> waypoints = new ArrayList<Position>();

	// Objects
	private static Ball ball;
	public static Robot nxt;
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

	// Arc fields
	static double rotation;
	static double distance;
	static double arcRadius;
	static double arcAngle;

	static Arc arc = new Arc();

	private static final int DEFAULT_SPEED = 35; // used for move_forward method
	// in Robot
	private static final int EACH_WHEEL_SPEED = 900; // used for
	// each_wheel_speed
	// method in Robot
	private int left_wheel_speed = EACH_WHEEL_SPEED;
	private int right_wheel_speed = EACH_WHEEL_SPEED;

	Position dest = new Position(0, 0);

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
		nxt.set_wheel_speed(20);
//		nxt.travel(30);

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
		// Get the size of the default screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		// Set the the control gui
		gui = new MainGui(instance);
		gui.setSize(650, 400);
		gui.setLocation((int) (dim.getWidth()- 650), 500);
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
		 * Program arguments. Not used.
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
					thresholdsState, pitchConstants, currentCamera, isMainPitch);

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void mainLoop() throws InterruptedException {
		bla.setCoors(100, 100);

		getPitchInfo(false);
		// s = new Strategy(instance);
		// Thread strategy = new Thread(s);

		// strategy.start();

		//		if (!stopFlag) {
		//			if(gui.isModeAvoid()) {
		//				ModeAvoid();
		//			} else {
		//				modeScore();
		//			}
		//		} else {
		//			nxt.stop();
		//			waitForNewInput();
		//		}

		while(!stopFlag) {
			motion(arc.calculateArc(new Point2D.Double(nxt.getCoors().getX(),nxt.getCoors().getY()), 
					new Point2D.Double(ball.getCoors().getX(),ball.getCoors().getY()), 
					new Point2D.Double(ball.getCoors().getX()-100,ball.getCoors().getY()),nxt.getAngle()));
		}
	}

	public static void motion(int type) {
		switch (type) {
		case(Arc.MOTION_ROTATE):
			rotation = arc.getRotation();
			System.out.println("rotate: " + rotation);
			nxt.rotateRobot((int) rotation);
			break;
		case(Arc.MOTION_FORWARD):
			distance = arc.getDistance();
			System.out.println("travel: " + distance);
			nxt.travel((int) ( distance/2.5));
		break;
		case(Arc.MOTION_ARC):
			arcRadius = arc.getArcRadius();
			arcAngle = arc.getArcAngle();
			System.out.println("arc: " + arcRadius + ", " + arcAngle);
			nxt.arc((int)arcRadius, (int)arcAngle);
		break;
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
		while (true) {
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

			nxt.moveForward(20);

			while(dist > 40 && stopFlag == false) { // dist in pixels
				//			System.out.println("dist to ball: " + dist);
				getPitchInfo(false);
				vision.plotPosition(ballOffsetPosition);
				dist = Move.getDist(nxt, ballOffsetPosition);
				int n = Move.getAngleToPosition(nxt, ballOffsetPosition);
				//			System.out.println("angle to offset ball: " + n);

				if((Math.abs(n) > 20)) {
					nxt.rotateRobot(n);
					getPitchInfo(false);
					dist = Move.getDist(nxt, ballOffsetPosition);

					nxt.moveForward(20);
				}
			}

			if (stopFlag == false) {
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
				nxt.moveForward(20);
				try {
					Thread.sleep(1100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				nxt.kick();
				nxt.stop();

				System.out.println("Ball coordinates!!" + ball.getCoors().getX());
			}
			getPitchInfo(false);
			if (isScore) {
				nxt.stop();	
				break;
			}
		}
	}

	private void ModeAvoid() {		
		while (Move.getDist(nxt, ball.getCoors()) > 50){

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

			int dist = Move.getDist(nxt, gotoBall);

			nxt.rotateRobot(angleToBall);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//		 while(true){
			//		 Vision.plotPoints(goals);
			//		 }

			nxt.moveForward(20);
			while (dist > 50) { 
				getPitchInfo(false);
				vision.drawPath(waypoints);
				dist = Move.getDist(nxt, gotoBall);
				int n = Move.getAngleToPosition(nxt, gotoBall);


				if ((Math.abs(n) > 20)) {
					nxt.rotateRobot(n);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					getPitchInfo(false);
					dist = Move.getDist(nxt, gotoBall);
					nxt.moveForward(20);
				}
			}
		}

		//		getPitchInfo(false);
		//		Ball goal = new Ball();
		//		if(attackLeft) {
		//			goal.setCoors(leftGoalSide);
		//		} else {
		//			goal.setCoors(rightGoalSide);
		//		}
		//		int angle = Move.getAngleToPosition(nxt, goal.getCoors());
		//		nxt.rotateRobot(angle);
		//		nxt.moveForward(30);
		//		try {
		//			Thread.sleep(1100);
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}
		//		nxt.kick();
		//		
		//		nxt.stop();

	}

/*	private void line() {
		// arc movement
		getPitchInfo(false);
//		float angle = getAverageAngle();
//		nxt.setAngle(angle);
//
//		int radiusPx = (int) (dist/(2*Math.sin(angleToBall))); // radius in px
//		System.out.println("Radius in PX: " + radiusPx);
//		int radius = (int) convertPxToCm(radiusPx); // radius in cm
//		System.out.println("Radius: " + radius);
//		int a = (int) (Math.PI - (2 * (Math.acos(dist/radiusPx)))); // angle inside circle in radians
//		int s = radius * a; // arc length in cm
//		
//		nxt.set_wheel_speed(30);
//		nxt.travelArcRobot(Math.abs(radius), (int) (s));
		
		int t = 500;
		// Position oldBallCoors = Move.modifyPositionCoors(nxt, new Position(ball.getCoors().getX(), ball.getCoors().getY()));
		Position oldBallCoors = new Position(ball.getCoors().getX(), ball.getCoors().getY());
		int angle = getAverageAngle();
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getPitchInfo(false);
		nxt.setAngle(angle);
		// Position newBallCoors = Move.modifyPositionCoors(nxt, new Position(ball.getCoors().getX(), ball.getCoors().getY()));
		Position newBallCoors =new Position(ball.getCoors().getX(), ball.getCoors().getY());
		Line line = new Line(oldBallCoors, newBallCoors);
		Position intercept = new Position(nxt.getCoors().getX(), line.getYfromX(80));
		nxt.rotateRobot(Move.getAngleToPosition(nxt, intercept));
		
		line.printEquation();
		vision.plotPosition(intercept);
		vision.drawLine(line);		
		int dist = Move.getDist(nxt, new Position(intercept.getX(), intercept.getY()-20));
		nxt.each_wheel_speed(left_wheel_speed, right_wheel_speed);
		int counter = 0;
		while(dist > 80) { // dist in pixels

			vision.drawPos(intercept);
			vision.drawLine(line);
			if(counter == 50){
				counter = 0;

				getPitchInfo(false);
				dist = Move.getDist(nxt, new Position(intercept.getX(), intercept.getY()-20));
				int n = Move.getAngleToPosition(nxt, intercept);

				System.out.println("SPEEDLEFT " + left_wheel_speed);
				System.out.println("SPEEDRIGHT " + right_wheel_speed);

				if((Math.abs(n) > 20)) {
					System.out.println("!!!!ANGLE TOO LARGE: " + n);
					if(n<20) {
						right_wheel_speed *= 0.85;
						nxt.each_wheel_speed(left_wheel_speed, right_wheel_speed);
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("SPEEDLEFT " + left_wheel_speed);
						System.out.println("SPEEDRIGHT " + right_wheel_speed);

					} else {
						left_wheel_speed *= 0.85;
						nxt.each_wheel_speed(left_wheel_speed, right_wheel_speed);try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						System.out.println("SPEEDLEFT " + left_wheel_speed);
						System.out.println("SPEEDRIGHT " + right_wheel_speed);
					}
				 
					if(left_wheel_speed > right_wheel_speed){
						right_wheel_speed = left_wheel_speed;
					} else {
						left_wheel_speed = right_wheel_speed;
					}
					nxt.each_wheel_speed(left_wheel_speed, right_wheel_speed);
					try {
						Thread.sleep(80);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}
			counter++;
		}
		nxt.each_wheel_speed(0, 0);
		getPitchInfo(false);
		Position faceGoal;
		if(attackLeft) {
			faceGoal = new Position(23, 160);
		} else {
			faceGoal = new Position(601, 160);
		}
		int faceGoalAngle = Move.getAngleToPosition(nxt, faceGoal);
		nxt.rotateRobot(faceGoalAngle);
	}*/

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

		if(findPath){
			waypoints = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(), otherRobot.getCoors());

			for (int s = 0; s < waypoints.size(); s++) {
				int distBetweenWaypoint = Move.getDist(nxt, waypoints.get(s));
				if(distBetweenWaypoint < 40) waypoints.remove(s);
			}
			bla.setCoors(waypoints.get(0).getX(),waypoints.get(0).getY());
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

	void plotCurve() {
		getPitchInfo(false);
		ArrayList<Position> pos = new ArrayList<Position>();
		ArrayList<Position> test = new ArrayList<Position>();

		test.add(new Position(nxt.getCoors().getX(),nxt.getCoors().getY()));
		test.add(new Position(Math.abs(nxt.getCoors().getX() - ball.getCoors().getX()), Math.abs(nxt.getCoors().getY() - ball.getCoors().getY())));
		test.add(new Position(ball.getCoors().getX(), ball.getCoors().getY() ));
		test.add(new Position(ball.getCoors().getX(), ball.getCoors().getY()));

		int numpoints = test.size()-2;
		double t;
		double k = .125;
		double x1,x2,y1,y2;
		x1 = test.get(0).getX();
		y1 = test.get(0).getY();

		for(t=k;t<=1+k;t+=k){
			//use Berstein polynomials
			x2=(test.get(0).getX()+t*(-test.get(0).getX()*3+t*(3*test.get(0).getX()-
					test.get(0).getX()*t)))+t*(3*test.get(1).getX()+t*(-6*test.get(1).getX()+
							test.get(1).getX()*3*t))+t*t*(test.get(2).getX()*3-test.get(2).getX()*3*t)+
							test.get(3).getX()*t*t*t;
			y2=(test.get(0).getY()+t*(-test.get(0).getY()*3+t*(3*test.get(0).getY()-
					test.get(0).getY()*t)))+t*(3*test.get(1).getY()+t*(-6*test.get(1).getY()+
							test.get(1).getY()*3*t))+t*t*(test.get(2).getY()*3-test.get(2).getY()*3*t)+
							test.get(3).getY()*t*t*t;


			//draw curve
			x1 = x2;
			y1 = y2;
			pos.add(new Position((int) x1, (int)y1));
		}

		while(true) {
			vision.drawPath(pos);
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

	public void setIsMainPitch(boolean pitch) {
		isMainPitch = pitch;
	}

	public void setCurrentCamera(int camera) {
		currentCamera = camera;
	}
}