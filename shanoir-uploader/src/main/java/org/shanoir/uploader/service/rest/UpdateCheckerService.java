package org.shanoir.uploader.service.rest;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.shanoir.uploader.ShUpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to check for updates on GitHub releases page.
 * This service is called when the user clicks on "Check for updates" in the Help menu.
 *
 * @author lvallet
 *
 */
public class UpdateCheckerService {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCheckerService.class);

    private static final String APP_VERSION = ShUpConfig.SHANOIR_UPLOADER_VERSION;
    private static final String RELEASES_URL = ShUpConfig.endpointProperties.getProperty("github.releases");
    private static final String TAG_PREFIX = "SHUP_v";

    public static void checkForUpdates(JFrame parent, ResourceBundle resourceBundle) {
        try {
            URL url = new URL(RELEASES_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray releases = new JSONArray(response.toString());
                if (releases.length() > 0) {
                    for (int i = 0; i < releases.length(); i++) {
                        JSONObject json = releases.getJSONObject(i);
                        String latestTag = json.getString("tag_name");
                        if (isNewerVersion(latestTag, APP_VERSION)) {
                            String updateLink = json.getString("html_url");

                            // We create a JPanel to hold the message and the link
                            JPanel panel = new JPanel();
                            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                            JLabel messageLabel = new JLabel("<html><body>"
                                + resourceBundle.getString("shanoir.uploader.helpMenu.newVersionAvailable")
                                + "<b>" + latestTag.replace(TAG_PREFIX, "") + "</b><br>"
                                + resourceBundle.getString("shanoir.uploader.helpMenu.currentVersion")
                                + APP_VERSION.replace("v", "")
                                + "<br><br>"
                                + resourceBundle.getString("shanoir.uploader.helpMenu.releaseLink")
                                + "</body></html>"
                            );

                            JLabel releaseLinkLabel = new JLabel("<html><a href='" + updateLink + "'>" + updateLink + "</a></html>");
                            releaseLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            // Adding a mouse listener to handle the click event
                            releaseLinkLabel.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(java.awt.event.MouseEvent e) {
                                    if (Desktop.isDesktopSupported()) {
                                        try {
                                            Desktop.getDesktop().browse(new URI(updateLink));
                                        } catch (Exception ex) {
                                            logger.error("Error while opening the link: ", ex);
                                        }
                                    }

                                }
                            });

                            panel.add(messageLabel);
                            panel.add(Box.createVerticalStrut(8));
                            panel.add(releaseLinkLabel);

                            JOptionPane.showMessageDialog(parent,
                                panel,
                                resourceBundle.getString("shanoir.uploader.helpMenu.updateAvailableTitle"),
                                JOptionPane.INFORMATION_MESSAGE);
                                logger.info("New release found at {}", RELEASES_URL);
                            return;
                        }
                    }
                    JOptionPane.showMessageDialog(parent,
                        resourceBundle.getString("shanoir.uploader.helpMenu.currentVersion") + APP_VERSION.replace("v", "") + " " +resourceBundle.getString("shanoir.uploader.helpMenu.versionUpToDate"),
                        resourceBundle.getString("shanoir.uploader.helpMenu.noUpdatesFound"),
                        JOptionPane.INFORMATION_MESSAGE);
                        logger.info("No new release found at {}", RELEASES_URL);
                } else {
                    logger.info("No releases found at {}", RELEASES_URL);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Error while checking for updates: ", e);
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        // Check if it is a SHUP release
        if (latest.contains(TAG_PREFIX)) {
            //format the tags to compare only the version numbers
            latest = latest.replace(TAG_PREFIX, "");
            current = current.replace("v", "");
            String[] latestParts = latest.split("\\.");
            String[] currentParts = current.split("\\.");

            // for each part of the version number, compare the integers
            for (int i = 0; i < Math.min(latestParts.length, currentParts.length); i++) {
                int l = Integer.parseInt(latestParts[i]);
                int c = Integer.parseInt(currentParts[i]);
                if (l > c) return true;
                if (l < c) return false;
            }
            // if every parts are equal, the latest is newer only if it has more parts
            return latestParts.length > currentParts.length;
        }
        return false;
    }
}
