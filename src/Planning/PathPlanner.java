package Planning;

import java.util.ArrayList;
import java.awt.Point;
import JavaVision.*;
public class PathPlanner {
	public static boolean shootingLeft = false;
	// SET PITCH INFO
	public static int pitchXX = 567;
	public static int pitchYY = 360;

	public static int pitchX = 493;// 500
	public static int pitchY = 245;// 245
	// Displacement for the side pitch
	public int displacementX = 59;
	public int displacementY = 89;
	public static int gridSize = 10;// size of a square on the map
	public static int gridX = (int) Math.ceil(pitchX / gridSize);
	public static int gridY = (int) Math.ceil(pitchY / gridSize);
	int correction = 40;
	public static ArrayList<GraphPoint> path;
	public static ArrayList<GraphPoint> occupied = new ArrayList<GraphPoint>();
	public static ArrayList<GraphPoint> checked;
	public static Position ourPositionCoor;
	public static Position goalPositionCoor;
	public static Position oppPositionCoor;
	public static GraphPoint oppPositionGrid;
	public static GraphPoint goalPositionGrid;
	public static GraphPoint ourPositionGrid;
	// ADDITIONAL
	public static GraphPoint newGoalGrid;
	private static GraphPointComparator comparator = new GraphPointComparator();

	// public Position getOptimalPath(Position ourPosition, Position goalPosition, Position
	// oppPosition, int angle){
	//
	//
	//
	// //Gets occupied grids on the map
	//
	// path = new ArrayList<GraphPoint>();
	// checked = new ArrayList<GraphPoint>();
	//
	// ourPositionCoor.x=ourPosition.x;// - displacementX;
	// ourPositionCoor.y=ourPosition.y;// - displacementY;
	//
	// oppPositionCoor.x=oppPosition.x;// - displacementX;
	// oppPositionCoor.y=oppPosition.y;// - displacementY;
	// //THIS WAS
	// CHANGED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!^
	// goalPositionCoor.x = goalPosition.x;//-displacementX;
	// goalPositionCoor.y = goalPosition.x;//-displacementY;
	//
	// ourPositionGrid = coordinatesToGrid(ourPositionCoor);
	// goalPositionGrid = coordinatesToGrid(goalPositionCoor);
	// oppPositionGrid = coordinatesToGrid(oppPositionCoor);
	//
	// //System.out.println(occupied);
	//
	// occupied.add(ourPositionGrid);
	//
	// occupied.add(oppPositionGrid);
	//
	//
	// //correction set to 10
	// for(int x = 0; x<correction;x++){
	// for(int y = 0;y<correction; y++){
	//
	// //getting 10 pixels in which the opposition may be sitting
	// if((oppPosition.x+x<pitchX)&&(oppPosition.y<pitchY)){
	// Position a = new Position(oppPosition.x+x,oppPosition.y+y);
	// GraphPoint aa = coordinatesToGrid(a);
	//
	// occupied.add(aa);
	//
	// }
	//
	// //getting 10 more pixels in which the opposition may be sitting
	// if((oppPosition.x-x>0)&&(oppPosition.y-y>0)){
	// Position b = new Position(oppPosition.x-x,oppPosition.y-y);
	// GraphPoint bb = coordinatesToGrid(b);
	// occupied.add(bb);
	// }
	//
	// }
	//
	// }
	//
	//
	// //System.out.println(ourPositionGrid);
	// // path = new ArrayList<GraphPoint>();
	// // checked = new ArrayList<GraphPoint>();
	// // occupied = new ArrayList<GraphPoint>();
	//
	//
	//
	// checked.add(ourPositionGrid);
	//
	// search(ourPositionGrid, goalPositionGrid);
	//
	//
	//
	// //path = optimisePath(path);
	// //System.out.println(path.get(0));
	//
	// ArrayList<Position> wayPositions=new ArrayList<Position>();
	// //System.out.println("PATH SIZE IS: " + path.size());
	//
	// for(int x = 0; x<path.size();x++){
	// wayPositions.add(x,gridToCoor(path.get(x)));
	// }
	//
	// // System.out.println(wayPositions.get(0));
	// // System.out.println(wayPositions.size());
	// //System.out.println(wayPositions);
	//
	//
	// //GIVES THE FIRST WAYPosition OF THE PATH FOUND
	// return wayPositions.get(0);
	//
	// }
	public PathPlanner(boolean attackLeft) {
		shootingLeft = attackLeft;
	}

