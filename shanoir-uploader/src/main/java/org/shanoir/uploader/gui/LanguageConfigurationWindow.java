package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.log4j.Logger;
import org.shanoir.uploader.action.LanguageConfigurationListener;

public class LanguageConfigurationWindow extends JFrame{
	
	private static Logger logger = Logger.getLogger(LanguageConfigurationWindow.class);
	public File shanoirUploaderFolder;
	public String LANGUAGE_PROPERTIES ;
	public ResourceBundle resourceBundle; 
	public JRadioButton  rbEnglish;
	public JRadioButton  rbFrench;
	
	LanguageConfigurationWindow( File shanoirUploaderFolder, String LANGUAGE_PROPERTIES, ResourceBundle resourceBundle)
	{
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.LANGUAGE_PROPERTIES = LANGUAGE_PROPERTIES;
		this.resourceBundle = resourceBundle;
		
		// Create the frame.
		JFrame frame = new JFrame(resourceBundle.getString("shanoir.uploader.configurationMenu.language.title"));

		// What happens when the frame closes?
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		


		// Panel content
		
		
		JPanel masterPanel = new JPanel(new BorderLayout());
		frame.setContentPane(masterPanel);
		
		final JPanel configurationPanel = new JPanel();
		//configurationPanel.setBorder(BorderFactory.createLineBorder(Color.black));		
		
		masterPanel.add(configurationPanel, BorderLayout.NORTH);
		
		GridBagLayout gBLPanel = new GridBagLayout();
		gBLPanel.columnWidths = new int[] { 0, 0, 0 };
		gBLPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gBLPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gBLPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		configurationPanel.setLayout(gBLPanel);
		 

		JLabel configurationLabel = new JLabel(resourceBundle.getString("shanoir.uploader.configurationMenu.language.label"));
		Font newLabelFont=new Font(configurationLabel.getFont().getName(),Font.BOLD,configurationLabel.getFont().getSize());
		configurationLabel.setFont(newLabelFont);
		addItem(configurationPanel, configurationLabel, 0, 0, 3, GridBagConstraints.WEST);
		
		
		
		
		
		
		
		rbEnglish = new JRadioButton (resourceBundle.getString("shanoir.uploader.configurationMenu.language.radioButton.english"));
		rbFrench = new JRadioButton(resourceBundle.getString("shanoir.uploader.configurationMenu.language.radioButton.french"));
		ButtonGroup bg1 = new ButtonGroup( );
		bg1.add(rbEnglish);
		bg1.add(rbFrench);
		addItem(configurationPanel, rbEnglish, 0, 1, 1, GridBagConstraints.CENTER);
		addItem(configurationPanel, rbFrench, 0, 2, 1, GridBagConstraints.CENTER);

		
		JButton configureButton= new JButton(resourceBundle.getString("shanoir.uploader.configurationMenu.language.configureButton"));
	    addItem(configurationPanel, configureButton, 0, 3, 1, GridBagConstraints.CENTER);
		
	    LanguageConfigurationListener lCL=new LanguageConfigurationListener(this);
	    configureButton.addActionListener(lCL);
		
		
		
		
		// Size the frame.
		frame.pack();
		
		// center the frame
		//frame.setLocationRelativeTo( null );
	    Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
	    int windowWidth = 300;
	    int windowHeight = 200;
	    // set position and size
	    frame.setBounds(center.x - windowWidth / 2, center.y - windowHeight / 2, windowWidth,
	        windowHeight);

		// Show it.
		frame.setVisible(true);
	}
	
	  private void addItem(JPanel p, JComponent c, int x, int y, int width, int align) {

		    GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = x;
			gc.gridy = y;
			gc.gridwidth = width;
			gc.anchor = align;
			gc.insets = new Insets(10, 10, 10, 10);
			p.add(c, gc);
		  }

}
