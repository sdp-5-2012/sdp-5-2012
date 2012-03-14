package GUI;
import java.awt.GridLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

@SuppressWarnings("serial")
public class OptionsPanel extends JPanel {

	// Components to be added to the control panel
	// Option labels
	private JLabel robotColour = new JLabel("Robot Colour:");
	private JLabel attackGoal = new JLabel("Goal to attack:");
	private JLabel mode = new JLabel("Mode:");
	private JLabel pitchChoice = new JLabel("Pitch Choice:");
	private JLabel cameraChoice = new JLabel("Camera Choice:");
		
	// Radio buttons
	public JRadioButton yellowRobotButton;
	public JRadioButton blueRobotButton;
	public JRadioButton attackLeft;
	public JRadioButton attackRight;
	public JRadioButton penaltyAttack;
	public JRadioButton penaltyDefend ;
	public JRadioButton normal;
	public JRadioButton pitchMain;
	public JRadioButton pitchSide;
	public JRadioButton cameraZero;
	public JRadioButton cameraOne;
	public JRadioButton cameraTwo;
	
	// Button Groups
	private ButtonGroup colourGroup;
	private ButtonGroup attackGroup;
	private ButtonGroup modeGroup;
	private ButtonGroup pitchChoiceGroup;
	private ButtonGroup cameraChoiceGroup;

	/** Constructor */
	public OptionsPanel() {

		setLayout(new GridLayout(5,4));

		// robot colour
		yellowRobotButton = new JRadioButton("Yellow");
		blueRobotButton = new JRadioButton("Blue");
		// group together
		colourGroup = new ButtonGroup();
		colourGroup.add(yellowRobotButton);
		colourGroup.add(blueRobotButton);
		// add to panel
		add(yellowRobotButton);
		add(blueRobotButton);

		// attack direction
		attackLeft = new JRadioButton("Left");
		attackRight = new JRadioButton("Right");
		// group together
		attackGroup = new ButtonGroup();
		attackGroup.add(attackLeft);
		attackGroup.add(attackRight);

		// Robot mode
		penaltyAttack = new JRadioButton("Penalty Attack");
		penaltyDefend = new JRadioButton("Penalty Defend");
		normal = new JRadioButton("Normal");
		// group together
		modeGroup = new ButtonGroup();
		modeGroup.add(penaltyAttack);
		modeGroup.add(penaltyDefend);
		modeGroup.add(normal);

		// Set the default selection
		yellowRobotButton.setSelected(true);
		attackRight.setSelected(true);
		normal.setSelected(true);
		
		//Pitch choice
		pitchMain = new JRadioButton("Main");
		pitchSide = new JRadioButton("Side");
		//Group together
		pitchChoiceGroup = new ButtonGroup();
		pitchChoiceGroup.add(pitchMain);
		pitchChoiceGroup.add(pitchSide);
		
		pitchMain.setSelected(true);
		
		// Camera Choice
		cameraZero = new JRadioButton("0 (side pitch)");
		cameraOne = new JRadioButton("1 (upper main)");
		cameraTwo = new JRadioButton("2 (lower main)");
		cameraZero.setSelected(true);
		
		// Group together
		cameraChoiceGroup = new ButtonGroup();
		cameraChoiceGroup.add(cameraZero);
		cameraChoiceGroup.add(cameraOne);
		cameraChoiceGroup.add(cameraTwo);
				
		// Add all components
		add(robotColour);
		add(yellowRobotButton);
		add(blueRobotButton);
		add(new JLabel("")); // Blank space 
		add(attackGoal);
		add(attackLeft);
		add(attackRight);
		add(new JLabel("")); // Blank space
		add(mode);
		add(normal);
		add(penaltyDefend);
		add(penaltyAttack);
		add(pitchChoice);
		add(pitchMain);
		add(pitchSide);
		add(new JLabel(""));
		add(cameraChoice);
		add(cameraZero);
		add(cameraOne);
		add(cameraTwo);
	}
}
