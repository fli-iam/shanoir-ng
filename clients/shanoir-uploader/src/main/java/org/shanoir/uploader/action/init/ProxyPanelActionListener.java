package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.ProxyConfigurationPanel;
import org.shanoir.uploader.utils.Util;

public class ProxyPanelActionListener implements ActionListener {

	private ProxyConfigurationPanel proxyPanel;

	private StartupStateContext sSC;

	public ProxyPanelActionListener(ProxyConfigurationPanel proxyPanel, StartupStateContext sSC) {
		this.proxyPanel = proxyPanel;
		this.sSC = sSC;
	}

	public void actionPerformed(ActionEvent e) {
		ShUpConfig.proxyProperties.setProperty("proxy.enabled",
				Boolean.toString(this.proxyPanel.enableProxyCB.isSelected()));
		ShUpConfig.proxyProperties.setProperty("proxy.secure",
				Boolean.toString(this.proxyPanel.secureSslProxyCB.isSelected()));
		ShUpConfig.proxyProperties.setProperty("proxy.host", this.proxyPanel.httpHostText.getText());
		ShUpConfig.proxyProperties.setProperty("proxy.port", this.proxyPanel.httpPortText.getText());
		ShUpConfig.proxyProperties.setProperty("proxy.user", this.proxyPanel.httpLoginText.getText());
		ShUpConfig.proxyProperties.setProperty("proxy.password",
				String.valueOf(this.proxyPanel.httpPasswordText.getPassword()));
		Util.encryptPasswordAndCopyPropertiesFile(ShUpConfig.shanoirUploaderFolder, ShUpConfig.proxyProperties,
				ShUpConfig.PROXY_PROPERTIES, "proxy.password");
		sSC.nextState();
	}

}
