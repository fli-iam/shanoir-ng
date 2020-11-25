package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;

public class AboutWindow extends JFrame {

	private static Logger logger = Logger.getLogger(AboutWindow.class);

	String supportMail = "imagerie@ofsep.org";

	public AboutWindow(final ResourceBundle resourceBundle) {
		// Create the frame.
		JFrame frame = new JFrame(resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.title"));

		// What happens when the frame closes?
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		// Panel content

		JPanel masterPanel = new JPanel(new BorderLayout());
		frame.setContentPane(masterPanel);

		final JPanel aboutPanel = new JPanel();
		aboutPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		masterPanel.add(aboutPanel, BorderLayout.NORTH);

		GridBagLayout gBLPanel = new GridBagLayout();
		gBLPanel.columnWidths = new int[] { 0, 0, 0 };
		gBLPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gBLPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gBLPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		aboutPanel.setLayout(gBLPanel);

		JLabel icon = new JLabel();
		icon.setIcon(DicomTreeCellRenderer.createImageIcon("/images/logo.shanoirUp_transp.128x128.png"));// your image
																											// here
		// aboutPanel.add(icon);
		addItem(aboutPanel, icon, 0, 1, 1, GridBagConstraints.CENTER);

		JLabel nameLabel = new JLabel("<html><body><B>"
				+ resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.name") + "</B></body></html>");
		addItem(aboutPanel, nameLabel, 0, 2, 1, GridBagConstraints.CENTER);

		JLabel versionLabel = new JLabel(ShUpConfig.SHANOIR_UPLOADER_VERSION);
		addItem(aboutPanel, versionLabel, 0, 4, 1, GridBagConstraints.CENTER);

		JLabel copyrightLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.copyrightShUp"));
		addItem(aboutPanel, copyrightLabel, 0, 5, 1, GridBagConstraints.CENTER);

		JLabel copyrightPseudonymusLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.copyrightPseudonymus"));
		addItem(aboutPanel, copyrightPseudonymusLabel, 0, 6, 1, GridBagConstraints.CENTER);

		final JLabel SupportLabel = new JLabel(
				resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.supportMail"));
		addItem(aboutPanel, SupportLabel, 0, 7, 1, GridBagConstraints.CENTER);

		SupportLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				try {
					Desktop.getDesktop().mail(new URI(
							"mailto:" + resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.supportMail")));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public void mouseEntered(MouseEvent e) {

				SupportLabel
						.setText("<html><body><u><font color =#0000FF>" + supportMail + "</font></u></body></html>");

			}

			public void mouseExited(MouseEvent e) {
				SupportLabel.setText(supportMail);
			}

		});

		// Size the frame.
		frame.pack();

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 300;
		int windowHeight = 369;
		// set position and size
		frame.setBounds(center.x - windowWidth / 2, center.y - windowHeight / 2, windowWidth, windowHeight);

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
