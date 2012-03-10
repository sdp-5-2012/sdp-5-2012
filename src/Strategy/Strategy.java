package Strategy;

import JavaVision.*;
import Planning.*;

public class Strategy {

	/*
	 * Modes:
	 * 0 - default "go to ball, aim, kick"
	 * 1 - kick wildly (at an angle - off the wall?)
	 * 2 - dribble towards enemy half
	 * 3 - retreat to own goal, defend
	 * 4 - attack hard
	 * OBSOLETE 5 - default "go to ball, aim, kick" but go to the ball in an arc because the other robot's in the way!
	 *
	 */

	public int whatToDo(Robot ourRobot, Robot theirRobot, Ball ball, Position ourGoal, Position theirGoal, Position pitchCentre) {

		int mode = 0;
		
		if(!(ball.getCoors().getX()==0) && !(ball.getCoors().getY()==0)){

		if (doWeHaveTheBall(ourRobot, ball.getCoors())) {
			if (areWeInOurHalf(ourRobot, ourGoal, theirGoal)) {
				if (areTheyInOurHalf(theirRobot, ourGoal, theirGoal)) {

					// mode 1 - kick wildly! (planning should work out an optimal angle)
					mode = 1;

					// else they're in their half
				} else {

					// mode 2 - dribble the ball towards (and into) their half
					mode = 2;
				}

				// else we're in their half
			} else {
				if (goalObstructed(ourRobot, theirRobot, theirGoal)) {
					// planning will work out an angle to bounce the ball off the wall
					mode = 1;
					// else it's UNOBSTRUCTED - Runner's main loop will just aim/shoot as normal
				}
			}

		} else if (doTheyHaveTheBall(theirRobot, ball.getCoors())) {
			if (areTheyInOurHalf(theirRobot, ourGoal, theirGoal)) {
				mode = 3;
			} else {
				if (!areWeInOurHalf(ourRobot, ourGoal, theirGoal)) {
					mode = 4;
				}
			}
		}

		/* POSSIBLY OBSOLETE DUE TO PATHFINDING ALGORITHM AVOIDING OTHER ROBOT ANYWAY
if (ballObstructed(ourRobot, theirRobot, ball)) {
mode = 5;
}
		 */
		
		} else {
			mode = 6;
		}

		return mode;

	}

	// Returns true if our robot has possession of the ball
	public boolean doWeHaveTheBall(Robot ourRobot, Position ballPos) {
		boolean ourBall = Move.getDist(ourRobot, ballPos) < 50;

		return ourBall;
	}

	// Returns true if their robot has possession of the ball
	public boolean doTheyHaveTheBall(Robot theirRobot, Position ballPos) {
		boolean theirBall = Move.getDist(theirRobot, ballPos) < 50;

		return theirBall;
	}

	// Returns true if we are closer to our own goal than the opposition's
	public boolean areWeInOurHalf(Robot ourRobot, Position ourGoal, Position theirGoal) {
		boolean usCloserToOurGoal = Position.sqrdEuclidDist(ourRobot.getCoors().getX(), ourRobot.getCoors().getY(), ourGoal.getX(), ourGoal.getY()) <
		Position.sqrdEuclidDist(ourRobot.getCoors().getX(), ourRobot.getCoors().getY(), theirGoal.getX(), theirGoal.getY());

		return usCloserToOurGoal;
	}

	// Returns true if they are closer to our goal than theirs
	public boolean areTheyInOurHalf(Robot theirRobot, Position ourGoal, Position theirGoal) {
		boolean themCloserToOurGoal = Position.sqrdEuclidDist(theirRobot.getCoors().getX(), theirRobot.getCoors().getY(), ourGoal.getX(), ourGoal.getY()) <
		Position.sqrdEuclidDist(theirRobot.getCoors().getX(), theirRobot.getCoors().getY(), theirGoal.getX(), theirGoal.getY());

		return themCloserToOurGoal;
	}

	// Returns true if there is a robot between us, and the goal, in a straight line
	public boolean goalObstructed(Robot ourRobot, Robot theirRobot, Position theirGoal) {
		boolean obstruction = false;
		double slopeThemGoal = 0;
		double slopeUsGoal = 0;

		// if they're behind us then nothing will happen - make sure they are at least closer to the goal we're shooting towards
		if (Position.sqrdEuclidDist(ourRobot.getCoors().getX(), ourRobot.getCoors().getY(), theirGoal.getX(), theirGoal.getY()) >
		Position.sqrdEuclidDist(theirRobot.getCoors().getX(), theirRobot.getCoors().getY(), theirGoal.getX(), theirGoal.getY())) {

			slopeThemGoal = (double) ((theirGoal.getY() - theirRobot.getCoors().getY()) / (theirGoal.getX() - theirRobot.getCoors().getX()));
			slopeUsGoal = (double) ((theirGoal.getY() - ourRobot.getCoors().getY()) / (theirGoal.getX() - ourRobot.getCoors().getX()));

			double theirAngle = Math.atan(slopeThemGoal);
			double ourAngle = Math.atan(slopeUsGoal);

			double difference = Math.abs(theirAngle - ourAngle);

			if (difference < 0.27) {
				obstruction = true;
			}

		}

		return obstruction;
	}

	public boolean ballObstructed(Robot ourRobot, Robot theirRobot, Ball ball) {
		boolean obstruction = false;
		double slopeThemBall = 0;
		double slopeUsBall = 0;

		// if they're behind us then nothing will happen - make sure they are at least closer to the ball than we are
		if (Position.sqrdEuclidDist(ourRobot.getCoors().getX(), ourRobot.getCoors().getY(), ball.getCoors().getX(), ball.getCoors().getY()) >
		Position.sqrdEuclidDist(theirRobot.getCoors().getX(), theirRobot.getCoors().getY(), ball.getCoors().getX(), ball.getCoors().getY())) {


			slopeThemBall = (double) ((ball.getCoors().getY() - theirRobot.getCoors().getY()) / (ball.getCoors().getX() - theirRobot.getCoors().getX()));
			slopeUsBall = (double) ((ball.getCoors().getY() - ourRobot.getCoors().getY()) / (ball.getCoors().getX() - ourRobot.getCoors().getX()));

			double theirAngle = Math.atan(slopeThemBall);
			double ourAngle = Math.atan(slopeUsBall);

			double difference = Math.abs(theirAngle - ourAngle);

			if (difference < 0.27) {
				obstruction = true;
			}


		}

		return obstruction;

	}

}