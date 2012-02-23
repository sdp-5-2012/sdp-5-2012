package Planning;

import java.util.ArrayList;
import java.awt.Point;

public class PathPlanner {
	
	public static boolean shootingLeft = true;
	//SET PITCH INFO
	public static int pitchX=565;
	public static int pitchY=358;
	
	public static int gridSize = 5;//size of a square on the map
	public static int gridX = (int) Math.ceil(pitchX/gridSize);
	public static int gridY = (int) Math.ceil(pitchY/gridSize);
	
	int correction = 10;
	public static ArrayList<GraphPoint> path;
	public static ArrayList<GraphPoint> occupied = new ArrayList<GraphPoint>();
	public static ArrayList<GraphPoint> checked;
	
	public static Point ourPositionCoor;
	public static Point goalPositionCoor;
	
		
	public static GraphPoint oppPositionGrid = new GraphPoint(0, 0);
	public static GraphPoint goalPositionGrid = new GraphPoint(0, 0);
	public static GraphPoint ourPositionGrid = new GraphPoint(0, 0);
	
	
	private static GraphPointComparator comparator = new GraphPointComparator();
	
	
	public Point getOptimalPath(Point ourPosition, Point goalPosition, Point oppPosition, int angle){
		
		GraphPoint ourPositionGrid = coordinatesToGrid(ourPosition) ;//Get our coordinates to grid X
		GraphPoint goalPositionGrid = coordinatesToGrid(goalPosition) ;//Get goal coordintes to grid X
		GraphPoint oppPositionGrid = coordinatesToGrid(oppPosition) ;//Get opponent's coordinates to grid X
	
		//Gets occupied grids on the map
//		ArrayList<GraphPoint> occupied = new ArrayList<GraphPoint>();
		path = new ArrayList<GraphPoint>();
		checked = new ArrayList<GraphPoint>();
		
		ourPositionCoor = ourPosition;
		goalPositionCoor = goalPosition;
		
		ourPositionGrid = coordinatesToGrid(ourPosition);
		goalPositionGrid = coordinatesToGrid(goalPosition);
		oppPositionGrid = coordinatesToGrid(oppPosition);
		//System.out.println(occupied);
		occupied.add(ourPositionGrid);
		occupied.add(goalPositionGrid);
		
		for(int x = 0; x<correction;x++){
			for(int y = 0;y<correction; y++){
				
				if((oppPosition.x+x<pitchX)&&(oppPosition.y<pitchY)){
					Point a = new Point(oppPosition.x+x,oppPosition.y+y);
					GraphPoint aa = coordinatesToGrid(a);
					
					occupied.add(aa);
					
				}
			   
				if((oppPosition.x-x>0)&&(oppPosition.y-y>0)){
					Point b = new Point(oppPosition.x-x,oppPosition.y-y);
					GraphPoint bb = coordinatesToGrid(b);
					occupied.add(bb);
				}
				
			}
		
		}
		
//		path = new ArrayList<GraphPoint>();
//		checked = new ArrayList<GraphPoint>();
//		occupied = new ArrayList<GraphPoint>();

		

		checked.add(ourPositionGrid);
		path = search(ourPositionGrid, goalPositionGrid);

		path = optimisePath(path);
		System.out.println(path.get(0));

		ArrayList<Point> waypoints=new ArrayList<Point>();;
		
		for(int x = 0; x<path.size();x++){
			waypoints.add(x,gridToCoor(path.get(x)));
		}
		
		System.out.println(waypoints.get(0));
		System.out.println(waypoints.size());
		return waypoints.get(0);
			
	}
		

	
	
	
	
	private GraphPoint coordinatesToGrid(Point a){
		
		int x = (int) Math.ceil(a.x/gridSize);
		int y = (int) Math.ceil(a.y/gridSize);

		GraphPoint b = new GraphPoint(x,y);
		
		
		return b;
	}

	private Point gridToCoor(GraphPoint a){
		int x = a.x*gridSize;
		int y = a.y*gridSize;
		
		a.x = x;
		a.y = y;
		
		return a;
		
	}
	
