package Planning;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import JavaVision.Position;
import JavaVision.WorldState;

public class Planning extends Thread {
	// Declare fields
	private AtomicBoolean stopFlag = new AtomicBoolean();
	private Robot nxt;
	private Robot otherRobot;
	private WorldState state;
	private boolean teamYellow;
	private boolean attackLeft;
	private boolean isPenaltyAttack;
	private boolean isPenaltyDefend;
	private FieldPositions fieldPositions;
	private Position ourGoal;
	private Position theirGoal;	
	public static Ball ball = new Ball();
	public PathPlanner planner;
	Runner instance;
	public Position bla = new Position(0, 0);
	public Position gotoBall = new Position(0, 0);
	public ArrayList<Position> test = new ArrayList<Position>();
	Position ballOffsetPosition = new Position(0,0);

	// used for move_forward method
	public static final int DEFAULT_SPEED = 10; 

	// used for each_wheel_speed
	public static final int EACH_WHEEL_SPEED = 900; 

	// game flags
	public boolean isScore = false;
	public boolean extremeStrip = false;

	private Planning() {}

	public static Builder createBuilder() {
		return new Builder();
	}

	/*
	 * Builder for Planning
	 */ 
	public static class Builder {
		private final Planning obj = new Planning();
		private boolean done;

		private Builder() {}

		// Return the planning object
		public Planning build() {
			done = true;
			return obj;
		}

		public Builder setRunnerInstance(Runner instance) {
			check();
			obj.instance = instance;
			return this;
		}

		public Builder setRobot(Robot nxt) {
			check();
			obj.nxt = nxt;
			return this;
		}

		public Builder setOtherRobot(Robot otherRobot) {
			check();
			obj.otherRobot = otherRobot;
			return this;
		}

		public Builder setState(WorldState state) {
			check();
			obj.state = state;
			return this;
		}

		public Builder setTeamYellow(boolean teamYellow) {
			check();
			obj.teamYellow = teamYellow;
			return this;
		}

		public Builder setAttackLeft(boolean attackLeft) {
			check();
			obj.attackLeft = attackLeft;
			return this;
		}

		public Builder setPenaltyAttack(boolean isPenaltyAttack) {
			check();
			obj.isPenaltyAttack = isPenaltyAttack;
			return this;
		}

		public Builder setPenaltyDefend(boolean isPenaltyDefend) {
			check();
			obj.isPenaltyDefend = isPenaltyDefend;
			return this;
		}

		public Builder setFieldPositions(FieldPositions fieldPositions) {
			check();
			obj.fieldPositions = fieldPositions;
			return this;
		}

		private void check() {
			if (done)
				throw new IllegalArgumentException("Do use other builder to create new instance");
		}
	}

	public void run() {

		modeStart();
		//		planner = new PathPlanner(attackLeft);
		//
		//		while (!stopFlag.get()) {
		//			if (isPenaltyAttack) {
		//				penaltyAttack();
		//			} else if (isPenaltyDefend) {
		//				try {
		//					penaltyDefend();
		//				} catch (InterruptedException e) {
		//					e.printStackTrace();
		//				}
		//			} else {
		//				mainLoop();
		//			}
		//		}
		//
		//		// Planning finished: Stop robot
		//		nxt.stop();
	}	

