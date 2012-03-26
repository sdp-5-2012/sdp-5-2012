package Planning;

import java.awt.Dimension;
import java.awt.Toolkit;
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

	// Objects
	public static Ball ball;
	public static Robot nxt;
	public static Robot otherRobot;
	public static Robot blueRobot;
	public static Robot yellowRobot;
	public static WorldState state;
	public ControlGUI control;
	public static Runner instance = null;
	public boolean usingSimulator = false;
	public Vision vision;
	public PathPlanner planner;
	public static MainGui gui;
	public Strategy s;
	public boolean stopFlag = false;

	// game flags
	public boolean teamYellow = false;
	public boolean attackLeft = false;
	public boolean isPenaltyAttack = false;
	public boolean isPenaltyDefend = false;
	public boolean isMainPitch = false;
	public String constantsLocation;
	public boolean isScore = false;
	public boolean extremeStrip = false;
	public int currentCamera = 0;

	// Positions
	public Position pitchCentre = null;
	public Position ourGoal = null;
	public Position theirGoal = null;
	public Position leftGoalMain = new Position(40, 250);
	public Position leftGoalSide = new Position(62, 233);
	public Position rightGoalMain = new Position(608, 242);
	public Position rightGoalSide = new Position(567, 238);
	public Position centreMain = new Position(284, 246);
	public Position centreSide = new Position(253, 236);

	// addednewsplit

	/*
	 * These following values should really be read from the pitch constants, if possible.
	 * That would mean they wouldn't be explicitly stated here but would get calculated
	 * from the buffer values in the constants files
	 */

	public Position leftGoal = null;
	public Position rightGoal = null;

	public static int topY = 0;
	public static int lowY = 0;
	public static int leftX = 0;
	public static int rightX = 0;

	public static int strip1 = 0;
	public static int strip2 = 0;
	public static int strip3 = 0;
	public static int strip4 = 0;
	public static int strip5 = 0;
	public static int strip6 = 0;

	public static int cornerTopLeftX = 0;
	public static int cornerTopLeftY = 0;
	public static int cornerTopRightX = 0;
	public static int cornerTopRightY = 0;
	public static int cornerBottomLeftX = 0;
	public static int cornerBottomLeftY = 0;
	public static int cornerBottomRightX = 0;
	public static int cornerBottomRightY = 0;

	//    public int mainTopY = 80;
	//    public int mainLowY = 392;
	//    public int mainLeftX = 40;
	//    public int mainRightX = 608;
	//    
	//    public int sideTopY = 92;
	//    public int sideLowY = 369;
	//    public int sideLeftX = 62;
	//    public int sideRightX = 567; 

	// endofadd

	public static final int DEFAULT_SPEED = 35; // used for move_forward method
	// in Robot
	public static final int EACH_WHEEL_SPEED = 900; // used for
	// each_wheel_speed
	// method in Robot

	long lr;

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
		ballOffsetPosition = new Position(0, 0);

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
		lr = System.currentTimeMillis();

		getPitchInfo(false);

		//nxt.steer(50, Move.getAngleToPosition(nxt, ball.getCoors()));

		try {
			mainLoop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Method to set centres and goals based on pitch choice and shooting
	 * direction - POSSIBLY RENDERED COMPLETELY VOID
	 */
	public void setPositionInformation() {



		if (attackLeft) {
			ourGoal = rightGoal;
			theirGoal = leftGoal;
		} else {
			ourGoal = leftGoal;
			theirGoal = rightGoal;
		}

	}


	public void getUserOptions() {
		teamYellow = gui.getTeam();
		attackLeft = gui.getAttackLeft();
		isPenaltyAttack = gui.getIsPenaltyAttack();
		isPenaltyDefend = gui.getIsPenaltyDefend();
		isMainPitch = gui.getIsMainPitch();
	}

	public void createAndShowGui() {
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

		// addednewsplit
		// The buffer values should be obtained from here, and
		// the top/bottom/left/right walls set as appropriate.
		// Strip values also set.
		topY = pitchConstants.topBuffer;
		lowY = 480 - pitchConstants.bottomBuffer;
		leftX = pitchConstants.leftBuffer;
		rightX = 640 - pitchConstants.rightBuffer;

		strip1 = topY + 50;
		strip6 = lowY - 50;

		int stripDifference = strip6 - strip1;

		strip2 = strip1 + (stripDifference / 5);
		strip3 = strip1 + ((stripDifference / 5) * 2);
		strip4 = strip1 + ((stripDifference / 5) * 3);
		strip5 = strip1 + ((stripDifference / 5) * 4);

		cornerTopLeftX     = leftX + 90;
		cornerBottomLeftX  = leftX + 90;
		cornerTopLeftY     = topY + 70;
		cornerTopRightY    = topY + 70;
		cornerBottomLeftY  = lowY - 70;
		cornerBottomRightY = lowY - 70;
		cornerTopRightX    = rightX - 90;
		cornerBottomRightX = rightX - 90;

		pitchCentre = new Position((int) (leftX + ((rightX - leftX) * 0.5)), (int) (topY + ((lowY - topY) * 0.5)));
		leftGoal = new Position(leftX, (int) (topY + (topY - lowY) * 0.5));
		rightGoal = new Position(rightX, (int) (topY + (topY - lowY) * 0.5));

		// endofadd

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

	/**
	 * Main Planning Loop
	 *
	 * @throws InterruptedException
	 */
	public void mainLoop() throws InterruptedException {

		bla.setCoors(100, 100);
		getPitchInfo(false);

		// initiate strategy thread
		//        s = new Strategy(instance);
		//        Thread strategy = new Thread(s);
		//        strategy.start();

		modeStart();

		while (true) {
			if (!stopFlag) {
				if (isPenaltyAttack) {
					penaltyAttack();
				} else if (isPenaltyDefend) {
					penaltyDefend();
				} else {
					while(true) {
						modeZero();
					}
				}
				//                switch (s.getCurrentMode()) {
				//                    case (0):
				//                        modeZero();
				//                        break;
				//                    case (1):
				//                        modeOne();
				//                        break;
				//                    case (2):
				//                        modeTwo();
				//                        break;
				//                    case (3):
				//                        try {
				//                            modeThree();
				//                        } catch (InterruptedException e) {
				//                            e.printStackTrace();
				//                        }
				//                        break;
				//                    case (4):
				//                        modeFour();
				//                        break;
				//                    case (5):
				//                        modeFive();
				//                        break;
				//                    case (6):
				//                        modeSix();
				//                        break;
				//                    default:
				//                        modeZero();
				//                        break;
				//                        
				//                }
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
	public void waitForNewInput() {
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

	/**
	 * Mode Start - Used at beginning of game
	 */
	public void modeStart() {
		System.out.println("MODE START");
		getPitchInfo(false);
		if (attackLeft) {
			double boo = Math.random();
			//            boo = .4;
			Position positionNearBall = new Position(0, 0);
			Position onWall = new Position(0, 0);
			System.out.println(boo);
			if (boo > 0.5) {

				// Gets the robot to either just right or just left of the ball
				getPitchInfo(false);
				int positionNearBallX = (ball.getCoors().getX()) + 10;
				int positionNearBallY = (ball.getCoors().getY()) + 10;

				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt,
						positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while ((Move.getDist(nxt, positionNearBall) > 30)) {
					if (!stopFlag) {
						break;
					}
					getPitchInfo(false);

					nxt.each_wheel_speed(700, 700);
				}
				nxt.stop();

				// Get angle to the point on the wall (in line with the mid
				// point between the
				// ball and the centre of goal)
				getPitchInfo(false);
				int halfDist = Move.getDist(nxt, theirGoal) / 2;
				int onWallX = ball.getCoors().getX() - halfDist;
				onWall.setCoors(onWallX, topY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();
				nxt.stop();

			} else {
				// Gets the robot to either just right or just left of the ball
				getPitchInfo(false);
				int positionNearBallX = (ball.getCoors().getX()) - 10;
				int positionNearBallY = (ball.getCoors().getY()) - 10;
				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt,
						positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while ((Move.getDist(nxt, positionNearBall) > 30)) {
					if (!stopFlag) {
						break;
					}
					getPitchInfo(false);

					nxt.each_wheel_speed(700, 700);
				}
				nxt.stop();

				// Get angle to the point on the wall (in line with the mid
				// point between the
				// ball and the centre of goal)

				getPitchInfo(false);
				int halfDist = Move.getDist(nxt, theirGoal) / 2;
				int onWallX = ball.getCoors().getX() - halfDist;
				onWall.setCoors(onWallX, lowY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();
				nxt.stop();
			}
		} else {
			getPitchInfo(false);
			double boo = Math.random();
			Position positionNearBall = new Position(0, 0);
			Position onWall = new Position(0, 0);
			if (boo > 0.5) {

				// Gets the robot to either just right or just left of the ball
				getPitchInfo(false);
				int positionNearBallX = ball.getCoors().getX() + 10;
				int positionNearBallY = ball.getCoors().getY() + 10;

				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt,
						positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while ((Move.getDist(nxt, positionNearBall) > 30)) {
					if (!stopFlag) {
						break;
					}
					System.out.println("in while loooop");
					getPitchInfo(false);
					nxt.moveForward(DEFAULT_SPEED);
				}
				nxt.stop();

				// Get angle to the point on the wall (in line with the mid
				// point between the
				// ball and the centre of goal)

				getPitchInfo(false);

				int halfDist = Move.getDist(nxt, theirGoal) / 2;
				int onWallX = ball.getCoors().getX() + halfDist;
				onWall.setCoors(onWallX, topY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();
				nxt.stop();

			} else {
				// Gets the robot to either just right or just left of the ball
				getPitchInfo(false);
				int positionNearBallX = ball.getCoors().getX() + 10;
				int positionNearBallY = ball.getCoors().getY() - 10;
				positionNearBall.setCoors(positionNearBallX, positionNearBallY);
				int angleToNearBall = Move.getAngleToPosition(nxt,
						positionNearBall);
				nxt.rotateRobot(angleToNearBall);

				while ((Move.getDist(nxt, positionNearBall) > 30)) {
					if (!stopFlag) {
						break;
					}
					getPitchInfo(false);
					nxt.moveForward(DEFAULT_SPEED);
				}
				nxt.stop();

				// Get angle to the point on the wall (in line with the mid
				// point between the
				// ball and the centre of goal)

				getPitchInfo(false);
				int halfDist = Move.getDist(nxt, theirGoal) / 2;
				int onWallX = ball.getCoors().getX() + halfDist;
				onWall.setCoors(onWallX, lowY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();
				nxt.stop();
			}
		}
	}

	public void modeAvoid() {
		while (Move.getDist(nxt, ball.getCoors()) > 50) {

			getPitchInfo(true);

			int avgAngle = getAverageAngle();

			nxt.setAngle(avgAngle);

			getPitchInfo(false);
			int angleToBall = Move.getAngleToPosition(nxt, gotoBall);

			int dist = Move.getDist(nxt, gotoBall);

			nxt.rotateRobot(angleToBall);
			System.out.println("GOTOBALL: " + gotoBall.getX() + " "
					+ gotoBall.getY() + " Distance: " + dist);

			while (dist > 20) {
				getPitchInfo(false);
				vision.drawPath(test);
				System.out.println("BASTARD");
				System.out.println("GOTOBALL: " + gotoBall.getX() + " "
						+ gotoBall.getY() + "coors of nxt: "
						+ nxt.getCoors().getX() + " " + nxt.getCoors().getY());
				int n = Move.getAngleToPosition(nxt, gotoBall);
				dist = Move.getDist(nxt, gotoBall);
				System.out.println("Distance to gotoBall: " + dist);

				nxt.each_wheel_speed(250, 250);
				if (n > 10) {
					while (dist > 20 && n > 10) {
						vision.drawPath(test);
						getPitchInfo(false);
						System.out.println("GOTOBALL: " + gotoBall.getX() + " "
								+ gotoBall.getY() + "coors of nxt: "
								+ nxt.getCoors().getX() + " "
								+ nxt.getCoors().getY());
						n = Move.getAngleToPosition(nxt, gotoBall);
						dist = Move.getDist(nxt, gotoBall);
						nxt.each_wheel_speed(50, 250);
						// System.out.println(("dist bigger and angle bigger than 10"));
					}
				}
				if (n < -10) {
					while (dist > 20 && n < -10) {
						vision.drawPath(test);
						getPitchInfo(false);
						System.out.println("GOTOBALL: " + gotoBall.getX() + " "
								+ gotoBall.getY() + "coors of nxt: "
								+ nxt.getCoors().getX() + " "
								+ nxt.getCoors().getY());
						n = Move.getAngleToPosition(nxt, gotoBall);
						dist = Move.getDist(nxt, gotoBall);
						nxt.each_wheel_speed(250, 50);
						// System.out.println(("dist bigger and angle less than 10"));
					}
				}
			}

		}
	}

	/**
	 * Mode 0: Default "go to ball, aim, kick"
	 *
	 * @throws InterruptedException
	 */
	public void modeZero() throws InterruptedException {
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

			// Thread.sleep(1000);
			// amIMoving();

			while (!stopFlag && dist > 30 && s.getCurrentMode() == 0) { // dist
				// in
				// pixels

				getPitchInfo(false);
				// vision.drawPos(ballOffsetPosition);
				dist = Move.getDist(nxt, ballOffsetPosition);
				int n = Move.getAngleToPosition(nxt, ballOffsetPosition);
				// System.out.println("angle to offset ball: " + n);
				vision.plotPosition(ballOffsetPosition);

				if ((Math.abs(n) > 20)) {
					nxt.rotateRobot(n);
					getPitchInfo(false);
					dist = Move.getDist(nxt, ballOffsetPosition);

					nxt.moveForward(DEFAULT_SPEED);
					// Thread.sleep(1000);
					// amIMoving();
				}
			}

			if (!stopFlag) {
				nxt.stop();
				// dist = Move.getDist(nxt, ball);

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
				// Thread.sleep(1000);
				//
				// amIMoving();
				try {
					Thread.sleep(1100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				nxt.kick();
				nxt.stop();

				// System.out.println("Ball coordinates!!" +
				// ball.getCoors().getX());
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
	 *
	 * @throws InterruptedException
	 */
	public void modeOne() {
		System.out.println("Change to mode 1");
		while (!stopFlag && s.getCurrentMode() == 1) {
			int dist = (int) Position.sqrdEuclidDist(nxt.getCoors().getX(), nxt
					.getCoors().getY(), theirGoal.getX(), theirGoal.getY());
			int angle;
			Position wallPoint = new Position(dist / 2, 80);

			angle = Move.getAngleToPosition(nxt, wallPoint);

			nxt.rotateRobot(angle);
			nxt.kick();
		}
	}

	/**
	 * Mode 2: dribble towards enemy half
	 */
	public void modeTwo() {
		System.out.println("Change to mode 2");
		while (!stopFlag && s.getCurrentMode() == 2) {

			getPitchInfo(false);

			int angle = Move.getAngleToPosition(nxt, theirGoal);
			nxt.rotateRobot(angle);

			while ((nxt.getCoors().getX() < pitchCentre.getX())
					&& s.getCurrentMode() == 2) {
				nxt.moveForward(50);
				// amIMoving();
			}
			nxt.kick();
		}
	}

	/**
	 * Mode 3: retreat to own goal, defend
	 *
	 * @throws InterruptedException
	 */
	public void modeThree() throws InterruptedException {
		System.out.println("Change to mode 3");
		ModeThreeLoop: while (!stopFlag && s.getCurrentMode() == 3) {

			Position inFrontOfGoal = new Position(0, 0);
			Position rotatePoint = new Position(0, 0);
			if (attackLeft) {
				inFrontOfGoal.setCoors(ourGoal.getX() - 60, ourGoal.getY());
			} else {
				inFrontOfGoal.setCoors(ourGoal.getX() + 60, ourGoal.getY());
			}

			int angle = Move.getAngleToPosition(nxt, inFrontOfGoal);

			while ((Move.getDist(nxt, inFrontOfGoal) > 5)) {

				if (s.getCurrentMode() != 3 || !stopFlag) {
					break ModeThreeLoop;
				}

				// checks if the ball is closer to our goal than us AND we're
				// far enough from the ball to
				// not score an own goal
				while ((!stopFlag && (Move.getDist(nxt, ball.getCoors())) > 20)
						&& ((Move.getDist(nxt, ourGoal) > (Position
								.sqrdEuclidDist(ball.getCoors().getX(), ball
										.getCoors().getY(), ourGoal.getX(),
										ourGoal.getY()))))) {
					nxt.rotateRobot(angle);
					nxt.moveForward(50);
				}

				while (!stopFlag && (Move.getDist(nxt, ball.getCoors())) < 20) {
					if (Math.abs(Move.getAngleToPosition(nxt, ball.getCoors())) < 15)
						nxt.rotateRobot(90);
					nxt.moveForward(DEFAULT_SPEED);
					Thread.sleep(1000);
				}
			}
			nxt.stop();

			rotatePoint.setCoors(nxt.getCoors().getX(),
					nxt.getCoors().getY() - 50);
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
	public void modeFour() {
		System.out.println("MODE FOUR");
		ModeFourLoop: while (!stopFlag && s.getCurrentMode() == 4) {

			// determine point ahead of enemy in direction of ball
			int pointAheadX = ball.getCoors().getX()
			+ (ball.getCoors().getX() - otherRobot.getCoors().getX());
			int pointAheadY = ball.getCoors().getY()
			+ (ball.getCoors().getY() - otherRobot.getCoors().getY());
			Position pointAheadOfEnemy = new Position(pointAheadX, pointAheadY);
			Position ballPositionAheadOfEnemy = new Position(0, 0);
			ballPositionAheadOfEnemy.setCoors(pointAheadOfEnemy.getX(),
					pointAheadOfEnemy.getY());

			getPitchInfo(false);

			int angle = Move.getAngleToPosition(nxt, ballPositionAheadOfEnemy);

			nxt.rotateRobot(angle);
			while ((Move.getDist(nxt, ballPositionAheadOfEnemy) > 5)) {
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
	public void modeFive() {
		System.out.println("MODE FIVE");
		while (!stopFlag && s.getCurrentMode() == 5
				&& Move.getDist(nxt, ball.getCoors()) > 50) {

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

			// int dist = Move.getDist(nxt, gotoBall);

			nxt.rotateRobot(angleToBall);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// while(true){
			// Vision.plotPoints(new ArrayList<Position> ());
			// }
			nxt.moveForward(DEFAULT_SPEED);
			while (!stopFlag && s.getCurrentMode() == 5
					&& Move.getDist(nxt, gotoBall) > 15) {
				getPitchInfo(false);
				// Vision.plotPoints(waypoints);
				// dist = Move.getDist(nxt, gotoBall);
				int n = Move.getAngleToPosition(nxt, gotoBall);

				if ((Math.abs(n) > 20)) {
					nxt.rotateRobot(n);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					getPitchInfo(false);
					// dist = Move.getDist(nxt, gotoBall);
					nxt.moveForward(DEFAULT_SPEED);
				}
			}
		}
	}

	/**
	 * Mode Six: kicker up and move back (we may be hiding the ball)
	 */
	public void modeSix() {
		System.out.println("MODE SIX");
		while (!stopFlag && s.getCurrentMode() == 6) {
			nxt.rotateRobot(Move.getAngleToPosition(nxt, theirGoal));
			nxt.moveForward(DEFAULT_SPEED);
			nxt.kick();
			nxt.stop();
			getPitchInfo(false);
		}
	}

	public void penaltyDefend() throws InterruptedException {
		getPitchInfo(false);
		Position ballInitial = new Position(ball.getCoors().getX(), ball
				.getCoors().getY());
		int dist = 0;
		int difference;
		long time = System.currentTimeMillis();
		penaltyLoop: while (!stopFlag) {
			while (!stopFlag && System.currentTimeMillis() - time < 30000) {
				getPitchInfo(false);
				dist = (int) Position.sqrdEuclidDist(ball.getCoors().getX(),
						ball.getCoors().getY(), ballInitial.getX(),
						ballInitial.getY());
				if (dist > 10) {
					difference = ballInitial.getY() - ball.getCoors().getY();
					if (Math.abs(difference) > 5) {
						if (difference > 0) {
							nxt.moveForward(DEFAULT_SPEED);
							Thread.sleep(1000);
							nxt.stop();
						} else if (difference < 0) {
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

	public void penaltyAttack() {
		double weird = Math.random();
		int angle;

		if (weird > 0.5) {
			angle = -15;
		} else {
			angle = 15;
		}
		System.out.println("Angle for penalty: " + angle);
		nxt.rotateRobot(angle);
		nxt.kick();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



	public int getAverageAngle() {
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

			// int m = (int) Runner.nxt.getAngle();
			int m = (int) Runner.nxt.getAngle();

			prevResults[i - 1] = m;
			System.out.println(prevResults[i-1]);

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
	public ArrayList<Integer> removeVal(int[] nums) {
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

		// addednewsplit
		int ballsX = ball.getCoors().getX();
		int ballsY = ball.getCoors().getY();
		//if (isMainPitch) {
		if (attackLeft) {
			// Upper Right Box
			if (ballsX > cornerTopRightX && ballsY < cornerTopRightY) {
				ballOffsetPosition.setCoors((int) (cornerTopRightX + ((rightX - cornerTopRightX) * 0.5)), cornerTopRightY + 30);
				// Lower Right Box
			} else if (ballsX > cornerBottomRightX && ballsY > cornerBottomRightY) {
				ballOffsetPosition.setCoors((int) (cornerBottomRightX + ((rightX - cornerBottomRightX) * 0.5)), cornerBottomRightY - 30);
				// Top strip (extreme)
			} else if (ballsY <= strip1) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY + 30);
				extremeStrip = true;
				// Top strip (normal)
			} else if (ballsY <= strip2) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY - 20);
				// Mid-Upper Strip
			} else if (ballsY <= strip3) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY - 10);
				// Middle Strip
			} else if (ballsY <= strip4) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY);
				// Mid-Lower Strip
			} else if (ballsY <= strip5) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY + 10);
				// Bottom strip (normal)
			} else if (ballsY <= strip6) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY + 20);
				// Bottom strip (extreme)
			} else if (ballsY > strip6) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY - 30);
			}
		} else if (!attackLeft) {
			// Upper left box
			if (ballsX < cornerTopLeftX && ballsY < cornerTopLeftY) {
				ballOffsetPosition.setCoors((int) (leftX + ((cornerTopLeftX - leftX) * 0.5)), cornerTopLeftY + 30);
				// Lower left box
			} else if (ballsX < cornerBottomLeftX && ballsY > cornerBottomLeftY) {
				ballOffsetPosition.setCoors((int) (leftX + ((cornerBottomLeftX - leftX) * 0.5)), cornerBottomLeftY - 30);
				// Top strip (extreme)
			} else if (ballsY <= strip1) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY + 30);
				extremeStrip = true;
				// Top strip (normal)
			} else if (ballsY <= strip2) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY - 20);
				// Mid-Upper Strip
			} else if (ballsY <= strip3) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY - 10);
				// Middle Strip
			} else if (ballsY <= strip4) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY);
				// Mid-Lower Strip
			} else if (ballsY <= strip5) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY + 10);
				// Bottom strip (normal)
			} else if (ballsY <= strip6) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY + 20);
				// Bottom strip (extreme)
			} else if (ballsY > strip6) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY - 30);
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
			otherRobot
			.setCoors(new Position(state.getBlueX(), state.getBlueY()));

		} else {
			nxt.setAngle(state.getBlueOrientation());
			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));

			otherRobot.setAngle(state.getYellowOrientation());
			otherRobot.setCoors(new Position(state.getYellowX(), state
					.getYellowY()));

		}
		gui.setCoordinateLabels(nxt.getCoors(), otherRobot.getCoors(),
				ball.getCoors());

		if (findPath) {
			test = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(),
					otherRobot.getCoors());

			for (int s = 0; s < test.size(); s++) {
				int distBetweenWaypoint = Move.getDist(nxt, test.get(s));
				if (distBetweenWaypoint < 40)
					test.remove(s);
			}
			bla.setCoors(test.get(0).getX(), test.get(0).getY());
			gotoBall.setCoors(test.get(0).getX(), test.get(0).getY());
		}
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

	public void setCurrentCamera(int camera) {
		currentCamera = camera;
	}

	public void setIsMainPitch(boolean pitch) {
		isMainPitch = pitch;
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

	void arc() {
		getPitchInfo(false);
		double dist = Move.getDist(nxt, ball.getCoors());
		double h = 50;

		int radius = (int) ((4*Math.pow(h, 2) + Math.pow(dist, 2)) / 8*h);

		nxt.travelArcRobot(radius, (int) dist);



	}

	public void chaseBall(Robot nxt, Position intersection) {

		//Test and delete in needed===
		Position intersectionReal = new Position(intersection.getX(), intersection.getY());
		//=========================



		int dist11 = Move.getDist(nxt, intersectionReal);

		while(dist11 > 30) { // dist in pixels
			//System.out.println("intersection point: " + intersectionReal.getX() + " " + intersectionReal.getY());           





			getPitchInfo(false);
			dist11 = Move.getDist(nxt, intersectionReal);
			intersection.setX(intersectionReal.getX());
			intersection.setY(intersectionReal.getY());
			int n = Move.getAngleToPosition(nxt, intersection);

			//                System.out.println("SPEEDLEFT " + left_wheel_speed);
			//                System.out.println("SPEEDRIGHT " + right_wheel_speed);

			getPitchInfo(false);


			nxt.each_wheel_speed(500, 500);
			try {
				Thread.sleep(280);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(n >= 20 && n < 130) {
				while(n >= 20 && n < 40) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(300, 500);
					try {
						Thread.sleep(280);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= 40 && n < 60) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(270,500);
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= 60 && n < 80) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(240, 500);
					try {
						Thread.sleep(220);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= 80 && n < 100) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(210, 500);
					try {
						Thread.sleep(190);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= 100 && n < 130) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(190, 500);
					try {
						Thread.sleep(160);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				nxt.each_wheel_speed(500, 500);
				try {
					Thread.sleep(280);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if(n <= -20 && n > -130) {
				while(n >= -40 && n < -20) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(500, 300);
					try {
						Thread.sleep(280);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= -60 && n < -40) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(500,270);
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= -80 && n < -60) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(500, 240);
					try {
						Thread.sleep(220);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= -100 && n < -80) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(500, 210);
					try {
						Thread.sleep(190);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				while(n >= -130 && n < -100) {
					getPitchInfo(false);
					n = Move.getAngleToPosition(nxt, intersection);
					nxt.each_wheel_speed(500, 190);
					try {
						Thread.sleep(160);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				nxt.each_wheel_speed(500, 500);

			} 
			if(n >= 160  || n < -160) {
				getPitchInfo(false);
				nxt.stop();
				nxt.rotateRobot(90);
				try {
					Thread.sleep(280);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}


			if(n > -20 && n <20  ) {
				getPitchInfo(false);
				n = Move.getAngleToPosition(nxt, intersection);
				// Comment this out for first two 
				if(Move.getDist(nxt, intersection) < 25) {
					nxt.kick();
				}
				// -----
				continue;

			}
			getPitchInfo(false);
			if((n <= 160 && n >= 130) || n >= -160 && n <= -130) {
				nxt.each_wheel_speed(-500, -500);

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		nxt.each_wheel_speed(0,0);
	}

	public boolean getAttackLeft() {
		return attackLeft;
	}

}