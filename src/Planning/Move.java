package Planning;

import JavaVision.*;

public class Move {

	// Suppress default constructor for noninstantiability
	private Move() {
		throw new AssertionError();
	}

	// returns the distance between the robot and the ball
	public static int getDist(Robot robot, Position p) {
		
		int robotX = robot.getCoors().getX();
		int robotY = robot.getCoors().getY();
		int ballX = p.getX();
		int ballY = p.getY();

		int x = (int) Math.abs(robotX - ballX);
		int y = (int) Math.abs(robotY - ballY);

		// int x = (int) Math.abs(robot.getCoors().getX() -
		// ball.getCoors().getX());
		// int y = (int) Math.abs(robot.getCoors().getY() -
		// ball.getCoors().getY());
		int dist = (int) Math.sqrt(x * x + y * y);
		System.out.println("Coordinates received in getDist: robot "
				+ robot.getCoors().getX() + " " + robot.getCoors().getY()
				+ " and Ball: " + p.getX() + " " + p.getY() + " dist " + dist);
		return dist;
	}

	// rotate coordinate system to get the position of the ball with
	// respect to the robot
	public static Position modifyPositionCoors(Robot robot, Position pos) {

		Position p;
		p = translatePoint(robot, pos); // ball relative to robot

		// rotate counter-clockwise
		// coordinates of the ball with respect to the orientation of the robot
		int x = p.getX();
		int y = p.getY();
		float angle = robot.getAngle(); // in radians
		pos.setCoors(((int) (Math.cos(angle) * x - Math.sin(angle) * y)),
				((int) (Math.sin(angle) * x + Math.cos(angle) * y)));
		return pos;
	}

	public static Position translatePoint(Robot robot, Position p) {

		int x = p.getX() - robot.getCoors().getX();
		int y = -(p.getY()) - (-(robot.getCoors().getY())); // negative y to
															// convert into
															// normal cordinate
															// system
		p.setCoors(x, y);
		return p;
	}

	public static int getBallPosition(Position ball) {
		int POS;

		// find the position of the ball with respect to the robot
		if (ball.getX() > 0) {// ball is on the right side of the robot
			if (ball.getY() > 0) { // ball is in top right square
				POS = 0; // top right
			} else {
				POS = 1;
			} // bottom right
		} else { // ball is on the left side of the robot
			if (ball.getY() < 0) {
				POS = 2; // bottom left
			} else {
				POS = 3;
			} // top left
		}

		return POS;
	}

	// returns the angle from the robot to the ball
	public static int getAngleToPosition(Robot robot, Position p) {

		int dist = getDist(robot, p);
		Position newPos = new Position(0, 0);
		newPos = modifyPositionCoors(robot, p); // ball with the new coordinates
		// System.out.println("Ball coors" + ball.getCoors().getX() + " " +
		// ball.getCoors().getY());
		float angle = 0;
		int ballPos = getBallPosition(newPos);
		// System.out.println("POS " + ballPos);
		switch (ballPos) {
		case 0: {
			double a = ((double) newPos.getX()) / ((double) dist);
			a = (a > 0) ? a : -a;
			// System.out.println("Bx : " + ball.getCoors().getX());
			// System.out.println("Dist : " + dist);
			// System.out.println("A is : " + a);
			angle = (float) -Math.asin(a);
			// System.out.println("Aangle in radians : " + angle);
			// System.out.println("Aangle in degrees : " +
			// Math.toDegrees(angle));
			break; // turn 'angle' radians right
		}
		case 1: {
			double a = ((double) newPos.getY()) / ((double) dist);
			a = (a > 0) ? a : -a;
			// System.out.println("Bx : " + ball.getCoors().getX());
			// System.out.println("Dist : " + dist);
			// System.out.println("A is : " + a);
			angle = (float) -(Math.asin(a) + Math.PI / 2);
			// System.out.println("Aangle in radians : " + angle);
			// System.out.println("Aangle in degrees : " +
			// Math.toDegrees(angle));
			break;
			// turn 'angle' radians right
		}
		case 2: {
			double a = ((double) newPos.getY()) / ((double) dist);
			a = (a > 0) ? a : -a;
			// System.out.println("Bx : " + ball.getCoors().getX());
			// System.out.println("Dist : " + dist);
			// System.out.println("A is : " + a);
			angle = (float) (Math.asin(a) + Math.PI / 2);
			// System.out.println("Aangle in radians : " + angle);
			// System.out.println("Aangle in degrees : " +
			// Math.toDegrees(angle));
			break;
			// turn 'angle' radians left
		}
		case 3: {
			double a = ((double) newPos.getX()) / ((double) dist);
			a = (a > 0) ? a : -a;
			// System.out.println("Bx : " + ball.getCoors().getX());
			// System.out.println("Dist : " + dist);
			// System.out.println("A is : " + a);
			angle = (float) Math.asin(a);
			// System.out.println("Aangle in radians : " + angle);
			// System.out.println("Aangle in degrees : " +
			// Math.toDegrees(angle));
			break; // turn 'angle' radians left
		}
		}
		return (int) Math.toDegrees(angle);

	}

}
