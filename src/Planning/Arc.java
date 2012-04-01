package Planning;

import java.awt.geom.Point2D;

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
			return MOTION_ROTATE;			
		}
		
		if (intersectsBehindTarget(intersectionPoint, ball, target)) {
			angleToRotateBy = findAngleFromThreePoints(intersectionPoint, ball, robot) + (((2*Math.PI/360)*5)%(2*Math.PI));
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
//			System.out.println("Ball distance greater");
			pointRobot = robot;
			pointBall.x = (Math.sin(findRelativeAngle(ball, intersectionPoint)) * distanceIntersectionRobot) + ball.x;
			pointBall.y = (Math.cos(findRelativeAngle(ball, intersectionPoint)) * distanceIntersectionRobot) + ball.y;
			
		} else if ( distanceIntersectionRobot > distanceIntersectionBall){
//			System.out.println("Robot distance greater");
			pointBall = ball;
			pointRobot.x = (Math.sin(findRelativeAngle(robot, intersectionPoint))* distanceIntersectionBall) + robot.x;
			pointRobot.y = (Math.cos(findRelativeAngle(robot, intersectionPoint))* distanceIntersectionBall) + robot.y;
			
		} else {
//			System.out.println("Distances equal");
			pointBall = ball;
			pointRobot = robot;
		}
		
		double angleRobotIntersection = findRelativeAngle(pointRobot, intersectionPoint);
		double angleBallIntersection = findRelativeAngle(pointBall, intersectionPoint);
		
		double xOrthogonalBallPoint = (Math.sin(angleBallIntersection+(Math.PI/2)) * 100) + pointBall.x;
		double yOrthogonalBallPoint = (Math.cos(angleBallIntersection+(Math.PI/2)) * 100) + pointBall.y;
		
		double xOrthogonalRobotPoint = (Math.sin(angleRobotIntersection+(Math.PI/2)) * 100) + pointRobot.x;
		double yOrthogonalRobotPoint = (Math.cos(angleRobotIntersection+(Math.PI/2)) * 100) + pointRobot.y;
		
		Point2D.Double orthogonalBallPoint = new Point2D.Double(xOrthogonalBallPoint, yOrthogonalBallPoint);
		Point2D.Double orthogonalRobotPoint = new Point2D.Double(xOrthogonalRobotPoint, yOrthogonalRobotPoint);
		
		Point2D.Double orthogonalIntersectionPoint = findIntersectionPoint(pointBall, orthogonalBallPoint, pointRobot, orthogonalRobotPoint);
		
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
		
		//radius of circle
		
		if(orthogonalIntersectionPoint.y < pointBall.y) {
			arcRadius = Math.sqrt(Math.pow(pointBall.x - orthogonalIntersectionPoint.x,2) + Math.pow(pointBall.y-orthogonalIntersectionPoint.y, 2) );	
		} else {
			arcRadius = -1*Math.sqrt(Math.pow(pointBall.x - orthogonalIntersectionPoint.x,2) + Math.pow(pointBall.y-orthogonalIntersectionPoint.y, 2) );
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
		return (Math.atan2(point.y -origin.y, point.x-origin.x)+(Math.PI*2))%(2*Math.PI);
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
