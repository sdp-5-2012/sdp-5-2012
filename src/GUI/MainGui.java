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
	int currentMode = 0;	// 0: Normal, 1: Penalty Defend, 2: Penalty Attack
	String constantsLocation;
	
	
	public MainGui(Runner runner) {
		this.runner = runner;

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
		
		// default constants is pitch0
		constantsLocation = getClass().getClassLoader().getResource(".").getPath()+ "src/JavaVision/constants/pitch0";
		log.setCurrentPitchConstants("pitch0");
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

				// Create a file chooser and default to constants folder
				constantsLocation = getClass().getClassLoader().getResource(".").getPath();
				String src = constantsLocation.substring(0, constantsLocation.length()-4);
				src = src + "src/JavaVision/constants";

				JFileChooser fc = new JFileChooser(new File(src));
				// Create the actions				
				fc.showOpenDialog(frame);
				constantsFile = fc.getSelectedFile();
				log.setCurrentPitchConstants(constantsFile.getName());


			}
		});        
	}

	public void addListeners() {
		// Start Vision Listener
		log.start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.start.setEnabled(false);
				log.apply.setEnabled(true);
				updateButtons();
				// Wake runner
				synchronized (runner) {
					runner.notify();
				}		
			}
		});
		
		log.apply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateButtons();
			}
		});
	}
	
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
			currentMode = 2;
		} else if(options.penaltyDefend.isSelected()) {
			log.setCurrentMode("Penalty Defend");
			currentMode = 1;
		} else if (options.normal.isSelected()) {
			log.setCurrentMode("Normal");
			currentMode = 0;
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
	
	public int getMode() {
		return currentMode;
	}
}