	public ArrayList<Position> getOptimalPath(Position ourPosition,
			Position goalPosition, Position oppPosition) {
		// Gets occupied grids on the map

		path = new ArrayList<GraphPoint>();
		checked = new ArrayList<GraphPoint>();
		ourPositionCoor = new Position(ourPosition.getX() - displacementX, ourPosition.getY() - displacementY);
		oppPositionCoor = new Position(oppPosition.getX() - displacementX, oppPosition.getY() - displacementY);
		goalPositionCoor = new Position(goalPosition.getX() - displacementX, goalPosition.getY() - displacementY);


		//UNCOMMENT WHEN POSITIONING TO SHOOT
//		if(shootingLeft) {
//			goalPositionCoor.setX(goalPositionCoor.getX() + 60);
//
//			int dev = 122 -goalPositionCoor.getY();
//			if(dev<0&&((goalPositionCoor.getY()-dev/2)<pitchY)){
//				goalPositionCoor.setY(goalPositionCoor.getY()-dev/3);
//			}
//			if(dev>0&&(goalPositionCoor.getY()-dev/2>0)){
//				goalPositionCoor.setY(goalPositionCoor.getY()-dev/3);
//			}
//
//		}else{
//			goalPositionCoor.setX(goalPositionCoor.getX() - 60);
//
//			int dev = 122 -goalPositionCoor.getY();
//			if(dev<0&&((goalPositionCoor.getY()-dev/2)<pitchY)){
//				goalPositionCoor.setY(goalPositionCoor.getY()-dev/3);
//			}
//			if(dev>0&&(goalPositionCoor.getY()-dev/2>0)){
//				goalPositionCoor.setY(goalPositionCoor.getY()-dev/3);
//			}
//		}
		// THIS WAS
		// CHANGED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!^
		ourPositionGrid = coordinatesToGrid(ourPositionCoor);
		goalPositionGrid = coordinatesToGrid(goalPositionCoor);
		oppPositionGrid = coordinatesToGrid(oppPositionCoor);
		// System.out.println(occupied);
		occupied.add(ourPositionGrid);
		occupied.add(oppPositionGrid);

		// correction set to 10
//		for (int x = 0; x < correction; x++) {
//			for (int y = 0; y < correction; y++) {
//				// getting 10 pixels in which the opposition may be sitting
//				if ((oppPositionCoor.getX() + x < pitchX) && (oppPositionCoor.getY() < pitchY)) {
//					Position a = new Position(oppPositionCoor.getX() + x, oppPositionCoor.getY() + y);
//					GraphPoint aa = coordinatesToGrid(a);
//					occupied.add(aa);
//				}
//
//				// getting 10 more pixels in which the opposition may be sitting
//				if ((oppPositionCoor.getX() - x > 0) && (oppPositionCoor.getY() - y > 0)) {
//
//					Position b = new Position(oppPositionCoor.getX() - x, oppPositionCoor.getY() - y);
//					GraphPoint bb = coordinatesToGrid(b);
//					occupied.add(bb);
//				}
//			}
//		}

		// correction set to 10
		//		for (int x = 0; x < 30; x++) {
		//			for (int y = 0; y < 2; y++) {
		//				// getting 10 pixels in which the opposition may be sitting
		//				if ((goalPositionCoor.getX() + x < pitchX) && (goalPositionCoor.getY() < pitchY)) {
		//					Position a = new Position(goalPositionCoor.getX() + x, goalPositionCoor.getY() + y);
		//					GraphPoint aa = coordinatesToGrid(a);
		//					occupied.add(aa);
		//				}
		//
		//				// getting 10 more pixels in which the opposition may be sitting
		//				if ((goalPositionCoor.getX() - x > 0) && (goalPositionCoor.getY() - y > 0)) {
		//					
		//					Position b = new Position(goalPositionCoor.getX() - x, goalPositionCoor.getY() - y);
		//					GraphPoint bb = coordinatesToGrid(b);
		//					occupied.add(bb);
		//				}
		//			}
		//		}
		//		for(int index = 0; index<occupied.size(); index++){
		//			GraphPoint pp = coordinatesToGrid(new Position(goalPositionCoor.getX(), goalPositionCoor.getY()));
		//			if((occupied.get(index).getX()==pp.getX())&&(occupied.get(index).getY()==pp.getY())){
		//				System.out.println("SEXYY");
		//				occupied.remove(index);
		//				break;
		//			}
		//		}
		//System.out.println(occupied);
		// System.out.println(ourPositionGrid);
		// path = new ArrayList<GraphPoint>();
		// checked = new ArrayList<GraphPoint>();
		// occupied = new ArrayList<GraphPoint>();

		checked.add(ourPositionGrid);
		goalPositionGrid.x = goalPositionGrid.x;
		search(ourPositionGrid, goalPositionGrid);
		path = optimisePath(path);
		// System.out.println(path.get(0));

		ArrayList<Position> wayPositions = new ArrayList<Position>();
		System.out.println("PATH SIZE IS: " + path.size());
		for (int x = 0; x < path.size(); x++) {
			wayPositions.add(x, gridToCoor(path.get(x)));
		}
		// System.out.println(wayPositions.get(0));
		// System.out.println(wayPositions.size());
		for (int i = 0; i < wayPositions.size(); i++) {
			System.out.println("(" + wayPositions.get(i).getX() + ", " + wayPositions.get(i).getY() + ") ");
		} 
		int a = goalPositionCoor.getX()+displacementX;
		int b = goalPositionCoor.getY()+displacementY;
		goalPositionCoor.setX(a);
		goalPositionCoor.setY(b);
		wayPositions.add(goalPositionCoor);
		return wayPositions;
	}

