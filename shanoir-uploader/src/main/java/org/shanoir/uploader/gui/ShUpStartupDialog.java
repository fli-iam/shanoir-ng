package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.init.StartupStateContext;
import org.shanoir.uploader.service.wsdl.ServiceConfiguration;

/**
 * This class is the shanoir startup GUI which : - Display the startup info -
 * Display the Proxy GUI, Authentication GUI in case manual setting has to be
 * done.
 *
 * @author atouboulic
 * @author mkain
 *
 */
@SuppressWarnings("serial")
public class ShUpStartupDialog extends JFrame {

	private static Logger logger = Logger.getLogger(ShUpStartupDialog.class);

	public JFrame frame = this;
	public JPanel contentPanel;
	public JPanel startupPanel;
	public JPanel imagePanel;
	public ProxyConfigurationPanel proxyPanel;
	public LoginConfigurationPanel loginPanel;
	public JPanel logPanel;
	public JPanel additionalPanel = null; // handle the additional panel that can be info, proxy or login panel
	private static JTextPane startupText;

	public JTextArea startupTextArea;

	public ShUpStartupDialog(StartupStateContext sSC) {
		setTitle("ShanoirUploader " + ShUpConfig.SHANOIR_UPLOADER_VERSION);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		setBounds(x - 200, y - 300, 400, 600);
		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		initTitle();
		try {
			initLogo();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		initInfoPanel();
		initProxyPanel(sSC);
		initLoginPanel(sSC);
		initStartupText();
		setContentPane(contentPanel);
	}

	private void initTitle() {
		startupPanel = new JPanel();
		startupText = new JTextPane();
		Font font = new Font("Serif", Font.BOLD, 20);
		startupText.setFont(font);
		startupText.setText(ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.title"));
		startupText.setBackground(contentPanel.getBackground());
		startupText.setEditable(false);
		startupPanel.add(startupText);
		contentPanel.add(startupPanel);
	}

	private void initLogo() throws IOException {
		imagePanel = new JPanel();
		ImageIcon icon = DicomTreeCellRenderer.createImageIcon("/images/logo.shanoirUp_transp.128x128.png");
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		imagePanel.add(lbl);
		contentPanel.add(imagePanel);
	}

	private void initInfoPanel() {
		additionalPanel = new JPanel();
		additionalPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		additionalPanel.setLayout(new BorderLayout(0, 0));
		contentPanel.add(additionalPanel);
	}

	private void initProxyPanel(StartupStateContext sSC) {
		proxyPanel = new ProxyConfigurationPanel(sSC);
	}

	private void initLoginPanel(StartupStateContext sSC) {
		loginPanel = new LoginConfigurationPanel(sSC);
	}

	private void initStartupText() {
		logPanel = new JPanel();
		logPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		logPanel.setLayout(new BorderLayout(0, 0));
		contentPanel.add(logPanel);
		startupTextArea = new JTextArea(10, 120);
		startupTextArea.setMargin(new Insets(5, 5, 5, 5));
		startupTextArea.setEditable(false);
		logPanel.add(BorderLayout.CENTER, startupTextArea);
	}

	public void updateStartupText(String text) {
		startupTextArea.setText(startupTextArea.getText() + text);
	}

	public void showProxyForm() {
		ServiceConfiguration sc = ServiceConfiguration.getInstance();
		additionalPanel.removeAll();
		proxyPanel.httpConnect.setEnabled(true);
		proxyPanel.enableProxyCB.setSelected(sc.isProxyEnable());
		proxyPanel.secureSslProxyCB.setSelected(sc.isProxySecure());
		String proxyHost = sc.getProxyHost();
		if (proxyHost != null)
			proxyPanel.httpHostText.setText(proxyHost);
		String proxyPort = sc.getProxyPort();
		if (proxyPort != null)
			proxyPanel.httpPortText.setText(proxyPort);
		String proxyUser = sc.getProxyUser();
		if (proxyUser != null)
			proxyPanel.httpLoginText.setText(proxyUser);
		additionalPanel.add(proxyPanel);
		proxyPanel.repaint();
		proxyPanel.revalidate();
	}

	public void showLoginForm() {
		additionalPanel.removeAll();
		loginPanel.connect.setEnabled(true);
		loginPanel.loginText.setText(ServiceConfiguration.getInstance().getUsername());
		loginPanel.setFocusable(true);
		loginPanel.requestFocusInWindow();
		additionalPanel.add(loginPanel);
		loginPanel.repaint();
		loginPanel.revalidate();
	}

}
