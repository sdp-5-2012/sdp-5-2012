package Planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GraphPointComparator implements Comparator {
	
	public int compare(Object o1, Object o2) {
		if (((GraphPoint) o1).getTotalCost() < ((GraphPoint) o2).getTotalCost())
			return -1;
		else
			return 1;
	}
	
	public ArrayList<GraphPoint> sortGridPoints(ArrayList<GraphPoint> grids) {
		Collections.sort(grids, this);
		return grids;
	}
}