	private GraphPoint coordinatesToGrid(Position a) {
		int x = (int) Math.ceil(a.getX() / gridSize);
		int y = (int) Math.ceil(a.getY() / gridSize);

		GraphPoint b = new GraphPoint(x, y);
		return b;
	}

	private Position gridToCoor(GraphPoint a) {
		int x = a.x * gridSize;
		int y = a.y * gridSize;
		a.x = x + displacementX;
		a.y = y + displacementY;

		return new Position(a.x, a.y);
	}

	private static int calcMovementCost(GraphPoint currentPosition,
			GraphPoint newPosition) {
		/*
		 * SAME THING WITH PLACING INACCESSIBLE PositionS ON THE GRID
		 */
		if (oppPositionGrid.distance(newPosition) < 9) {
			// discourage it heavily, to not crash into opponent
			return 500;
		}

		//		if ((Math.abs(gridX - newPosition.x) < 3)
		//				|| (Math.abs(gridY - newPosition.y)) < 3) {
		//			// Discourage going too close to walls
		//			return 100;
		//		}
		if (!shootingLeft) {
			if (Math.abs(newPosition.y - goalPositionGrid.y) < 6
					&& newPosition.x >= goalPositionGrid.x)
				return 65;
		}
		if (shootingLeft) {
			if (Math.abs(newPosition.y - goalPositionGrid.y) < 6
					&& newPosition.x <= goalPositionGrid.x)
				return 65;
		}
		if (oppPositionGrid.distance(newPosition) < 5) {
			// discourage Positions that are quite close to the opponent
			return 30;
		}
		if (Math.abs(oppPositionGrid.y - newPosition.y) < 5) {
			return 18;
		}
		// horizontal and vertical movements
		if (Math.abs(newPosition.x - currentPosition.x)
				+ Math.abs(newPosition.y - currentPosition.y) == 1) {
			return 10;
		}

		// diagonal movements
		if (Math.abs(newPosition.x - currentPosition.x)
				+ Math.abs(newPosition.y - currentPosition.y) == 2) {
			return 14;
		}

		return 0;

	}

	private static int calcHeuristicCost(GraphPoint currentPosition,
			GraphPoint endPosition) {
		// int xDist = (Math.abs(currentPosition.x - endPosition.x));
		// int yDist = (Math.abs(currentPosition.y - endPosition.y));
		// if (xDist > yDist) {
		// return 14 * yDist + 10*(xDist - yDist);
		// } else {
		// return 14 * xDist + 10*(yDist - xDist);
		// }
		return 10 * (Math.abs(endPosition.x - currentPosition.x) + Math
				.abs(endPosition.y - currentPosition.y));
	}

