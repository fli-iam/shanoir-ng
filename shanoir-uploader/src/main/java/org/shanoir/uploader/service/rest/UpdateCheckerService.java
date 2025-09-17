package org.shanoir.uploader.service.rest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.JSONObject;

public class UpdateCheckerService {

    private static final String APP_VERSION = "1.2.3"; // ta version locale
    private static final String REPO = "MonOrganisation/MonRepo"; // ton repo GitHub

    public static void checkForUpdates(JFrame parent) {
        try {
            URL url = new URL("https://api.github.com/repos/" + REPO + "/releases/latest");
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

                JSONObject json = new JSONObject(response.toString());
                String latestTag = json.getString("tag_name"); // ex: "v1.3.0"

                if (isNewerVersion(latestTag, APP_VERSION)) {
                    JOptionPane.showMessageDialog(parent,
                        "Une nouvelle version est disponible : " + latestTag +
                        "\nVotre version actuelle : " + APP_VERSION +
                        "\nTéléchargez-la depuis : " + json.getString("html_url"),
                        "Mise à jour disponible",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println("Impossible de vérifier les mises à jour : " + e.getMessage());
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        latest = latest.replace("v", "");
        current = current.replace("v", "");
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        for (int i = 0; i < Math.min(latestParts.length, currentParts.length); i++) {
            int l = Integer.parseInt(latestParts[i]);
            int c = Integer.parseInt(currentParts[i]);
            if (l > c) return true;
            if (l < c) return false;
        }
        return latestParts.length > currentParts.length;
    }
}