	/**
	 * Main Loop - Loop for all other modes
	 */
	public void mainLoop() {

		Point2D.Double target;
		if(attackLeft) {
			target = new Point2D.Double(40, -250);
		} else {
			target = new Point2D.Double(608, -242);
		}
		Arc arc = new Arc();

		double robotOrientation;

		while(!stopFlag.get()) {
			System.out.println(stopFlag.get());
			getPitchInfo(false);

			Point2D.Double robotPoint2d = new Point2D.Double(nxt.getCoors().getX(), nxt.getCoors().getY() * -1);
			Point2D.Double ballPoint2d = new Point2D.Double(ball.getCoors().getX(), ball.getCoors().getY() * -1);

			if(teamYellow) {
				robotOrientation = state.getYellowOrientation();
			} else {
				robotOrientation = state.getBlueOrientation();
			}

			int arcState = arc.calculateArc(robotPoint2d, ballPoint2d, target, robotOrientation);
			//			System.out.println("yellow orietation " + state.getYellowOrientation());
			//			System.out.println("blue orietation " + state.getBlueOrientation());
			//			
			//			System.out.println("robot " + robotPoint2d.getX() + " " + robotPoint2d.getY());
			//			System.out.println("ball " + ballPoint2d.getX()+ " " + ballPoint2d.getY());
			//			System.out.println("target "+ target.getX() + " " + target.getY());
			//			
			//			System.out.println();
			//			
			//			System.out.println("goal " +  fieldPositions.ourGoal.getX() + " " + fieldPositions.ourGoal.getY());

			Runner.vision.drawLine(new Point2D.Double(ballPoint2d.x, ballPoint2d.y*-1), new Point2D.Double(target.x, target.y*-1), Color.ORANGE);
			Runner.vision.drawLine(new Point2D.Double(arc.intersectionPoint.x, arc.intersectionPoint.y*-1), new Point2D.Double(robotPoint2d.x, robotPoint2d.y*-1), Color.GREEN);

			Runner.vision.drawPosition(new Position((int) ballPoint2d.x, (int) ballPoint2d.y*-1), Color.BLACK);
			Runner.vision.drawPosition(new Position((int) target.x, (int) target.y*-1), Color.RED);

			switch (arcState) {
			case(Arc.MOTION_ROTATE):
				double rotation = arc.getRotation();
			System.out.println("rotate: " + rotation);
			rotation = Math.toDegrees(rotation);
			System.out.println("MOTION ROTATE\t" + rotation);
			nxt.rotateRobotInteruptible((int) rotation);
			break;
			case(Arc.MOTION_FORWARD):
				double distance = arc.getDistance();
			System.out.println("travel: " + distance);
			System.out.println("MOTION FORWARD\t" + distance);
			nxt.moveForward(DEFAULT_SPEED);	
			break;
			case(Arc.MOTION_ARC):
				double arcRadius = arc.getArcRadius();
			double arcAngle = arc.getArcAngle();
			System.out.println("arc: " + arcRadius + ", " + arcAngle);
			int arcLength = (int) (arcRadius * arcAngle);
			System.out.println("MOTION ARC\tRadius" + arcRadius  + "\t" + "angle " + arcAngle);
			arcRadius = Move.PixelToCM(arcRadius);
			arcLength = (int) Math.abs(Move.PixelToCM(arcLength));
			System.out.println("MOTION ARC\tRadius" + arcRadius  + "\t" + "angle " + arcAngle);

			nxt.travelArcRobot((int) arcRadius, arcLength);
			break;
			}
		}
	}

	/**
	 * Mode Start - Used at beginning of game
	 */
	public void modeStart() {

		getPitchInfo(false);
		int distanceToBall = Move.getDist(nxt, ball.getCoors());
		int angleToBall = Move.getAngleToPosition(nxt, ball.getCoors());
		nxt.each_wheel_speed(300, 300);
		while(Move.PixelToCM(distanceToBall) > 36) {
			getPitchInfo(false);
			distanceToBall = Move.getDist(nxt, ball.getCoors());
			angleToBall = Move.getAngleToPosition(nxt, ball.getCoors());
			System.out.println(angleToBall);
//			while(Move.getAngleToPosition(nxt, ball.getCoors()) > 0 || Move.getAngleToPosition(nxt, ball.getCoors()) < 0) {
//				getPitchInfo(false);
//				System.out.println("GO LEFT");
//				nxt.each_wheel_speed(500, 200);
//			}
		}
		nxt.kick();
		nxt.stop();
//		getPitchInfo(false);
//		angleToBall = Move.getAngleToPosition(nxt, ball.getCoors());
//		nxt.rotateRobot(angleToBall);
//		nxt.moveForward(25);
//		
		

				
				//			} else if(angleToBall < -8) {
//				getPitchInfo(false);
//				getPitchInfo(false);
//				System.out.println("GO RIGHT");
//				nxt.each_wheel_speed(200, 500);
//			}			








		/*		System.out.println("MODE START");
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
					if (!stopFlag.get()) {
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
				onWall.setCoors(onWallX, fieldPositions.topY);

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
					if (!stopFlag.get()) {
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
				onWall.setCoors(onWallX, fieldPositions.lowY);

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
					if (!stopFlag.get()) {
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
				onWall.setCoors(onWallX, fieldPositions.topY);

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
					if (!stopFlag.get()) {
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
				onWall.setCoors(onWallX, fieldPositions.lowY);

				nxt.rotateRobot(Move.getAngleToPosition(nxt, onWall));
				nxt.kick();
				nxt.stop();
			}
		}*/
	}

