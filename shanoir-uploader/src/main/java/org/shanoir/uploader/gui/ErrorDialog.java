package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class ErrorDialog extends JDialog {
	public JDialog errorDialog;
	public JLabel errorLabel;
	public JButton errorButton;
	public JLabel connexionStatus;
	ResourceBundle resourceBundle; 
	
	public ErrorDialog(JDialog jdialog, String title, ResourceBundle resourceBundle) {
		super(jdialog,title,true);
		errorDialog = new JDialog(this,resourceBundle.getString("shanoir.uploader.systemErrorDialog.title"));
		errorDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		errorDialog.setSize(400, 150);
		errorDialog.setLocationRelativeTo(this);
		
		errorLabel = new JLabel("<html><p>"+resourceBundle.getString("shanoir.uploader.systemErrorDialog.label")+"</p>");
		errorDialog.add(errorLabel,BorderLayout.NORTH);
		
		errorButton = new JButton(resourceBundle.getString("shanoir.uploader.systemErrorDialog.button"));
		errorDialog.add(errorButton,BorderLayout.SOUTH);
		
	}
	
	

}
