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
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.service.rest.UpdateCheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatesWindow extends JFrame {

	private static final Logger logger = LoggerFactory.getLogger(UpdatesWindow.class);

	String releasesUrl = ShUpConfig.endpointProperties.getProperty("github.releases");

	private String currentVersion;

    private ResourceBundle resourceBundle;

	public UpdatesWindow(final ResourceBundle resourceBundle, String currentVersion) {

		this.resourceBundle = resourceBundle;
		this.currentVersion = currentVersion;

		// Create the frame.
		JFrame frame = new JFrame(resourceBundle.getString("shanoir.uploader.helpMenu.checkUpdates"));

		// What happens when the frame closes?
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		// Panel content
		JPanel masterPanel = new JPanel(new BorderLayout());
		frame.setContentPane(masterPanel);

		final JPanel updatesPanel = new JPanel();
		updatesPanel.setBorder(BorderFactory.createLineBorder(Color.black));

		masterPanel.add(updatesPanel, BorderLayout.CENTER);

		GridBagLayout gBLPanel = new GridBagLayout();
		gBLPanel.columnWidths = new int[] { 0, 0, 0 };
		gBLPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gBLPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gBLPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		updatesPanel.setLayout(gBLPanel);

		JLabel icon = new JLabel();
		icon.setIcon(DicomTreeCellRenderer.createImageIcon("/images/logo.shanoirUp_transp.128x128.png"));// your image
																											// here
		// updatesPanel.add(icon);
		addItem(updatesPanel, icon, 0, 1, 1, GridBagConstraints.CENTER);

		JLabel nameLabel = new JLabel("<html><body><B>"
				+ resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.name") + "</B></body></html>");
		addItem(updatesPanel, nameLabel, 0, 2, 1, GridBagConstraints.CENTER);

		// Setting a default value for release date (-in dev mode for exemple- it might not be set)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		String defaultDate = LocalDate.now().format(formatter)
;
		if (ShUpConfig.basicProperties.getProperty(ShUpConfig.RELEASE_DATE) == null
				|| ShUpConfig.basicProperties.getProperty(ShUpConfig.RELEASE_DATE).isEmpty()) {
			ShUpConfig.basicProperties.setProperty(ShUpConfig.RELEASE_DATE, defaultDate);
		}

		JLabel releasesLabel = new JLabel("<html><div style='white-space: nowrap;'><a href=''>"
				+ resourceBundle.getString("shanoir.uploader.helpMenu.aboutShUp.releases") + "</a></div></html>");
		addItem(updatesPanel, releasesLabel, 0, 4, 1, GridBagConstraints.CENTER);

		releasesLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI(releasesUrl));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

		// Size the frame.
		frame.pack();

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 415;
		int windowHeight = 400;
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

	private void checkUpdates(String currentVersion) {
        String latest = UpdateCheckerService.getLatestVersionFromGithub("MyOrg/MyRepo");
        if (UpdateCheckerService.isNewerVersion(latest, currentVersion)) {
            JOptionPane.showMessageDialog(this,
                "Nouvelle version dispo : " + latest,
                "Mise à jour",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Vous avez la dernière version (" + currentVersion + ")",
                "À jour",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
