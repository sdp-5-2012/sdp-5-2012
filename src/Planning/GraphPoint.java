package Planning;

import java.awt.Point;

@SuppressWarnings("serial")
public class GraphPoint extends Point{

	GraphPoint parent;
	int movementCost;
	int heuristicCost;
	int totalCost;

	public GraphPoint(int x,int y) {
		super(x,y);
		movementCost = 0;
		heuristicCost = 0;
		totalCost = 0;
	}

	public void setParent(GraphPoint parent) {
		this.parent = parent;
	}
	public GraphPoint getParent() {
		return parent;
	}

	public void setMovementCost(int cost) {
		this.movementCost = cost;
	}

	public int getMovementCost() {
		return movementCost;
	}

	public void setHeuristicCost(int cost) {
		this.heuristicCost = cost;
	}

	public int getHeuristicCost() {
		return heuristicCost;
	}

	public void setTotalCost(int cost) {
		this.totalCost = cost;
	}

	public int getTotalCost() {
		return totalCost;
	}
}