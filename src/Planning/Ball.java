package Planning;

public class Ball extends ObjectDetails {

//	private PointF speed = new PointF(0,0);
//	private PointF acceleration = new PointF(0,0);
//	private long lastSetTime;
//	private Position coors;

//	private LinkedList<Point> last5 = new LinkedList<Point>();

//	public Position getCoors() {
//		return coors;
//	}

//	public PointF getSpeed() {
//		return speed;
//	}

//	public PointF getAcc() {
//		return acceleration;
//	}

//	public void setCoors(Position pos) {
//		long timeSinceLastSet = System.currentTimeMillis() - lastSetTime;
		
//		if (last5.size() == 5) 
//		{
//				setSpeed(getRateOfChange(getAveragePoint(), new PointF(pos), timeSinceLastSet),
//						timeSinceLastSet);
//				lastSetTime = System.currentTimeMillis();
//				last5.remove(0);
//				last5.add(4,pos);
//				coors = pos;
//				
//		}
//		else
//		{
//			coors = pos;
//			last5.add(coors);
//		}import JavaVision.*;
//		
//		coors = pos;
//	}
//		public PointF getAveragePoint()
//		{
//			PointF ret = new PointF(0,0);
//			for (Point p : last5)
//			{
//				ret.x += p.x;
//				ret.y += p.y;
//			}
//			ret.x /= 5;
//			ret.y /= 5;
//			return ret;
//		}
	
//	public static final int WALL_TOP = ImageProcessor.yupperlimit;
//	public static final int WALL_BOTTOM = ImageProcessor.ylowerlimit;
//	public static final int WALL_LEFT = ImageProcessor.xlowerlimit;
//	public static final int WALL_RIGHT = ImageProcessor.xupperlimit;
//
//	public Point getExpectedPosition(int timeInFuture) {
//		float dx = getCoors().x + getSpeed().x * timeInFuture + 0.5f;// * acceleration.x * timeInFuture * timeInFuture;
//		float dy = getCoors().y + getSpeed().y * timeInFuture + 0.5f;// * acceleration.y * timeInFuture * timeInFuture;;
//		
//		if (dy > WALL_TOP)
//		{
//			//work out what time it would reach the wall then calculate from that spot where it will go next
//			//actually for now, im going to assume the ball stops...
//			dy = WALL_TOP;
//		}
//		
//		if (dy < WALL_BOTTOM)
//			dy = WALL_BOTTOM;
//		
//		if (dx < WALL_LEFT )
//			dx = WALL_LEFT;
//		
//		if (dx > WALL_RIGHT)
//			dx = WALL_RIGHT;
//		
//		
//		return new PointF(dx,dy).toPoint();
//	}
//
//	private void setSpeed(PointF newSpeed, long timeSinceLastSet) {
//		if (speed != null) {
//			setAcceleration(getRateOfChange(speed, newSpeed, timeSinceLastSet));
//		}
//
//		speed = newSpeed;
//	}
//
//	private void setAcceleration(PointF acceleration) {
//		this.acceleration = acceleration;
//	}
//
//	private PointF getRateOfChange(PointF oldP, PointF newP, long timeChange) {
//		if (timeChange == 0)
//			timeChange = 1;
//		return new PointF((newP.x - oldP.x) / timeChange, (newP.y - oldP.y) / timeChange);
//	}
}
