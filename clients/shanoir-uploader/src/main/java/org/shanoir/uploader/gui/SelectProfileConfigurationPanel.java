package org.shanoir.uploader.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.SelectProfilePanelActionListener;
import org.shanoir.uploader.action.init.StartupStateContext;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;

@SuppressWarnings("serial")
public class SelectProfileConfigurationPanel extends JPanel {

	public JLabel selectProfileLabel;
	@SuppressWarnings("rawtypes")
	public JComboBoxMandatory selectProfileCB;
	public JButton select;

	public SelectProfileConfigurationPanel(StartupStateContext sSC) {
		Container container = new Container();
		container.setLayout(new GridBagLayout());
		GridBagConstraints shanoirStartupGBC = new GridBagConstraints();
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		this.add(container);
		this.setBorder(new EmptyBorder(5, 5, 5, 5));

		selectProfileLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.profile.select.label"));
		selectProfileLabel.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 1;
		shanoirStartupGBC.gridy = 0;
		container.add(selectProfileLabel, shanoirStartupGBC);

		selectProfileCB = new JComboBoxMandatory();
		selectProfileCB.setBackground(Color.WHITE);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 1;
		shanoirStartupGBC.gridy = 1;
		container.add(selectProfileCB, shanoirStartupGBC);
		
		selectProfileCB.removeAllItems();
		String[] profiles = ShUpConfig.profiles;
		for (int i = 0; i < profiles.length; i++) {
			selectProfileCB.addItem(profiles[i]);
		}

		select = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.profile.select.button"));
		select.setPreferredSize(new Dimension(150, 20));
		select.setHorizontalAlignment(SwingConstants.CENTER);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 1;
		shanoirStartupGBC.gridy = 2;
		container.add(select, shanoirStartupGBC);
		
		SelectProfilePanelActionListener sPPAL = new SelectProfilePanelActionListener(this, sSC);
		select.addActionListener(sPPAL);
	}
}
