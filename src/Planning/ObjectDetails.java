package Planning;
import JavaVision.*;


public abstract class ObjectDetails {

	protected Position coors;
	protected float angle;
	
	/**
	 * Getters and Setters for coors and angle
	 */
	
	public Position getCoors() {
		return coors;
	}

	public float getAngle() {
		return angle;
	}

	public void setCoors(Position coors) {
		this.coors = coors;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}
}
