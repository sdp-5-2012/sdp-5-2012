package Planning;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import JavaVision.Position;

public class Arc {
	public static final int MOTION_ROTATE = 0;
	public static final int MOTION_FORWARD = 1;
	public static final int MOTION_ARC = 2;
	
	private double angleToRotateBy;
	private double arcRadius;
	private double arcAngle;
	private double distanceToTravel;
	
	
	public Point2D.Double intersectionPoint;
	
	public Arc() {
		angleToRotateBy = 0;
		arcRadius = 0;
		arcAngle = 0;
		distanceToTravel = 0;
	}
	
	public int calculateArc(Point2D.Double robot, Point2D.Double ball, Point2D.Double target, double orientation) {
		angleToRotateBy = 0;
		arcRadius = 0;
		arcAngle = 0;
		distanceToTravel = 0;
		
		double xPoint = (Math.sin(orientation) * 100) + robot.x;
		double yPoint = (Math.cos(orientation) * 100) + robot.y;
		
		Point2D.Double robotHelperPoint = new Point2D.Double(xPoint, yPoint);
		
		intersectionPoint = findIntersectionPoint(robot, robotHelperPoint, ball, target);
				
		if (intersectionPoint == null) {
			//we are parallel
			//must rotate by 5 degrees
			angleToRotateBy = (2*Math.PI /360) * 5;
			if (angleToRotateBy > Math.PI) {
				angleToRotateBy = (2* Math.PI) - angleToRotateBy * -1;
			} 
			System.out.println("parallel");
			return MOTION_ROTATE;			
		}
		/*
		 * not clear
		 */
		if (intersectsBehindTarget(intersectionPoint, ball, target)) {
			if (robot.y > ball.y) {
				angleToRotateBy = -1 * (findAngleFromThreePoints(intersectionPoint, robot, ball) + (((2*Math.PI/360)*5)%(2*Math.PI)));
			} else if (robot.y < ball.y) {
				angleToRotateBy = findAngleFromThreePoints(intersectionPoint, ball, robot) + (((2*Math.PI/360)*5)%(2*Math.PI));
			}
			
			if (angleToRotateBy > Math.PI) {
				angleToRotateBy = (((2* Math.PI) - angleToRotateBy) * -1) % (2*Math.PI);
			} 
			System.out.println("Intersection behind target");
			return MOTION_ROTATE;
		}
		
		double tmpPointX = (Math.sin(Math.PI * 2) * 10 ) + robot.x;
		double tmpPointY = (Math.cos(Math.PI * 2) * 10) + robot.y;
		
		Point2D.Double tmpPoint = new Point2D.Double(tmpPointX, tmpPointY);
		
	
		
		

		double projectedOrientation = findAngleFromThreePoints(robot, tmpPoint , intersectionPoint);
		System.out.println("projected, actual: " + projectedOrientation + ", " + orientation);
		
		
		if (Math.abs(projectedOrientation-orientation) > (Math.PI) ) {
			angleToRotateBy = (Math.PI - findAngleFromThreePoints(intersectionPoint, ball, robot)) + ((2*Math.PI/360)%(2*Math.PI));
			if (angleToRotateBy > Math.PI) {
				angleToRotateBy = (2* Math.PI) - angleToRotateBy * -1;
			} 
			System.out.println(intersectionPoint.x + ", " + robot.x);
			System.out.println("facing wrong direction " + angleToRotateBy);
			return MOTION_ROTATE;
		}
		
		
		double distanceIntersectionRobot = Math.sqrt( 
				Math.pow((intersectionPoint.x-robot.x),2) 
				+ (Math.pow((intersectionPoint.y-robot.y),2)));
		
		double distanceIntersectionBall = Math.sqrt( 
				Math.pow((intersectionPoint.x-ball.x),2) 
				+ (Math.pow((intersectionPoint.y-ball.y),2)));
		
		
		Point2D.Double pointRobot = new Point2D.Double();
		Point2D.Double pointBall = new Point2D.Double();
		
		if ( distanceIntersectionBall > distanceIntersectionRobot) {
			pointRobot = robot;
			pointBall.x = (Math.sin(findRelativeAngle(ball, intersectionPoint)) * distanceIntersectionRobot) + intersectionPoint.x;
			pointBall.y = (Math.cos(findRelativeAngle(ball, intersectionPoint)) * distanceIntersectionRobot) + intersectionPoint.y;
			
		} else if ( distanceIntersectionRobot > distanceIntersectionBall){
			pointBall = ball;
			pointRobot.x = (Math.sin(findRelativeAngle(robot, intersectionPoint))* distanceIntersectionBall) + intersectionPoint.x;
			pointRobot.y = (Math.cos(findRelativeAngle(robot, intersectionPoint))* distanceIntersectionBall) + intersectionPoint.y;
			
		} else {
			pointBall = ball;
			pointRobot = robot;
		}
		
		double angleRobotIntersection = findRelativeAngle(intersectionPoint, pointRobot);
		double angleBallIntersection = findRelativeAngle(intersectionPoint, pointBall);
		
		System.out.println("robot angle ball angle:" + angleRobotIntersection + ", " + angleBallIntersection);
		
		double xOrthogonalBallPoint = ((Math.sin(angleBallIntersection+(Math.PI/2)) % (2*Math.PI) ) * 5) + pointBall.x;
		double yOrthogonalBallPoint = ((Math.cos(angleBallIntersection+(Math.PI/2)) % (2*Math.PI) ) * 5) + pointBall.y;
		
		double xOrthogonalRobotPoint = ((Math.sin(angleRobotIntersection+(Math.PI/2)) % (2*Math.PI) ) * 5) + pointRobot.x;
		double yOrthogonalRobotPoint = ((Math.cos(angleRobotIntersection+(Math.PI/2)) % (2*Math.PI) ) * 5) + pointRobot.y;
		
		Point2D.Double orthogonalBallPoint = new Point2D.Double(xOrthogonalBallPoint, yOrthogonalBallPoint);
		Point2D.Double orthogonalRobotPoint = new Point2D.Double(xOrthogonalRobotPoint, yOrthogonalRobotPoint);
		
		Point2D.Double orthogonalIntersectionPoint = findIntersectionPoint(pointBall, orthogonalBallPoint, pointRobot, orthogonalRobotPoint);
		
		System.out.println("circle point: " + orthogonalIntersectionPoint.x + ", " + orthogonalIntersectionPoint.y);
		System.out.println("Ball: " + ball.x + ", " + ball.y);
		System.out.println("displaced ball: " + pointBall.x + ", " + pointBall.y);
		System.out.println("robot: " + robot.x + ", " + robot.y);
		System.out.println("displaced robot: "+ pointRobot.x + ", " + pointRobot.y);
		
		//travel to point
		double distancePointRobotToIntersection = Math.sqrt(Math.pow(pointRobot.x - intersectionPoint.x, 2) + Math.pow(pointRobot.y - intersectionPoint.y,2));
		System.out.println("Distance to Robot: " + distanceIntersectionRobot);
		System.out.println("Distance to displaced robot: " + distancePointRobotToIntersection);
		distanceToTravel = Math.abs(distanceIntersectionRobot - distancePointRobotToIntersection);
		if (distanceToTravel > 50) {
			System.out.println("distance to travel " + distanceToTravel);
			return MOTION_FORWARD;
		}
		
		
		arcAngle = findAngleFromThreePoints(orthogonalIntersectionPoint, pointBall, pointRobot);
		
		if (arcAngle > Math.PI)
			arcAngle = (2*Math.PI) - arcAngle;
		
		//radius of circle
		
		arcRadius = Math.sqrt(Math.pow(pointRobot.x - orthogonalIntersectionPoint.x,2) + Math.pow(pointRobot.y-orthogonalIntersectionPoint.y, 2) );
		
		if(orthogonalIntersectionPoint.y > pointBall.y) {
			arcRadius = arcRadius * -1;	
		} 
		
		return MOTION_ARC;	
				
		
		
	} 
	
