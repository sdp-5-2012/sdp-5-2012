package Planning;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.concurrent.atomic.AtomicBoolean;
import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import GUI.MainGui;
import JavaVision.*;

public class Runner extends Thread {

	// Objects
	public static Robot nxt;
	public static Robot otherRobot;
	public static Robot blueRobot;
	public static Robot yellowRobot;
	public WorldState state;
	public ControlGUI control;
	public static Runner instance = null;
	public static Vision vision;
	public static MainGui gui;
	FieldPositions fieldPositions;

	// game flags
	boolean teamYellow = false;
	public boolean attackLeft = false;
	public boolean isPenaltyAttack = false;
	public boolean isPenaltyDefend = false;
	public boolean isMainPitch = false;
	public String constantsLocation;
	public int currentCamera = 0;

	public static void main(String args[]) {
		instance = new Runner();
	}

	/**
	 * start the planning thread
	 */
	public Runner() {
		start();
	}

	/**
	 * Planning thread which begins planning loop
	 */
	public void run() {
		blueRobot = new Robot();
		yellowRobot = new Robot();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGui();
			}
		});

		// Planning sleeps until GUI notifies
		synchronized (instance) {
			try {
				instance.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		getUserOptions();
		setPositionInformation();
		setRobotColour();

		begin();
	}

	/**
	 * Sets up the planning thread then makes this
	 * thread wait until GUI interrupts
	 */
	private void begin() {
		Planning planning = Planning.createBuilder()
		.setRunnerInstance(instance)
		.setAttackLeft(attackLeft)
		.setFieldPositions(fieldPositions)
		.setPenaltyAttack(isPenaltyAttack)
		.setPenaltyDefend(isPenaltyDefend)
		.setRobot(nxt)
		.setOtherRobot(otherRobot)
		.setState(state)
		.setTeamYellow(teamYellow)
		.build();

		Thread p = new Thread(planning);
		p.start();

		while(true) {
			// Planning sleeps until GUI notifies
			synchronized (instance) {
				try {
					instance.wait();
				} catch (InterruptedException e) {
					// Tell planning to stop
					planning.setStopFlag(true);
					// Wait for thread to terminate
					try {
						p.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					// Wait for start button to be pressed again
					try {
						instance.wait();
					} catch (InterruptedException e2) {
						e.printStackTrace();
					}

					// Update game information and initiate a new Planning object
					updateInput();

					planning = Planning.createBuilder()
					.setRunnerInstance(instance)
					.setAttackLeft(attackLeft)
					.setFieldPositions(fieldPositions)
					.setPenaltyAttack(isPenaltyAttack)
					.setPenaltyDefend(isPenaltyDefend)
					.setRobot(nxt)
					.setOtherRobot(otherRobot)
					.setState(state)
					.setTeamYellow(teamYellow)
					.build();
					
					// Make new thread and begin
					p = new Thread(planning);
					p.start();

					continue;
				}
			}
		}
	}

	/**
	 * Method to set which goal is ours and theirs
	 */
	public void setPositionInformation() {
		if (attackLeft) {
			fieldPositions.setOurGoal(fieldPositions.getRightGoal());
			fieldPositions.setTheirGoal(fieldPositions.getLeftGoal());
		} else {
			fieldPositions.setOurGoal(fieldPositions.getLeftGoal());
			fieldPositions.setTheirGoal(fieldPositions.getRightGoal());
		}
	}

	public void getUserOptions() {
		teamYellow = gui.getTeam();
		attackLeft = gui.getAttackLeft();
		isPenaltyAttack = gui.getIsPenaltyAttack();
		isPenaltyDefend = gui.getIsPenaltyDefend();
		isMainPitch = gui.getIsMainPitch();
	}

	public void createAndShowGui() {
		// Get the size of the default screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		// Set the the control gui
		gui = new MainGui(instance);
		gui.setSize(650, 400);
		gui.setLocation((int) (dim.getWidth()- 650), 500);
		gui.setTitle("N.U.K.E Control Panel");
		gui.setResizable(false);
		gui.setVisible(true);
	}

	/**
	 * Method to initiate the vision
	 */
	public void startVision() {
		/**
		 * Creates the control GUI, and initialises the image processing.
		 *
		 * @param args
		 * Program arguments. Not used.
		 */
		// Get constants location from gui
		constantsLocation = gui.getConstantsFileLocation();
		WorldState worldState = new WorldState();
		ThresholdsState thresholdsState = new ThresholdsState();

		/* Default to main pitch. */
		PitchConstants pitchConstants = new PitchConstants(constantsLocation);

		// The buffer values should be obtained from here, and
		// the top/bottom/left/right walls set as appropriate.
		// Strip values also set.
		int topY = pitchConstants.topBuffer;
		int lowY = 480 - pitchConstants.bottomBuffer;
		int leftX = pitchConstants.leftBuffer;
		int rightX = 640 - pitchConstants.rightBuffer;

		int strip1 = topY + 50;
		int strip6 = lowY - 50;

		int stripDifference = strip6 - strip1;

		int strip2 = strip1 + (stripDifference / 5);
		int strip3 = strip1 + ((stripDifference / 5) * 2);
		int strip4 = strip1 + ((stripDifference / 5) * 3);
		int strip5 = strip1 + ((stripDifference / 5) * 4);

		int cornerTopLeftX     = leftX + 90;
		int cornerBottomLeftX  = leftX + 90;
		int cornerTopLeftY     = topY + 70;
		int cornerTopRightY    = topY + 70;
		int cornerBottomLeftY  = lowY - 70;
		int cornerBottomRightY = lowY - 70;
		int cornerTopRightX    = rightX - 90;
		int cornerBottomRightX = rightX - 90;

		Position pitchCentre = new Position((int) (leftX + ((rightX - leftX) * 0.5)), (int) (topY + ((lowY - topY) * 0.5)));
		Position leftGoal = new Position(leftX, (int) (topY + (topY - lowY) * 0.5));
		Position rightGoal = new Position(rightX, (int) (topY + (topY - lowY) * 0.5));

		fieldPositions = FieldPositions.createBuilder()
		.leftGoal(leftGoal)
		.rightGoal(rightGoal)
		.pitchCentre(pitchCentre)
		.setCornerBottomLeftX(cornerBottomLeftX)
		.setCornerBottomLeftY(cornerBottomLeftY)
		.setCornerBottomRightX(cornerBottomRightX)
		.setCornerBottomRightY(cornerBottomRightY)
		.setCornerTopLeftX(cornerTopLeftX)
		.setCornerBottomRightY(cornerBottomRightY)
		.setCornerTopLeftX(cornerTopLeftX)
		.setCornerTopLeftY(cornerTopLeftY)
		.setCornerTopRightX(cornerTopRightX)
		.setCornerTopRightY(cornerTopRightY)
		.setLeftX(leftX)
		.setLowY(lowY)
		.setRightX(rightX)
		.setStrip1(strip1)
		.setStrip2(strip2)
		.setStrip3(strip3)
		.setStrip4(strip4)
		.setStrip5(strip5)
		.setStrip6(strip6)
		.setTopY(topY)
		.build();

		if (attackLeft) {
			fieldPositions.setOurGoal(rightGoal);
			fieldPositions.setTheirGoal(leftGoal);
		} else {
			fieldPositions.setOurGoal(leftGoal);
			fieldPositions.setTheirGoal(rightGoal);
		}

		// endofadd

		control = new ControlGUI(thresholdsState, worldState, pitchConstants);
		control.initGUI();

		/* Default values for the main vision window. */
		String videoDevice = "/dev/video0";
		int width = 640;
		int height = 480;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 80;

		try {
			/* Create a new Vision object to serve the main vision window. */
			vision = new Vision(videoDevice, width, height, channel,
					videoStandard, compressionQuality, worldState,
					thresholdsState, pitchConstants, currentCamera, isMainPitch);

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method stops robot and waits for new input from GUI
	 */
	public void updateInput() {
		// Planning sleeps until Gui notifies		
		getUserOptions();
		setPositionInformation();
		setRobotColour();
	}

	public void setRobotColour() {
		if (teamYellow) {
			nxt = yellowRobot;
			otherRobot = blueRobot;

		} else {
			nxt = blueRobot;
			otherRobot = yellowRobot;
		}
	}

	public void setTeamYellow(boolean team) {
		teamYellow = team;
	}

	public void setCurrentCamera(int camera) {
		currentCamera = camera;
	}

	public void setIsMainPitch(boolean pitch) {
		isMainPitch = pitch;
	}


	public WorldState getWorldState() {
		return state;
	}

	public void updateGuiLabels(Position nxt, Position otherRobot, Position ball) {
		gui.setCoordinateLabels(nxt, otherRobot, ball);
	}
}