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


public class MainGui extends JFrame {
	GuiLog log;
	JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	OptionsPanel options;
	String fileDir = new String("");
	JFileChooser chooser;
	File constantsFile;



	public MainGui() {


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
				String filename = getClass().getClassLoader().getResource(".").getPath();
				String src = filename.substring(0, filename.length()-4);
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
		log.startVision.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Start Vision");				
			}
		});

		log.apply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// Case for colour options
				if(options.yellowRobotButton.isSelected()) {
					log.setCurrentColour("Yellow");
				} else if(options.blueRobotButton.isSelected()) {
					log.setCurrentColour("Blue");
				} 

				// Case for attack options
				if(options.attackLeft.isSelected()) {
					log.setCurrentAttackGoal("Left");
				} else if(options.attackRight.isSelected()) {
					log.setCurrentAttackGoal("Right");
				} 

				// Case for mode options
				if(options.penaltyAttack.isSelected()) {
					log.setCurrentMode("Penalty Attack");
				} else if(options.penaltyDefend.isSelected()) {
					log.setCurrentMode("Penalty Defend");
				} else if (options.normal.isSelected()) {
					log.setCurrentMode("Normal");
				}
			}
		});
	}
}