	public double getRotation() {
		//changed to negative angle
		
			return -angleToRotateBy;
		
		
	}
	
	public double getDistance() {
		return distanceToTravel;
	}
	
	public double getArcRadius() {
		return arcRadius;
	}
	
	public double getArcAngle() {
		return arcAngle;
	}
	

	private static double findAngleFromThreePoints(Point2D.Double intersectionPoint, Point2D.Double ball, Point2D.Double robot) {
		//cos(theta) = (a^2+b^2-c^2) / 2ab
		double c = Math.sqrt(Math.pow((robot.x-ball.x),2) + Math.pow((robot.y-ball.y), 2));
		double a = Math.sqrt(Math.pow((intersectionPoint.x-ball.x),2) + Math.pow((intersectionPoint.y-ball.y), 2));
		double b = Math.sqrt(Math.pow((intersectionPoint.x-robot.x),2) + Math.pow((intersectionPoint.y-robot.y), 2));
		
		if (robot.x < intersectionPoint.x ) {
			return (2*Math.PI) - Math.acos((a*a + b*b - c*c)/(2*a*b));	
		}
		return Math.acos((a*a + b*b - c*c)/(2*a*b));
		
		
	}
	private static boolean intersectsBehindTarget(Point2D.Double intersectPoint, Point2D.Double ball, Point2D.Double target) {
		if (ball.x > target.x && ball.y > target.y) { 
			if (intersectPoint.x < ball.x) {
				return true;
			}
		} else if (ball.x > target.x && ball.y < target.y) {
			if (intersectPoint.x < ball.x) {
				return true;
			}
		} else if (ball.x < target.x && ball.y > target.y) {
			if (intersectPoint.x > ball.x) {
				return true;
			}
		} else if (ball.x < target.x && ball.y < target.y) {
			if (intersectPoint.x > ball.x) {
				return true;
			}
		}
		return false;
	}
	
