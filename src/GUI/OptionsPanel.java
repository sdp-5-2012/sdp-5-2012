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

class OptionsPanel extends JPanel {

	// Components to be added to the control panel
	// Option labels
	JLabel robotColour = new JLabel("Robot Colour:");
	JLabel attackGoal = new JLabel("Goal to attack:");
	JLabel mode = new JLabel("Mode:");
	
	// Radio buttons
	JRadioButton yellowRobotButton;
	JRadioButton blueRobotButton;
	JRadioButton attackLeft;
	JRadioButton attackRight;
	JRadioButton penaltyAttack;
	JRadioButton penaltyDefend ;
	JRadioButton normal;
	
	// Button Groups
	ButtonGroup colourGroup;
	ButtonGroup attackGroup;
	ButtonGroup modeGroup;
	
	//	JPanel robotColourPanel = new JPanel();
	//	JPanel attackGoalPanel = new JPanel();
	//	JPanel modePanel = new JPanel(new FlowLayout(0, 5, 0));

	/** Constructor */
	public OptionsPanel() {

		setLayout(new GridLayout(3,4));
		//		robotColourPanel.add(robotColour);
		//		attackGoalPanel.add(attackGoal);
		//		modePanel.add(mode);

		// robotColourPanel radio buttons
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
		//		robotColourPanel.add(yellowRobotButton);
		//		robotColourPanel.add(blueRobotButton);

		// attackGoalPanel radio buttons
		// robot colour
		attackLeft = new JRadioButton("Left");
		attackRight = new JRadioButton("Right");

		// group together
		attackGroup = new ButtonGroup();
		attackGroup.add(attackLeft);
		attackGroup.add(attackRight);
		// add to panel
		//		attackGoalPanel.add(attackLeft);
		//		attackGoalPanel.add(attackRight);

		//		robotColourPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		//		attackGoalPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		// modePanel radio buttons
		// robot colour
		penaltyAttack = new JRadioButton("Penalty Attack");
		penaltyDefend = new JRadioButton("Penalty Defend");
		normal = new JRadioButton("Normal");
		// group together
		modeGroup = new ButtonGroup();
		modeGroup.add(penaltyAttack);
		modeGroup.add(penaltyDefend);
		modeGroup.add(normal);

		// add to panel
		//		modePanel.add(penaltyAttack);
		//modePanel.add(penaltyDefend);
		//modePanel.add(normal);
		yellowRobotButton.setSelected(true);
		attackRight.setSelected(true);
		normal.setSelected(true);
		
		// Add all components
		add(robotColour);
		add(yellowRobotButton);
		add(blueRobotButton);
		add(new JLabel(""));
		add(attackGoal);
		add(attackLeft);
		add(attackRight);
		add(new JLabel(""));
		add(mode);
		add(penaltyAttack);
		add(penaltyDefend);
		add(normal);

		// add components to control panel
		//		add(robotColourPanel);
		//		add(attackGoalPanel);
		//		add(modePanel);
	}
}