	public static void search(GraphPoint currentPosition, GraphPoint endPosition) {
		// going through a few Positions around the current position
		for (int x = currentPosition.x - 1; x < currentPosition.x + 2; x++) {
			for (int y = currentPosition.y - 1; y < currentPosition.y + 2; y++) {
				GraphPoint pt = new GraphPoint(x, y);
				// check whether grid is on the "blacklist"
				// System.out.println(occupied);
				if (!occupied.contains(pt)) {
					// check in range of grids
					if (x > 0 && y > 0 && x <= gridX && y <= gridY) {
						// if it's not already on check list, add it
						if (!checked.contains(pt)) {
							checked.add(pt); // checked means it's a valid Position
							// in which we can travel
							pt.setParent(currentPosition);
							if ((pt.x == goalPositionGrid.x)
									&& (pt.y == goalPositionGrid.y)) {
								goalPositionGrid.setParent(currentPosition);
							}
							// the distance to reach the parent + the distance
							// from the parent to the current Position
							// getmovementcost - the cost to reach the Position
							// calcmovementcost - calc the distance between 2
							// Positions
							pt.setMovementCost(pt.getParent().getMovementCost()
									+ calcMovementCost(currentPosition, pt));
							// computing the distance from pt onwards to the
							// goal
							pt.setHeuristicCost(calcHeuristicCost(pt, endPosition));
							// the total distance is the distance to reach pt +
							// the distance remained to goal
							pt.setTotalCost(pt.getMovementCost()
									+ pt.getHeuristicCost());
						}
						// if it's already in my list of ok Positions
						if (checked.contains(pt)) {
							// if there is an easier way of reaching pt by
							// considering currentPosition as parent
							if (pt.getMovementCost() > calcMovementCost(
									currentPosition, pt)) {
								pt.setParent(currentPosition);
								pt.setMovementCost(pt.getParent()
										.getMovementCost()
										+ calcMovementCost(currentPosition, pt));
								pt.setTotalCost(pt.getMovementCost()
										+ pt.getHeuristicCost());
							}
						}
					}
				}
			}
		}
		// sorts checked Positions by cost to reach them
		checked = comparator.sortGridPoints(checked);
		if (checked.size() > 0) {
			GraphPoint closestPt = checked.get(0);

			checked.remove(closestPt);
			if (closestPt.y == goalPositionGrid.y
					&& closestPt.x == goalPositionGrid.x)
				// System.out.println("CLOSEST Position X AND GOAL X :" +
				// closestPt.x + " " + goalPositionGrid.x );
				System.out.println("CLOSEST Position Y AND GOAL Y :" + closestPt.y
						+ " " + goalPositionGrid.y);
			if ((closestPt.x == goalPositionGrid.x)
					&& (closestPt.y == goalPositionGrid.y)) {
				tracePath(ourPositionGrid, goalPositionGrid);
			} else {
				occupied.add(closestPt);
				search(closestPt, endPosition);
			}
		} else
			return;
	}

	private static void tracePath(GraphPoint startPosition, GraphPoint endPosition) {
		// System.out.println("IT REACHED TRACEPATH");
		path.add(0, endPosition);
		if (endPosition.getParent() != null) {
			tracePath(startPosition, endPosition.getParent());
		}
	}

	private ArrayList<GraphPoint> optimisePath(ArrayList<GraphPoint> path) {

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
		for(int x = 0; x<newPath.size()-1;x++){//removes points that are too close to each other
			if(Math.abs(newPath.get(x).getX()-newPath.get(x+1).getX())<15){
				if(Math.abs(newPath.get(x).getY()-newPath.get(x+1).getY())<15){
					newPath.remove(x+1);
				}
			}
		}
		if(ourPositionCoor.getX() - gridToCoor(newPath.get(0)).getX() < 20 && ourPositionCoor.getY() - gridToCoor(newPath.get(0)).getY() < 20) {
			newPath.remove(0);
		}
		return newPath;
	}

	private static double getAngle(GraphPoint a, GraphPoint b) {
		return Math.toDegrees(Math.atan2((a.y - b.y), (b.x - a.x)));
	}

	// public static GraphPoint getGoalPosition(GraphPoint ballPosition){
	//
	// newGoalGrid = new GraphPoint(ballPosition.x,ballPosition.y);
	//
	// if(shootingLeft){
	// //x po golqmo
	// newGoalGrid.x = ballPosition.x-2;
	//
	// if(ballPosition.y>gridY/2){
	//
	// }
	//
	// }
	//
	// }

}