	private static double findRelativeAngle(Point2D.Double point, Point2D.Double origin) {
		
		double pointValueX = (Math.sin(2*Math.PI) * 10) + origin.x;
		double pointValueY = (Math.cos(2*Math.PI) * 10) + origin.y;
		Point2D.Double pointValue = new Point2D.Double(pointValueX, pointValueY);
		double tempAngle = findAngleFromThreePoints(origin, pointValue, point);
		
//		if (tempAngle > Math.PI) {
//			return (2*Math.PI - tempAngle);
//		}
		return tempAngle;
		
	}
	
	private static Point2D.Double findIntersectionPoint(Point2D.Double l1p1, Point2D.Double l1p2, Point2D.Double l2p1, Point2D.Double l2p2) {
		
		double denominator = ((l1p1.x - l1p2.x)*(l2p1.y-l2p2.y)) 
				- ((l1p1.y-l1p2.y)*(l2p1.x-l2p2.x));
		
		if (Math.abs(denominator) < 0.001) {
			return null;
		}
		
		double xIntersectionPoint = ((((l1p1.x*l1p2.y)
				- (l1p1.y*l1p2.x))*(l2p1.x-l2p2.x))
				- ((l1p1.x-l1p2.x) 
				* ((l2p1.x*l2p2.y)-(l2p1.y*l2p2.x) ))) / denominator;
		
		double yIntersectionPoint = ((((l1p1.x*l1p2.y)
				- (l1p1.y*l1p2.x))*(l2p1.y-l2p2.y))
				- ((l1p1.y-l1p2.y) 
				* ((l2p1.x*l2p2.y)-(l2p1.y*l2p2.x) ))) / denominator;
		
		return new Point2D.Double(xIntersectionPoint, yIntersectionPoint);
	}

	
	
}