	private static int calcMovementCost(GraphPoint currentPoint, GraphPoint newPoint){
		
		/* SAME THING WITH PLACING INACCESSIBLE POINTS ON THE GRID
		 * 
		 * if (oppPositionGrid.distance(newPoint) < 3) {
			// discourage it heavily, to not crash into opponent
			return 500;
		}*/ 
		if((Math.abs(gridX-newPoint.x)<3)||(Math.abs(gridY-newPoint.y))<3){
			//Discourage going too close to walls
			return 100;
		}
		if (!shootingLeft) {
			if (Math.abs(newPoint.y - goalPositionGrid.y) < 6
					&& newPoint.x >= goalPositionGrid.x)
				return 65;
		}
		if (shootingLeft) {
			if (Math.abs(newPoint.y - goalPositionGrid.y) < 6
					&& newPoint.x <= goalPositionGrid.x)
				return 65;
		}
		if (oppPositionGrid.distance(newPoint) < 5) {
			// discourage points that are quite close to the opponent
			return 30;
		}
		if (Math.abs(oppPositionGrid.y - newPoint.y) < 5) {
			return 18;
		}
		// horizontal and vertical movements
		if (Math.abs(newPoint.x - currentPoint.x)
				+ Math.abs(newPoint.y - currentPoint.y) == 1) {
			return 10;
		}

		// diagonal movements
		if (Math.abs(newPoint.x - currentPoint.x)
				+ Math.abs(newPoint.y - currentPoint.y) == 2) {
			return 14;
		}

		return 0;

			
			
		}
	
	private static int calcHeuristicCost(GraphPoint currentPoint,
			GraphPoint endPoint) {
		// int xDist = (Math.abs(currentPoint.x - endPoint.x));
		// int yDist = (Math.abs(currentPoint.y - endPoint.y));
		// if (xDist > yDist) {
		// return 14 * yDist + 10*(xDist - yDist);
		// } else {
		// return 14 * xDist + 10*(yDist - xDist);
		// }
		return 10 * (Math.abs(endPoint.x - currentPoint.x) + Math
				.abs(endPoint.y - currentPoint.y));
	}

	public static void search(GraphPoint currentPoint, GraphPoint endPoint){
		
		for (int x = currentPoint.x - 1; x < currentPoint.x + 2; x++) {
			for (int y = currentPoint.y - 1; y < currentPoint.y + 2; y++) {
				GraphPoint pt = new GraphPoint(x, y);
				// check whether grid is on the "blacklist"
//				System.out.println(occupied);
				if (!occupied.contains(pt)) {
					// check in range of grids
					if (x > 0 && y > 0 && x <= gridX && y <= gridY) {
						// if it's not already on check list, add it
						if (!checked.contains(pt)) {
							checked.add(pt);
							pt.setParent(currentPoint);
							if ((pt.x == goalPositionGrid.x) && (pt.y == goalPositionGrid.y)) {
								goalPositionGrid.setParent(currentPoint);
							}
							pt.setMovementCost(pt.getParent().getMovementCost() + calcMovementCost(currentPoint, pt));
							pt.setHeuristicCost(calcHeuristicCost(pt,endPoint));
							pt.setTotalCost(pt.getMovementCost() + pt.getHeuristicCost());
						}
						if (checked.contains(pt)) {
							if (pt.getMovementCost() > calcMovementCost(currentPoint, pt)) {
								pt.setParent(currentPoint);
								pt.setMovementCost(pt.getParent().getMovementCost() + calcMovementCost(currentPoint, pt));
								pt.setTotalCost(pt.getMovementCost() + pt.getHeuristicCost());
							}
						}
					}
				}
				
				
				
			}
		}
		checked = comparator.sortGridPoints(checked);
		if (checked.size() > 0) {
			GraphPoint closestPt = checked.get(0);

			checked.remove(closestPt);
			if ((closestPt.x == goalPositionGrid.x)
					&& (closestPt.y == goalPositionGrid.y)) {
				tracePath(ourPositionGrid, goalPositionGrid);
			} else {
				occupied.add(closestPt);
				search(closestPt, endPoint);
			}
		} else
			return;
	}
	
	
	private static void tracePath(GraphPoint startPoint, GraphPoint endPoint) {
		path.add(0, endPoint);
		if (endPoint.getParent() != null) {
			tracePath(startPoint, endPoint.getParent());
		}
	}
	
	
	
	private static ArrayList<GraphPoint> optimisePath(ArrayList<GraphPoint> path) {

		ArrayList<GraphPoint> newPath = path;
		for (int i = 0; i < newPath.size() - 1; i++) {
			// remove points that are too close to each other
			if (newPath.get(i).distance(newPath.get(i + 1)) < 3) {
				newPath.remove(i + 1);
			}
		}

		// optimise angles repeatedly 3 times
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < newPath.size() - 2; i++) {
				// remove points that hardly change in gradient
				if (Math.abs((getAngle(newPath.get(i), newPath.get(i + 1)))
						- (getAngle(newPath.get(i + 1), newPath.get(i + 2)))) < 30)
					newPath.remove(i + 2);
			}
		}

		return newPath;
	}
	
	
	private static double getAngle(Point a, Point b) {
		return Math.toDegrees(Math.atan2((a.y - b.y), (b.x - a.x)));
	}
}

	

