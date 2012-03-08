//package Strategy;
//
//import JavaVision.*;
//import Planning.*;
//
//public class StrategyX
//{
//	
//	Move move;
//	
//	int loopOurStrategy(Robot ourRobot, Robot theirRobot, Ball ball, Position ourGoal, Position theirGoal, Position pitchCentre) 
//	{
//		
//		boolean stopcommand = false;
//		
//		while(!stopcommand)
//		{
//			goAndGetTheBall();//-->This method has not been created yet.
//			
//			switch(doWeHaveTheBall())
//			{
//			case 0: //If we fail to get the ball than defend
//				defend();//-->This method has not been completed yet:
//				          //
//				          //switch(haveWeOstructedThem(ourRobot, theirRobot, ourGoal))
//		                  //{
//		                  //  case 0: while(haveWeOstructedThem(ourRobot, theirRobot, ourGoal))
//				          //          obstructEnemy(); //GO AND OBSTRUCT IT;
//		                  //          break;
//		                  //  case 1:
//				          //          break;
//		                  //}
//				break;
//			case 1://-->if doWeHaveTheBall is 0(false) : we must defend
//				attack(); //-->This method has not been completed yet :
//				          //
//				          //switch(goalObstructed(ourRobot, theirRobot, theirGoal))
//				          //{
//				          //  case 0: kick();
//				          //          break;
//				          //  case 1: while(goalObstructed(ourRobot, theirRobot, theirGoal))
//				          //          findKickChance(); //GO UP AND DOWN TO MAKE TWO ROBOTS NOT IN THE STRAIGHT LINE
//				          //          break;
//				          //}
//				break;
//			}
//			if(all work has been completed)//-->find a way to make the variable 'stopcommand' true if all work has been completed
//			{
//				stopcommand = true;
//			}
//			else
//			{
//				//-->Do more work
//			}
//			
//			
//		}
//	
//		
//		
//	// Returns true if our robot has possession of the ball
//	public boolean doWeHaveTheBall(Robot ourRobot, Ball ball) 
//	{
//		boolean ourBall = move.getDist(ourRobot, ball) < 50; 
//		
//		return ourBall;
//	}
//	
//	// Returns true if there is a robot between us, and the goal, in a straight line
//	public boolean goalObstructed(Robot ourRobot, Robot theirRobot, Position theirGoal) 
//	{
//		boolean obstruction = false;
//		double slopeThemGoal = 0;
//		double slopeUsGoal = 0;
//		
//		// if they're behind us then nothing will happen - make sure they are at least closer to the goal we're shooting towards
//		if (getPtDist(ourRobot.getCoors(), theirGoal) < getPtDist(theirRobot.getCoors(), theirGoal)) 
//		{
//		
//			slopeThemGoal = (double) ((theirGoal.getY() - theirRobot.getCoors().getY()) / (theirGoal.getX() - theirRobot.getCoors().getX()));
//			slopeUsGoal = (double) ((theirGoal.getY() - ourRobot.getCoors().getY()) / (theirGoal.getX() - ourRobot.getCoors().getX()));
//		
//			double theirAngle = Math.atan(slopeThemGoal);
//			double ourAngle = Math.atan(slopeUsGoal);
//			
//			double difference = Math.abs(theirAngle - ourAngle);
//			
//			if (difference > 0.27) 
//			{
//				obstruction = true;
//			}
//		
//		}
//	
//		return obstruction;
//	}
//	// Returns true if we obstructed them successfully(Our robot is between our goal and their robot, in a straight line)
//		public boolean haveWeOstructedThem(Robot ourRobot, Robot theirRobot, Position ourGoal) 
//		{
//			boolean obstruction = false;
//			double slopeThemOurgoal = 0;
//			double slopeUsOurgoal = 0;
//			
//			if (getPtDist(ourRobot.getCoors(), ourGoal) < getPtDist(theirRobot.getCoors(), ourGoal)) 
//			{
//			
//				slopeThemOurgoal = (double) ((ourGoal.getY() - theirRobot.getCoors().getY()) / (ourGoal.getX() - theirRobot.getCoors().getX()));
//				slopeUsOurgoal = (double) ((ourGoal.getY() - ourRobot.getCoors().getY()) / (ourGoal.getX() - ourRobot.getCoors().getX()));
//			
//				double theirAngle = Math.atan(slopeThemOurgoal);
//				double ourAngle = Math.atan(slopeUsOurgoal);
//				
//				double difference = Math.abs(theirAngle - ourAngle);
//				
//				if (difference > 0.27) 
//				{
//					obstruction = true;
//				}
//			
//			}
//		
//			return obstruction;
//		}