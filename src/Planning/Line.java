package Planning;

import JavaVision.Position;

public class Line {

	Position p1;
	Position p2;
	
	double angle;
	
	double gradient;
	double yIntercept;

	public Line(Position p1, Position p2)
	{
		this.p1 = p1;
		this.p2 = p2;
		
		gradient = getGradient(p1, p2);
		yIntercept = getIntercept(p1, gradient);
	}
	public Line(Position p1, double angle)
	{
		this.p1 = p1;
		this.angle = angle;
		
		gradient = Math.tan(angle);
		yIntercept = getIntercept(p1, gradient);
	}
	
	public int getYfromX(int x)
	{
		return (int) Math.round(x * gradient + yIntercept);
	}
	
	public Position getP1() {
		return p1;
	}
	public void setP1(Position p1) {
		this.p1 = p1;
	}
	public Position getP2() {
		return p2;
	}
	public void setP2(Position p2) {
		this.p2 = p2;
	}
	public double getYfromX(double x)
	{
		return (double)x * gradient + yIntercept;
	}
	
	public boolean isPositionAboveLine(Position p)
	{
		return getYfromX(p.getX()) < p.getY();
	}
	
	public void printEquation()
	{
		System.out.println("y = " + gradient + "x + " + yIntercept);
	}
	
    public static float getGradient(Position p1, Position p2)
    {
    	//slight hack allowing us to ignore when p1.x == p2.x
    	if (p1.getX() == p2.getX())
    		return Float.MAX_VALUE;
    	
    	return ((float) (p2.getY() - p1.getY())) / (p2.getX()-p1.getX());
    }
    
    public static double getIntercept(Position p, double gradient2)
    {
    	return (p.getY() - gradient2 * p.getX());
    }
    
    public boolean isPositionOnTheLine(Position o)
    {
    	return distanceBetweenPositionAndALine(o) < 1;
    }
    
    public double distanceBetweenPositionAndALine(Position o)
    {
    	//variables are left as x0, y0 etc for clarity.static
		int x0 = o.getX();
		int y0 = o.getY();
		
		int x1 = p1.getX();
		int y1 = p1.getY();
		
		double x2;
		double y2;
		
		if(p2 == null){
			x2 = x1+1;
			y2 = this.getYfromX((double)x1+1);
		} else {
			x2 = p2.getX();
			y2 = p2.getY();
		}
		//http://mathworld.wolfram.com/Position-LineDistance2-Dimensional.html
		return Math.abs((x2-x1)*(y1-y0)-(x1-x0)*(y2-y1))/Math.sqrt(Math.pow((x2-x1), 2)+Math.pow((y2-y1), 2));
    }
}
