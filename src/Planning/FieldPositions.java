package Planning;

import JavaVision.Position;

public class FieldPositions {
	// Positions
	public Position pitchCentre = null;
	public Position leftGoalMain = new Position(40, 250);
	public Position leftGoalSide = new Position(62, 233);
	public Position rightGoalMain = new Position(608, 242);
	public Position rightGoalSide = new Position(567, 238);
	public Position centreMain = new Position(284, 246);
	public Position centreSide = new Position(253, 236);


	/*
	 * These following values should really be read from the pitch constants, if possible.
	 * That would mean they wouldn't be explicitly stated here but would get calculated
	 * from the buffer values in the constants files
	 */
	public Position leftGoal = null;
	public Position rightGoal = null;
	
	public Position ourGoal = null;
	public Position theirGoal = null;

	public int topY = 0;
	public int lowY = 0;
	public int leftX = 0;
	public int rightX = 0;

	public int strip1 = 0;
	public int strip2 = 0;
	public int strip3 = 0;
	public int strip4 = 0;
	public int strip5 = 0;
	public int strip6 = 0;

	public int cornerTopLeftX = 0;
	public int cornerTopLeftY = 0;
	public int cornerTopRightX = 0;
	public int cornerTopRightY = 0;
	public int cornerBottomLeftX = 0;
	public int cornerBottomLeftY = 0;
	public int cornerBottomRightX = 0;
	public int cornerBottomRightY = 0;


	private FieldPositions() {}

	public static Builder createBuilder() {
		return new Builder();
	}

	public void setOurGoal(Position ourGoal) {
		this.ourGoal = ourGoal;
	}
	
	public void setTheirGoal(Position theirGoal) {
		this.theirGoal = theirGoal;
	}
	
		
	public Position getLeftGoal() {
		return leftGoal;
	}

	public Position getRightGoal() {
		return rightGoal;
	}

	/*
	 * Builder
	 */ 
	 public static class Builder {
		 private final FieldPositions obj = new FieldPositions();
		 private boolean done;

		 private Builder() {}

		 public FieldPositions build() {
			 done = true;
			 return obj;
		 }
		 
		 public Builder leftGoal(Position leftGoal) {
			 check();
			 obj.leftGoal = leftGoal;
			 return this;
		 }
		 
		 public Builder rightGoal(Position rightGoal) {
			 check();
			 obj.rightGoal = rightGoal;
			 return this;
		 }
		 
		 public Builder pitchCentre(Position pitchCentre) {
			 check();
			 obj.pitchCentre = pitchCentre;
			 return this;
		 }

		 public Builder setTopY(int topY) {
			 check();
			 obj.topY = topY;
			 return this;
		 }

		 public Builder setLowY(int lowY) {
			 check();
			 obj.lowY = lowY;
			 return this;
		 }

		 public Builder setLeftX(int leftX) {
			 check();
			 obj.leftX = leftX;
			 return this;
		 }

		 public Builder setRightX(int rightX) {
			 check();
			 obj.rightX = rightX;
			 return this;
		 }

		 public Builder setStrip1(int strip1) {
			 check();
			 obj.strip1 = strip1;
			 return this;
		 }

		 public Builder setStrip2(int strip2) {
			 check();
			 obj.strip2 = strip2;
			 return this;
		 }

		 public Builder setStrip3(int strip3) {
			 check();
			 obj.strip3 = strip3;
			 return this;
		 }

		 public Builder setStrip4(int strip4) {
			 check();
			 obj.strip4 = strip4;
			 return this;
		 }

		 public Builder setStrip5(int strip5) {
			 check();
			 obj.strip5 = strip5;
			 return this;
		 }

		 public Builder setStrip6(int strip6) {
			 check();
			 obj.strip6 = strip6;
			 return this;
		 }

		 public Builder setCornerTopLeftX(int cornerTopLeftX) {
			 check();
			 obj.cornerTopLeftX = cornerTopLeftX;
			 return this;
		 }

		 public Builder setCornerTopLeftY(int cornerTopLeftY) {
			 check();
			 obj.cornerTopLeftY = cornerTopLeftY;
			 return this;
		 }

		 public Builder setCornerTopRightX(int cornerTopRightX) {
			 check();
			 obj.cornerTopRightX = cornerTopRightX;
			 return this;
		 }

		 public Builder setCornerTopRightY(int cornerTopRightY) {
			 check();
			 obj.cornerTopRightY = cornerTopRightY;
			 return this;
		 }

		 public Builder setCornerBottomLeftX(int cornerBottomLeftX) {
			 check();
			 obj.cornerBottomLeftX = cornerBottomLeftX;
			 return this;
		 }

		 public Builder setCornerBottomLeftY(int cornerBottomLeftY) {
			 check();
			 obj.cornerBottomLeftY = cornerBottomLeftY;
			 return this;
		 }

		 public Builder setCornerBottomRightX(int cornerBottomRightX) {
			 check();
			 obj.cornerBottomRightX = cornerBottomRightX;
			 return this;
		 }

		 public Builder setCornerBottomRightY(int cornerBottomRightY) {
			 check();
			 obj.cornerBottomRightY = cornerBottomRightY;
			 return this;
		 }

		 private void check() {
			 if (done)
				 throw new IllegalArgumentException("Do use other builder to create new instance");
		 }
	 }
}