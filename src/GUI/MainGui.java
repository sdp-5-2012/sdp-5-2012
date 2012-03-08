package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import JavaVision.PitchConstants;
import JavaVision.Position;
import JavaVision.ThresholdsState;
import JavaVision.WorldState;
import Planning.Runner;


public class MainGui extends JFrame {
	public GuiLog log;
	JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	public OptionsPanel options;
	String fileDir = new String("");
	JFileChooser chooser;
	File constantsFile;
	//	volatile boolean applyClicked = false;
	//	volatile boolean isYellow = true;
	Runner runner;

	// values Runner needs
	boolean attackLeft = true;
	boolean isYellow = true;
	//int currentMode = 0;	// 0: Normal, 1: Penalty Defend, 2: Penalty Attack
	boolean isPenaltyAttack = false;
	boolean isPenaltyDefend = false;
	String constantsLocation;
	boolean isMainPitch = false;
	boolean isModeAvoid = true;


	public MainGui(Runner runner) {
		this.runner = runner;
	
		// default constants is pitch0
		constantsLocation = getClass().getClassLoader().getResource(".").getPath();
		String src = constantsLocation.substring(0, constantsLocation.length()-4);
		src = src + "src/JavaVision/constants/pitch0";
		constantsLocation = src;
		
		getContentPane().setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addMenu();

		log = new GuiLog();
		options = new OptionsPanel();
		options.setAlignmentX(LEFT_ALIGNMENT);
		topPanel.add(options);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, log);
		getContentPane().add(splitPane);
		splitPane.setDividerLocation(200);

		addListeners();
		log.setCurrentPitchConstants("pitch0");
		
		pack();
	}

	private void addMenu() {

		// Creates a menubar for a JFrame
		JMenuBar menuBar = new JMenuBar();

		setJMenuBar(menuBar);
		JMenu importMenu = new JMenu("Import");

		menuBar.add(importMenu);
		JMenuItem loadConstants = new JMenuItem("Load Constants");

		importMenu.add(loadConstants);		

		// Add Listener for load constants menu button
		loadConstants.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame frame = new JFrame();
				constantsLocation = "";
				// Create a file chooser and default to constants folder
				constantsLocation = getClass().getClassLoader().getResource(".").getPath();
				String src = constantsLocation.substring(0, constantsLocation.length()-4);
				src = src + "src/JavaVision/constants";
				constantsLocation = src;

				JFileChooser fc = new JFileChooser(new File(src));
				// Create the actions				
				fc.showOpenDialog(frame);
				constantsFile = fc.getSelectedFile();
				constantsLocation += "/" + constantsFile.getName();

//				int pitchNumber = (constantsFile.getName() == "pitch0") ? 0 : 1;
//				controls.setNewConstants(constantsLocation, pitchNumber);
				
				log.setCurrentPitchConstants(constantsFile.getName());
			}
		});        
	}

	public void addListeners() {
		// Connect Listener
		log.connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (options.yellowRobotButton.isSelected()) {
					runner.setTeamYellow(true);
				} else {
					runner.setTeamYellow(false);
				}
				
				runner.setRobotColour();

				// Repeatedly try and make connection
				while (!Runner.nxt.startCommunications()) {
					log.setIsConnected(false);
				}

				log.setIsConnected(true);
				// Start vision
				runner.startVision();
				log.startStop.setEnabled(true);
				log.connect.setEnabled(false);
				log.apply.setEnabled(true);

			}
		});

		// Start Listener
		log.startStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (log.startStop.getText() == "Start") {
					log.startStop.setText("Stop");
					updateButtons();
					runner.setStopFlag(false);
					// Wake runner
					synchronized (runner) {
						runner.notify();
					}
				} else if(log.startStop.getText() == "Stop") {
					runner.setStopFlag(true);
					log.apply.setEnabled(false);
					log.startStop.setText("Start");
					Runner.nxt.stop();
				}
			}
		});


		log.apply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				runner.setStopFlag(true);
				runner.applyChanges();
			}
		});
	}

	/**
	 * Method to update game values based on values in GUI
	 */
	void updateButtons() {
		// Case for colour options
		if(options.yellowRobotButton.isSelected()) {
			log.setCurrentColour("Yellow");
			isYellow = true;
		} else if(options.blueRobotButton.isSelected()) {
			log.setCurrentColour("Blue");
			isYellow = false;
		} 

		// Case for attack options
		if(options.attackLeft.isSelected()) {
			log.setCurrentAttackGoal("Left");
			attackLeft = true;
		} else if(options.attackRight.isSelected()) {
			log.setCurrentAttackGoal("Right");
			attackLeft = false;
		} 

		// Case for mode options
		if(options.penaltyAttack.isSelected()) {
			log.setCurrentMode("Penalty Attack");
			isPenaltyAttack = true;
		} else if(options.penaltyDefend.isSelected()) {
			log.setCurrentMode("Penalty Defend");
			isPenaltyDefend = true;
		} else if (options.normal.isSelected()) {
			log.setCurrentMode("Normal");
			isPenaltyAttack = false;
			isPenaltyDefend = false;
		}

		// Case for pitch
		if(options.pitchMain.isSelected()) {
			isMainPitch = true;
		} else {
			isMainPitch = false;
		}
	}

	// Getters
	//	public boolean getApplyClicked() {
	//		return applyClicked;
	//	}

	public boolean getTeam() {
		return isYellow;
	}

	public boolean getAttackLeft() {
		return attackLeft;
	}

	public boolean getIsPenaltyAttack() {
		return isPenaltyAttack;
	}

	public boolean getIsPenaltyDefend() {
		return isPenaltyDefend;
	}

	public boolean getIsMainPitch() {
		return isMainPitch;
	}

	public String getConstantsFileLocation() {
		return constantsLocation;
	}
	
	public boolean isModeAvoid() {
		return isModeAvoid;
	}
	
	public void setCoordinateLabels(Position ourRobot, Position enemyRobot, Position ball) {
		log.setOurCoors(ourRobot);
		log.setEnemyCoors(enemyRobot);
		log.setBallCoors(ball);
	}
}