	public void chaseBall() {
		getPitchInfo(false);
		ArrayList<Position> pathchase = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(), otherRobot.getCoors());

		for(int i = 0 ; i<pathchase.size(); i++){
			Position pointchase = pathchase.get(i);
			int distchase = Move.getDist(nxt, pointchase);
			int anglechase = Move.getAngleToPosition(nxt, pointchase);
			System.out.println(distchase);
			nxt.rotateRobot(anglechase);
			while(distchase>20){
				getPitchInfo(false);
				distchase = Move.getDist(nxt, pointchase);
				anglechase = Move.getAngleToPosition(nxt, pointchase);
				while(anglechase>10&&distchase>50){
					getPitchInfo(false);
					distchase = Move.getDist(nxt, pointchase);
					anglechase = Move.getAngleToPosition(nxt, pointchase);
					nxt.each_wheel_speed(250, 500);

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				while(anglechase<-10&&distchase>50){
					getPitchInfo(false);
					distchase = Move.getDist(nxt, pointchase);
					anglechase = Move.getAngleToPosition(nxt, pointchase);
					nxt.each_wheel_speed(500, 250);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println(distchase);
				nxt.each_wheel_speed(500, 500);
			}
			nxt.stop();
		}
	}

	public void penaltyDefend() throws InterruptedException {
		getPitchInfo(false);
		System.out.println("Other Robot Initial Angle " + Math.toDegrees(otherRobot.angle));
		Position ballInitial = new Position(ball.getCoors().getX(), ball
				.getCoors().getY());
		int dist = 0;
		while (!stopFlag.get()) {
			getPitchInfo(false);

			System.out.println("Other Robot Angle " + Math.toDegrees(otherRobot.angle));
			dist = (int) Position.sqrdEuclidDist(ball.getCoors().getX(),
					ball.getCoors().getY(), ballInitial.getX(),
					ballInitial.getY());

			// when attack left and defending right goal
			//moves based on where the other robot is facing
			if (attackLeft) {
				if (otherRobot.angle >= 85 && otherRobot.angle <= 95) {
					if (dist > 15) {
						chaseBall();
					}
				}
				else if (otherRobot.angle > 95) {
					nxt.moveBackward(DEFAULT_SPEED);
					Thread.sleep(1000);
					nxt.stop();
					if (dist > 15) {
						chaseBall();
					}
				}
				else if (otherRobot.angle < 85 ){
					nxt.moveForward(DEFAULT_SPEED);
					Thread.sleep(1000);
					nxt.stop();
					if (dist > 15) {
						chaseBall();
					}
				}
			}

			//when attacking right and defending left goal
			//moves based on where the other robot is facing
			else {
				if (otherRobot.angle >= 265 && otherRobot.angle <= 275) {
					if (dist > 15) {
						chaseBall();
					}
				}
				else if (otherRobot.angle < 265) {
					nxt.moveBackward(DEFAULT_SPEED);
					Thread.sleep(1000);
					nxt.stop();
					if (dist > 15) {
						chaseBall();
					}
				}
				else if (otherRobot.angle > 275){
					nxt.moveForward(DEFAULT_SPEED);
					Thread.sleep(1000);
					nxt.stop();
					if (dist > 15) {
						chaseBall();
					}
				}
			}
		}
	}

	public void penaltyAttack() {
		System.out.println("Penalty Attack Mode");
		double weird = Math.random();
		int angle;

		if (weird > 0.5) {
			angle = -12;
		} else {
			angle = 12;
		}
		System.out.println("Angle for penalty: " + angle);
		nxt.rotateRobot(angle);
		nxt.kick();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Finished Kicking!!");
		chaseBall();
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
		state = instance.getWorldState();

		// Get pitch information from vision
		state = Runner.vision.getWorldState();

		// while not receiving correct info from Vision
		while (state.getBlueX() == 0) {
			state = Runner.vision.getWorldState();
		}

		ball.setCoors(new Position(state.getBallX(), state.getBallY()));
		gotoBall.setCoors(bla.getX(), bla.getY());
		extremeStrip = false;

		// set the coordinates of offset ball based on location of ball
		int ballsX = ball.getCoors().getX();
		int ballsY = ball.getCoors().getY();
		//if (isMainPitch) {
		if (attackLeft) {
			// Upper Right Box
			if (ballsX > fieldPositions.cornerTopRightX && ballsY < fieldPositions.cornerTopRightY) {
				ballOffsetPosition.setCoors((int) (fieldPositions.cornerTopRightX + ((fieldPositions.rightX - fieldPositions.cornerTopRightX) * 0.5)), fieldPositions.cornerTopRightY + 30);
				// Lower Right Box
			} else if (ballsX > fieldPositions.cornerBottomRightX && ballsY > fieldPositions.cornerBottomRightY) {
				ballOffsetPosition.setCoors((int) (fieldPositions.cornerBottomRightX + ((fieldPositions.rightX - fieldPositions.cornerBottomRightX) * 0.5)), fieldPositions.cornerBottomRightY - 30);
				// Top strip (extreme)
			} else if (ballsY <= fieldPositions.strip1) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY + 30);
				extremeStrip = true;
				// Top strip (normal)
			} else if (ballsY <= fieldPositions.strip2) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY - 20);
				// Mid-Upper Strip
			} else if (ballsY <= fieldPositions.strip3) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY - 10);
				// Middle Strip
			} else if (ballsY <= fieldPositions.strip4) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY);
				// Mid-Lower Strip
			} else if (ballsY <= fieldPositions.strip5) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY + 10);
				// Bottom strip (normal)
			} else if (ballsY <= fieldPositions.strip6) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY + 20);
				// Bottom strip (extreme)
			} else if (ballsY > fieldPositions.strip6) {
				ballOffsetPosition.setCoors(ballsX + 50, ballsY - 30);
			}
		} else if (!attackLeft) {
			// Upper left box
			if (ballsX < fieldPositions.cornerTopLeftX && ballsY < fieldPositions.cornerTopLeftY) {
				ballOffsetPosition.setCoors((int) (fieldPositions.leftX + ((fieldPositions.cornerTopLeftX - fieldPositions.leftX) * 0.5)), fieldPositions.cornerTopLeftY + 30);
				// Lower left box
			} else if (ballsX < fieldPositions.cornerBottomLeftX && ballsY > fieldPositions.cornerBottomLeftY) {
				ballOffsetPosition.setCoors((int) (fieldPositions.leftX + ((fieldPositions.cornerBottomLeftX - fieldPositions.leftX) * 0.5)), fieldPositions.cornerBottomLeftY - 30);
				// Top strip (extreme)
			} else if (ballsY <= fieldPositions.strip1) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY + 30);
				extremeStrip = true;
				// Top strip (normal)
			} else if (ballsY <= fieldPositions.strip2) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY - 20);
				// Mid-Upper Strip
			} else if (ballsY <= fieldPositions.strip3) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY - 10);
				// Middle Strip
			} else if (ballsY <= fieldPositions.strip4) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY);
				// Mid-Lower Strip
			} else if (ballsY <= fieldPositions.strip5) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY + 10);
				// Bottom strip (normal)
			} else if (ballsY <= fieldPositions.strip6) {
				ballOffsetPosition.setCoors(ballsX - 50, ballsY + 20);
				// Bottom strip (extreme)
			} else if (ballsY > fieldPositions.strip6) {
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
			otherRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));

		} else {
			nxt.setAngle(state.getBlueOrientation());
			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));

			otherRobot.setAngle(state.getYellowOrientation());
			otherRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
		}

		instance.updateGuiLabels(nxt.getCoors(), otherRobot.getCoors(), ball.getCoors());

		if (findPath) {
			test = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(),
					otherRobot.getCoors());

			bla.setCoors(test.get(0).getX(), test.get(0).getY());
			gotoBall.setCoors(test.get(0).getX(), test.get(0).getY());
		}
	}

	public void setStopFlag(boolean flag) {
		stopFlag.set(flag);
	}
}
