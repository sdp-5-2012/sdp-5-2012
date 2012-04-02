package Planning;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class Defend {
	
	
	
	public boolean isFacingGoal(Point2D.Double robot, Point2D.Double ball, Point2D.Double goalUpper, Point2D.Double goalLower, double orientation) {

		Point2D.Double robotBallIntersection = findIntersectionPoint(robot, ball, goalUpper, goalLower);

				
		double tmpX = (Math.sin(Math.PI * 2) * 10 + ball.x);
		double tmpY = (Math.cos(Math.PI * 2) * 10 + ball.y);
		
		Point2D.Double tmpPoint = new Point2D.Double(tmpX, tmpY);
		
//		System.out.println("robot ball intersection");
//		System.out.println(robot.x + ", " + robot.y);
//		System.out.println(ball.x + ", " + ball.y);
//		System.out.println(robotBallIntersection.x + ", " + robotBallIntersection.y);
//		System.out.println();
		if ((robotBallIntersection.y < goalUpper.y) && (robotBallIntersection.y > goalLower.y)) {
			
			double angleBetweenLines = findAngleFromThreePoints(ball, tmpPoint, robotBallIntersection);
			if (Math.abs(angleBetweenLines - orientation) <= (Math.PI/8)) {
				return true;
			}
		}
		return false;

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

	private static double findAngleFromThreePoints(Point2D.Double intersectionPoint, Point2D.Double ball, Point2D.Double robot) {
		//cos(theta) = (a^2+b^2-c^2) / 2ab
		double c = Math.sqrt(Math.pow((robot.x-ball.x),2) + Math.pow((robot.y-ball.y), 2));
		double a = Math.sqrt(Math.pow((intersectionPoint.x-ball.x),2) + Math.pow((intersectionPoint.y-ball.y), 2));
		double b = Math.sqrt(Math.pow((intersectionPoint.x-robot.x),2) + Math.pow((intersectionPoint.y-robot.y), 2));

		return Math.acos((a*a + b*b - c*c)/(2*a*b));
		
		

	}
	
}
