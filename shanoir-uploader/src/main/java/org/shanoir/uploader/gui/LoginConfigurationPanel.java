package org.shanoir.uploader.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.LoginPanelActionListener;
import org.shanoir.uploader.action.init.StartupStateContext;

/**
 * 
 * This class is the Authentication GUI which allow a user to connect
 * with his login/password in log into the remote Shanoir server.
 * @author atouboul
 * 
 */
public class LoginConfigurationPanel extends JPanel {

	public JLabel loginLabel;
	public JTextField loginText;
	public JLabel passwordLabel;
	public JPasswordField passwordText;
	public JButton connect;

	public LoginConfigurationPanel(StartupStateContext sSC) {
		Container container = new Container();
		container.setLayout(new GridBagLayout());
		GridBagConstraints shanoirStartupGBC = new GridBagConstraints();
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		this.add(container);

		this.setBorder(new EmptyBorder(5, 5, 5, 5));

		loginLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.login"));
		loginLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 0;
		container.add(loginLabel, shanoirStartupGBC);

		loginText = new JTextField("");
		loginText.setPreferredSize(new Dimension(150, 20));
		loginText.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 0;
		container.add(loginText, shanoirStartupGBC);

		passwordLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.password"));
		passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 1;
		container.add(passwordLabel, shanoirStartupGBC);

		passwordText = new JPasswordField();
		passwordText.setPreferredSize(new Dimension(150, 20));
		passwordText.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 1;
		container.add(passwordText, shanoirStartupGBC);

		connect = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.connect"));
		connect.setPreferredSize(new Dimension(150, 20));
		connect.setHorizontalAlignment(SwingConstants.CENTER);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 2;
		container.add(connect, shanoirStartupGBC);
		
		LoginPanelActionListener lPAL = new LoginPanelActionListener(this, sSC);
		connect.addActionListener(lPAL);
		
	}
}
