package Planning;

import java.awt.geom.Point2D;

public class ArcTest {

	static double rotation;
	static double distance;
	static double arcRadius;
	static double arcAngle;
	
	static Arc arc = new Arc();
	
	public static void main(String[] args ) {
		System.out.println("test1: ");
		asdf(arc.calculateArc(new Point2D.Double(4,-5), new Point2D.Double(3,4), new Point2D.Double(0,0), (Math.PI/2)));
		System.out.println("test2: ");
		asdf(arc.calculateArc(new Point2D.Double(3,-2), new Point2D.Double(6,1), new Point2D.Double(0,0), (Math.PI*2)));
		System.out.println("test3: ");
		asdf(arc.calculateArc(new Point2D.Double(3,-2), new Point2D.Double(5,0), new Point2D.Double(0,0), (Math.PI/2)));
		System.out.println("test4: ");
		asdf(arc.calculateArc(new Point2D.Double(4,-4), new Point2D.Double(4,4), new Point2D.Double(0,0), (4*Math.PI/6)));
		System.out.println("test5: ");
		asdf(arc.calculateArc(new Point2D.Double(5,0), new Point2D.Double(5,4), new Point2D.Double(0,0), (Math.PI/4)));
		System.out.println("test6: ");
		asdf(arc.calculateArc(new Point2D.Double(2,-5), new Point2D.Double(1,1), new Point2D.Double(0,0), (Math.PI/8)));
		System.out.println("test7: ");
		asdf(arc.calculateArc(new Point2D.Double(5,-1), new Point2D.Double(5,0), new Point2D.Double(0,0), (Math.PI*2)));
		
		System.out.println("test8: ");
		asdf(arc.calculateArc(new Point2D.Double(14,-10), new Point2D.Double(15,-3), new Point2D.Double(5,-5), (Math.PI)));
		
		System.out.println("test9: ");
		asdf(arc.calculateArc(new Point2D.Double(5,-10), new Point2D.Double(5,-3), new Point2D.Double(5,-5), (Math.PI/2)));
		
		System.out.println("test10: ");
		asdf(arc.calculateArc(new Point2D.Double(15,-5), new Point2D.Double(10,-8), new Point2D.Double(5,-5), (Math.PI/4)));
		
		System.out.println("test11: ");
		asdf(arc.calculateArc(new Point2D.Double(15,-5), new Point2D.Double(10,-8), new Point2D.Double(5,-5), (Math.PI*5/6)));
	}
	
	public static void asdf(int boo) {
		//this could be in a while loop
		switch (boo) {
		case(Arc.MOTION_ROTATE):
			rotation = arc.getRotation();
			System.out.println("rotate: " + rotation);
			//nxt.rotate or something
			break;
		case(Arc.MOTION_FORWARD):
			distance = arc.getDistance();
			System.out.println("travel: " + distance);
			//nxt.travel ...
			break;
		case(Arc.MOTION_ARC):
			arcRadius = arc.getArcRadius();
			arcAngle = arc.getArcAngle();
			System.out.println("arc: " + arcRadius + ", " + arcAngle);
			//nxt.arc with radius and angle
			break;
		}
	}
 }
