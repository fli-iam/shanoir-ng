package org.shanoir.uploader.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.ProxyPanelActionListener;
import org.shanoir.uploader.action.init.StartupStateContext;

/**
 *	This class is the Proxy GUI which allows an user to configure the proxy
 *  in order to access the remote Shanoir server.
 * 
 * @author arno
 * @author mkain
 * 
 */
@SuppressWarnings("serial")
public class ProxyConfigurationPanel extends JPanel {

	public JLabel enableProxyLabel;
	public JCheckBox enableProxyCB;
	public JLabel secureSslProxyLabel;
	public JCheckBox secureSslProxyCB;
	public JLabel httpHostLabel;
	public JTextField httpHostText;
	public JLabel httpPortLabel;
	public JTextField httpPortText;
	public JLabel httpLoginLabel;
	public JTextField httpLoginText;
	public JLabel httpPasswordLabel;
	public JPasswordField httpPasswordText;
	public JButton httpConnect;

	public ProxyConfigurationPanel(StartupStateContext sSC) {
		Container container = new Container();
		container.setLayout(new GridBagLayout());
		GridBagConstraints shanoirStartupGBC = new GridBagConstraints();
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		this.add(container);

		this.setBorder(new EmptyBorder(5, 5, 5, 5));

		enableProxyLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.proxy.activate"));
		enableProxyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 0;
		container.add(enableProxyLabel, shanoirStartupGBC);

		enableProxyCB = new JCheckBox();
		enableProxyCB.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 0;
		container.add(enableProxyCB, shanoirStartupGBC);
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (enableProxyCB.isSelected()) {
					secureSslProxyCB.setEnabled(true);
					httpHostText.setEnabled(true);
					httpPortText.setEnabled(true);
					httpLoginText.setEnabled(true);
					httpPasswordText.setEnabled(true);
				} else {
					secureSslProxyCB.setSelected(false);
					secureSslProxyCB.setEnabled(false);
					httpHostText.setText("");
					httpHostText.setEnabled(false);
					httpPortText.setText("");
					httpPortText.setEnabled(false);
					httpLoginText.setText("");
					httpLoginText.setEnabled(false);
					httpPasswordText.setText("");
					httpPasswordText.setEnabled(false);
				}
			}
		};
		enableProxyCB.addActionListener(actionListener);
		
		secureSslProxyLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.proxy.ssl"));
		secureSslProxyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 1;
		container.add(secureSslProxyLabel, shanoirStartupGBC);

		secureSslProxyCB = new JCheckBox();
		secureSslProxyCB.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 1;
		container.add(secureSslProxyCB, shanoirStartupGBC);

		httpHostLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.proxy.host"));
		httpHostLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 2;
		container.add(httpHostLabel, shanoirStartupGBC);

		httpHostText = new JTextField("");
		httpHostText.setPreferredSize(new Dimension(150, 20));
		httpHostText.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 2;
		container.add(httpHostText, shanoirStartupGBC);

		httpPortLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.proxy.port"));
		httpPortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 3;
		container.add(httpPortLabel, shanoirStartupGBC);

		httpPortText = new JTextField("");
		httpPortText.setPreferredSize(new Dimension(150, 20));
		httpPortText.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 3;
		container.add(httpPortText, shanoirStartupGBC);

		httpLoginLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.login"));
		httpLoginLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 4;
		container.add(httpLoginLabel, shanoirStartupGBC);

		httpLoginText = new JTextField("");
		httpLoginText.setPreferredSize(new Dimension(150, 20));
		httpLoginText.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 4;
		container.add(httpLoginText, shanoirStartupGBC);

		httpPasswordLabel = new JLabel(ShUpConfig.resourceBundle.getString("shanoir.uploader.password"));
		httpPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		shanoirStartupGBC.weightx = 0.2;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 0;
		shanoirStartupGBC.gridy = 5;
		container.add(httpPasswordLabel, shanoirStartupGBC);

		httpPasswordText = new JPasswordField();
		httpPasswordText.setPreferredSize(new Dimension(150, 20));
		httpPasswordText.setHorizontalAlignment(SwingConstants.LEFT);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 5;
		container.add(httpPasswordText, shanoirStartupGBC);

		httpConnect = new JButton(ShUpConfig.resourceBundle.getString("shanoir.uploader.proxy.save"));
		httpConnect.setPreferredSize(new Dimension(150, 20));
		httpConnect.setHorizontalAlignment(SwingConstants.CENTER);
		shanoirStartupGBC.weightx = 0.7;
		shanoirStartupGBC.fill = GridBagConstraints.HORIZONTAL;
		shanoirStartupGBC.insets = new Insets(5, 5, 5, 5);
		shanoirStartupGBC.gridx = 2;
		shanoirStartupGBC.gridy = 6;
		container.add(httpConnect, shanoirStartupGBC);
		ProxyPanelActionListener pPAL = new ProxyPanelActionListener(this, sSC);
		httpConnect.addActionListener(pPAL);

	}
}
