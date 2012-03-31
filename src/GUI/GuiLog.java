package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import JavaVision.Position;

@SuppressWarnings("serial")
public class GuiLog extends JPanel {

	// Components to be added to the control panel
	private JTextArea logging;
	private Position ourCoords = new Position(0, 0);
	private Position enemyCoords = new Position(0, 0);
	private Position ballCoords = new Position(0, 0);

	JPanel buttons = new JPanel(new GridLayout(2, 1));
	JButton startStop = new JButton("Start");
	JButton connect = new JButton("Connect");

	private String currentPitchConstants = "...";
	private String currentColour ="Yellow";
	private String currentAttackGoal ="Right";
	private String currentMode = "Normal";
	private int currentCamera = 1;

	private boolean isConnected = false;

	private String ballCoordsStr ="";
	private String ourCoordsStr = "";
	private String enemyCoordsStr = "";

	/** Constructor */
	public GuiLog() {
		setBackground(Color.gray);
		setLayout(new BorderLayout(5,5));

		// Log text area
		logging = new JTextArea(5,35);
		JScrollPane scrollPane = new JScrollPane(logging);
		logging.setForeground(Color.GREEN);
		logging.setBackground(Color.BLACK);
		logging.setEditable(false);
		logging.setTabSize(3);

		ballCoordsStr = positionToString(ballCoords);
		ourCoordsStr = positionToString(ourCoords);
		enemyCoordsStr = positionToString(enemyCoords);
		updateJTextArea();

		// apply and start disabled at launch
		startStop.setEnabled(false);

		// Add buttons to buttons JPanel
		buttons.add(connect);
		buttons.add(startStop);

		// add components to control panel
		add(scrollPane, BorderLayout.WEST);
		add(buttons);
	}

	// Setters for the strings for the log
	public void setCurrentColour(String currentColour) {
		this.currentColour = currentColour;
		updateJTextArea();
	}

	public void setCurrentAttackGoal(String currentAttackGoal) {
		this.currentAttackGoal = currentAttackGoal;
		updateJTextArea();
	}


	public void setCurrentMode(String currentMode) {
		this.currentMode = currentMode;
		updateJTextArea();
	}


	public void setBallCoors(Position newBallCoords) {
		ballCoordsStr = positionToString(newBallCoords);
		updateJTextArea();
	}

	public void setOurCoors(Position newOurCoords) {
		ourCoordsStr = positionToString(newOurCoords);
		updateJTextArea();
	}


	public void setEnemyCoors(Position newEnemyCoords) {
		enemyCoordsStr = positionToString(newEnemyCoords);
		updateJTextArea();
	}


	public void setIsConnected(boolean status) {
		isConnected = status;
		updateJTextArea();
	}

	public void setCurrentPitchConstants(String newPitchConstants) {
		currentPitchConstants = newPitchConstants;
		updateJTextArea();
	}
	
	public void setCurrentCamera(int camera) {
		currentCamera = camera;
		updateJTextArea();
	}


	/**
	 * Return a string from a Position
	 * @param p - The Position to create String from
	 * @return - The Position as a String
	 */
	public String positionToString(Position p) {
		return ("(" + p.getX() + ", " + p.getY() + ")");
	}

	/**
	 * Method to update the logging text area
	 */
	public void updateJTextArea() {
		logging.setText("Ball Coords:\t" + ballCoordsStr + "\t\t" + "Current Colour: "+ currentColour + "\n" +
				"Our Coords:\t" + ourCoordsStr + "\t\t" + "Current Attack Goal: "+ currentAttackGoal +"\n" +
				"Enemy Coords:\t" + enemyCoordsStr + "\t\t" + "Current Mode: "+ currentMode + "\n\n" +
				"Connection Status:\t" + connectionStatus() + "\n" +
				"Loaded Constants:\t" + currentPitchConstants + "\n" +
				"Loaded Camera:\t" + "Camera " + currentCamera);
	}

	public String connectionStatus() {
		if (isConnected) {
			return "Connected";
		} else {
			return "Not Connected";
		}
	}
}
