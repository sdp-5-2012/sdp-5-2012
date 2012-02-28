package GUI;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class OptionsPanel extends JPanel {

	// Components to be added to the control panel
	// Option labels
	JLabel robotColour = new JLabel("Robot Colour:");
	JLabel attackGoal = new JLabel("Goal to attack:");
	JLabel mode = new JLabel("Mode:");
	JLabel pitchChoice = new JLabel("Pitch Choice:");
	
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
	
	// Button Groups
	ButtonGroup colourGroup;
	ButtonGroup attackGroup;
	ButtonGroup modeGroup;
	ButtonGroup pitchChoiceGroup;

	/** Constructor */
	public OptionsPanel() {

		setLayout(new GridLayout(4,4));

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
	}
}


