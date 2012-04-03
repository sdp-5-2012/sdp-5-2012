package Planning;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
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
	public ArrayList<Position> waypoints = new ArrayList<Position>();
	Position ballOffsetPosition = new Position(0,0);
	/*
	 * For arcing and scoring (ONLY USED ONCE)
	 */
	Arc arc = new Arc();
	Defend defend = new Defend();
	Point2D.Double target;
	Point2D.Double goalUpper;
	Point2D.Double goalLower;
	Point2D.Double robotPoint2d;
	Point2D.Double ballPoint2d;

	// used for move_forward method
	public static final int DEFAULT_SPEED = 35; 

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
		
//		modeStart();
		mainLoop();
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
////		// Planning finished: Stop robot
//		nxt.stop();
	}	

	/**
	 * Main Loop - Loop for all other modes
	 */
	public void mainLoop() {

		System.out.println(attackLeft);
		if(attackLeft) {
			target = new Point2D.Double(40, -250);
			goalUpper = new Point2D.Double(40, -200);
			goalLower = new Point2D.Double(40, -300);
		} else {
			target = new Point2D.Double(608, -242);
			goalUpper = new Point2D.Double(608, -200);
			goalLower = new Point2D.Double(608, -300);

		}


		double robotOrientation;

		while(!stopFlag.get()) {
			System.out.println(stopFlag.get());
			getPitchInfo(false);



			robotPoint2d = new Point2D.Double(nxt.getCoors().getX(), nxt.getCoors().getY() * -1);
			ballPoint2d = new Point2D.Double(ball.getCoors().getX(), ball.getCoors().getY() * -1);

			if(teamYellow) {
				robotOrientation = state.getYellowOrientation();
			} else {
				robotOrientation = state.getBlueOrientation();
			}

			/*
			 * if our robot is facing the goal, then go forward
			 */
			if (defend.isFacingGoal(robotPoint2d, ballPoint2d, goalUpper, goalLower, robotOrientation)) {
				
				if (Math.sqrt(Math.pow(robotPoint2d.x - ballPoint2d.x,2) + Math.pow(robotPoint2d.y-ballPoint2d.y, 2) ) <= 50 ) {
					System.out.println("KICK!");
					nxt.kick();
				} else {
					System.out.println("CHARGE!");
					nxt.moveForward(DEFAULT_SPEED);
				}
				continue;
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

			//Runner.vision.drawPosition(new Position((int) arc.orthogonalIntersectionPoint.x, (int) arc.orthogonalIntersectionPoint.y), Color.MAGENTA);
			Runner.vision.drawPosition(new Position((int) ballPoint2d.x, (int) ballPoint2d.y*-1), Color.BLACK);
			Runner.vision.drawPosition(new Position((int) target.x, (int) target.y*-1), Color.RED);

			switch (arcState) {
			case(Arc.MOTION_ROTATE):
				double rotation = arc.getRotation();
				System.out.println("rotate: " + rotation);
				rotation = Math.toDegrees(rotation);
				System.out.println("MOTION ROTATE\t" + rotation);
				nxt.rotateRobot((int) rotation);
				//nxt.rotateRobotInteruptible((int) rotation);
				System.out.println("working2");
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
				System.out.println("LENGTH OF ARC: " + arcLength);
	
				nxt.travelArcRobot((int) arcRadius, arcLength);
				break;

			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Mode Start - Used at beginning of game
	 */
	public void modeStart() {

		long startTime = System.currentTimeMillis();		

		getPitchInfo(false);
		int distanceToBall = Move.getDist(nxt, ball.getCoors());
		int angleToBall = Move.getAngleToPosition(nxt, ball.getCoors());
		nxt.each_wheel_speed(700, 700);
		while(Move.PixelToCM(distanceToBall) > 25 && System.currentTimeMillis() - startTime < 5000) {
			getPitchInfo(false);
			distanceToBall = Move.getDist(nxt, ball.getCoors());
			angleToBall = Move.getAngleToPosition(nxt, ball.getCoors());
			//			
			//			if(angleToBall > 8) {
			//				getPitchInfo(false);
			//				System.out.println("GO LEFT");
			//				nxt.each_wheel_speed(850, 900);
			//			}else if(angleToBall < -8) {
			//				getPitchInfo(false);
			//				System.out.println("GO RIGHT");
			//				nxt.each_wheel_speed(900, 800);
			//			}

		}
		nxt.kick();
		nxt.stop();

	}

	public void chaseBall() {
		getPitchInfo(true);
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

	public void chaseBallAstar(){ // to be tested and adjusted uses a star algorithm
		while(!stopFlag.get()){
			Runner.vision.drawPath(waypoints);
			getPitchInfo(true);
			System.out.println(waypoints.size());
			Position chasingPosition = waypoints.get(0);
			int angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
			int distChasing = Move.getDist(nxt, chasingPosition);

			if(distChasing<60 && waypoints.size() > 1){
				chasingPosition = waypoints.get(1);
			}
			angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
			distChasing = Move.getDist(nxt, chasingPosition);
			while(distChasing>40 && !stopFlag.get()){
				Runner.vision.drawPath(waypoints);

				getPitchInfo(false);
				angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
				distChasing = Move.getDist(nxt, chasingPosition);

				if(distChasing<60 && waypoints.size() > 1){
					chasingPosition = waypoints.get(1);
				}
				
				angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
				distChasing = Move.getDist(nxt, chasingPosition);


				nxt.rotateRobot(angleChasing);


				if(angleChasing>-10 &&angleChasing<10){
					while(distChasing>40&&angleChasing>-10 &&angleChasing<10 && !stopFlag.get()){
						Runner.vision.drawPath(waypoints);
						getPitchInfo(false);
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						if(distChasing<60 && waypoints.size() > 1){
							chasingPosition = waypoints.get(1);
						}
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						nxt.each_wheel_speed(600, 600);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				}


				//BALL ON THE LEFT
				if(angleChasing>-30 &&angleChasing<=-10){
					while(distChasing>40&&angleChasing>-30 &&angleChasing<=-10 && !stopFlag.get()){
						Runner.vision.drawPath(waypoints);
						getPitchInfo(false);
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						if(distChasing<60 && waypoints.size() > 1){
							chasingPosition = waypoints.get(1);
						}
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						nxt.each_wheel_speed(300, 500);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}


					}
				}

				if(angleChasing>-90 &&angleChasing<=-30){
					while(distChasing>40&&angleChasing>-90 &&angleChasing<=-30 && !stopFlag.get()){
						Runner.vision.drawPath(waypoints);
						getPitchInfo(false);
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						if(distChasing<60 && waypoints.size() > 1){
							chasingPosition = waypoints.get(1);
						}
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						nxt.each_wheel_speed(250, 500);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}


					}
				}

				if(angleChasing>-180 &&angleChasing<=-90){
					while(distChasing>40&&angleChasing>-180 &&angleChasing<=-90 && !stopFlag.get()){
						Runner.vision.drawPath(waypoints);
						getPitchInfo(false);
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						if(distChasing<60 && waypoints.size() > 1){
							chasingPosition = waypoints.get(1);
						}
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						nxt.each_wheel_speed(250, 500);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}


					}
				}

				//BALL ON THE RIGHT


				if(angleChasing>=10 &&angleChasing<30){
					while(distChasing>40&&angleChasing>=10 &&angleChasing<30 && !stopFlag.get()){
						Runner.vision.drawPath(waypoints);
						getPitchInfo(false);
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						if(distChasing<60 && waypoints.size() > 1){
							chasingPosition = waypoints.get(1);
						}
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						nxt.each_wheel_speed(500, 300);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}


					}
				}

				if(angleChasing>=30 &&angleChasing<90){
					while(distChasing>40&&angleChasing>=30 &&angleChasing<90 && !stopFlag.get()){
						Runner.vision.drawPath(waypoints);
						getPitchInfo(false);
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						if(distChasing<60 && waypoints.size() > 1){
							chasingPosition = waypoints.get(1);
						}
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						nxt.each_wheel_speed(500, 250);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}


					}
				}

				if(angleChasing>=90 &&angleChasing<180){
					while(distChasing>40&&angleChasing>=90 &&angleChasing<180 && !stopFlag.get()){
						Runner.vision.drawPath(waypoints);
						getPitchInfo(false);
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						if(distChasing<60 && waypoints.size() > 1){
							chasingPosition = waypoints.get(1);
						}
						angleChasing = Move.getAngleToPosition(nxt, chasingPosition);
						distChasing = Move.getDist(nxt, chasingPosition);

						nxt.each_wheel_speed(500,250);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}


					}
				}

			}
			getPitchInfo(false);
			int balls = Move.getDist(nxt, ball.getCoors());
			if(balls < 40) {
				nxt.kick();
			}
		}
	}
	
	private void penaltyDefend() throws InterruptedException {
		getPitchInfo(false);
		Position ballInitial = new Position(ball.getCoors().getX(), ball.getCoors().getY());
		int dist = 0;
		int difference;
		long time = System.currentTimeMillis();
		penaltyLoop:
			while(!stopFlag.get()) {
				while(!stopFlag.get() && System.currentTimeMillis() - time < 30000) {
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

//	public void penaltyDefend() throws InterruptedException {
//		getPitchInfo(false);
//		int otherRobotAngle = 0;
//		System.out.println("Other Robot Initial Angle " + Math.toDegrees(otherRobot.angle));
//		Position ballInitial = new Position(ball.getCoors().getX(), ball
//				.getCoors().getY());
//		int dist = 0;
//		while (!stopFlag.get()) {
//			getPitchInfo(false);
//			otherRobotAngle = (int) Math.toDegrees(otherRobot.angle);
//
//			System.out.println("Other Robot Angle " + Math.toDegrees(otherRobot.angle));
//			dist = (int) Position.sqrdEuclidDist(ball.getCoors().getX(),
//					ball.getCoors().getY(), ballInitial.getX(),
//					ballInitial.getY());
//
//			// when attack left and defending right goal
//			//moves based on where the other robot is facing
//			if (attackLeft) {
//				
//				if (otherRobotAngle >= 82 && otherRobotAngle <= 98) {
//					System.out.println("DO NOWT");
//					if (dist > 15) {
//						isPenaltyDefend = false;
//						chaseBall();
//					}
//				} else if (otherRobotAngle > 98) {
//					System.out.println("greater than 98");
//					nxt.moveBackward(DEFAULT_SPEED);
//					Thread.sleep(1000);
//					nxt.stop();
//					if (dist > 15) {
//						isPenaltyDefend = false;
//						chaseBall();
//					}
//				} else if (otherRobotAngle < 82 ){
//					System.out.println("less than 82");
//					nxt.moveForward(DEFAULT_SPEED);
//					Thread.sleep(1000);
//					nxt.stop();
//					if (dist > 15) {
//						isPenaltyDefend = false;
//						chaseBall();
//					}
//				}
//			}
//
//			//when attacking right and defending left goal
//			//moves based on where the other robot is facing
//			else {
//				if (otherRobotAngle >= 265 && otherRobotAngle <= 275) {
//					if (dist > 15) {
//						isPenaltyDefend = false;
//						chaseBall();
//					}
//				}
//				else if (otherRobotAngle < 265) {
//					nxt.moveBackward(DEFAULT_SPEED);
//					Thread.sleep(1000);
//					nxt.stop();
//					if (dist > 15) {
//						isPenaltyDefend = false;
//						chaseBall();
//					}
//				}
//				else if (otherRobotAngle > 275){
//					nxt.moveForward(DEFAULT_SPEED);
//					Thread.sleep(1000);
//					nxt.stop();
//					if (dist > 15) {
//						isPenaltyDefend = false;
//						chaseBall();
//					}
//				}
//			}
//		}
//	}

	public void penaltyAttack() {
		System.out.println("Penalty Attack Mode");
		double weird = Math.random();
		int angle;
		System.out.println("weird "  + weird);

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
		isPenaltyAttack = false;
//		chaseBall();
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

		if(findPath){
			waypoints = planner.getOptimalPath(nxt.getCoors(), ball.getCoors(), otherRobot.getCoors());
			System.out.println(waypoints.size() +" gpi");
		}
	}

	public void setStopFlag(boolean flag) {
		stopFlag.set(flag);
	}
}
