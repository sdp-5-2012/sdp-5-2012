package Strategy;

import JavaVision.*;
import Planning.*;

public class Strategy {

// NB CONSTANTS SUCH AS PITCH CENTRE/GOALS WILL HAVE TO BE AVAILABLE TO THIS CLASS
	
	Move move;
	
	/*
	 * Modes:
	 * 0 - default "go to ball, aim, kick"
	 * 1 - kick wildly (at an angle - off the wall?)
	 * 2 - dribble towards enemy half
	 * 3 - retreat to own goal, defend
	 * 4 - attack hard
	 * 
	 * 50 - penalty mode (shooting)			- triggered on planning GUI
	 * 60 - penalty mode (defending)		- triggered on planning GUI
	 *
	 */
	
	public int whatToDo(Robot ourRobot, Robot theirRobot, Ball ball, Position ourGoal, Position theirGoal, Position pitchCentre) {
		
		int mode = 0;
		
		if (doWeHaveTheBall(ARGUMENTS NEED TO GO HERE!!!)) {
			if (areWeInOurHalf(ARGUMENTS NEED TO GO HERE!!!)) {
				if (areTheyInOurHalf(ARGUMENTS NEED TO GO HERE!!!)) {
				
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
				
		} else if (doTheyHaveTheBall(ARGUMENTS NEED TO GO HERE!!!)) {
			if (areTheyInOurHalf) {
				mode = 3;
			} else {
				if (!areWeInOurHalf(ARGUMENTS NEED TO GO HERE!!!)) {
					mode = 4;
				}
			}
		
		return mode;
		
	}
	
	// Returns true if our robot has possession of the ball
	public boolean doWeHaveTheBall(Robot ourRobot, Ball ball) {
		boolean ourBall = move.getDist(ourRobot, ball) < 50; 
		
		return ourBall;
	}
	
	// Returns true if their robot has possession of the ball
	public boolean doTheyHaveTheBall(Robot theirRobot, Ball ball) {
		boolean theirBall = move.getDist(theirRobot, ball) < 50;
		
		return theirBall;
	}
	
	// Returns true if we are closer to our own goal than the opposition's
	public boolean areWeInOurHalf(Robot ourRobot, Position ourGoal, Position theirGoal) {
		boolean usCloserToOurGoal = move.getPtDist(ourRobot.getCoors(), ourGoal) < move.getPtDist(ourRobot.getCoors(), theirGoal);
		
		return usCloserToOurGoal;	
	}
	
	// Returns true if they are closer to our goal than theirs
	public boolean areTheyInOurHalf(Robot theirRobot, Position ourGoal, Position theirGoal) {
		boolean themCloserToOurGoal = move.getPtDist(theirRobot.getCoors(), ourGoal) < move.getPtDist(theirRobot.getCoors(), theirGoal);
	
		return themCloserToOurGoal;	
	}
	
	// Returns true if there is a robot between us, and the goal, in a straight line
	public boolean goalObstructed(Robot ourRobot, Robot theirRobot, Position theirGoal) {
		boolean obstruction = false;
		double slopeThemGoal = 0;
		double slopeUsGoal = 0;
		
		// if they're behind us then nothing will happen - make sure they are at least closer to the goal we're shooting towards
		if (getPtDist(ourRobot.getCoors(), theirGoal) < getPtDist(theirRobot.getCoors(), theirGoal)) {
		
			slopeThemGoal = (double) ((theirGoal.getY() - theirRobot.getCoors().getY()) / (theirGoal.getX() - theirRobot.getCoors().getX()));
			slopeUsGoal = (double) ((theirGoal.getY() - ourRobot.getCoors().getY()) / (theirGoal.getX() - ourRobot.getCoors().getX()));
		
			double theirAngle = Math.atan(slopeThemGoal);
			double ourAngle = Math.atan(slopeUsGoal);
			
			double difference = Math.abs(theirAngle - ourAngle);
			
			if (difference > 0.27) {
				obstruction = true;
			}
		
		}
	
		return obstruction;
	}

}