package com.legendsofsw.client.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.legendsofsw.client.GameClient;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(GameClient client) {
        setBackground(Color.LIGHT_GRAY);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 20, 6, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("Main Menu", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JButton newCampaignBtn = new JButton("New PvE Campaign");
        JButton continueCampaignBtn = new JButton("Continue Campaign");
        JButton pvpBtn = new JButton("PvP Battle");
        JButton partyBtn = new JButton("View Party");
        JButton savedPartiesBtn = new JButton("Saved Parties");
        JButton leaderboardBtn = new JButton("Leaderboard & Scores");
        JButton logoutBtn = new JButton("Logout");

        gbc.gridy = 0; add(title, gbc);
        gbc.gridy = 1; add(newCampaignBtn, gbc);
        gbc.gridy = 2; add(continueCampaignBtn, gbc);
        gbc.gridy = 3; add(pvpBtn, gbc);
        gbc.gridy = 4; add(partyBtn, gbc);
        gbc.gridy = 5; add(savedPartiesBtn, gbc);
        gbc.gridy = 6; add(leaderboardBtn, gbc);
        gbc.gridy = 7; add(logoutBtn, gbc);

        newCampaignBtn.addActionListener(e -> client.showPanel("NewCampaign"));

        continueCampaignBtn.addActionListener(e -> {
            List<Map<String, Object>> campaigns = client.getApi().getPlayerCampaigns(client.getPlayerId());
            // find a paused or active campaign
            Map<String, Object> found = null;
            for (Map<String, Object> c : campaigns) {
                String status = c.get("status").toString();
                if (status.equals("PAUSED") || status.equals("ACTIVE") || status.equals("AT_INN")) {
                    found = c;
                    break;
                }
            }
            if (found == null) {
                JOptionPane.showMessageDialog(this, "No saved campaign found. Start a new one.");
                return;
            }
            long campaignId = ((Number) found.get("id")).longValue();
            long partyId = ((Number) found.get("partyId")).longValue();
            String status = found.get("status").toString();

            client.setCurrentCampaignId(campaignId);
            client.setCurrentPartyId(partyId);

            if (status.equals("PAUSED")) {
                client.getApi().resumeCampaign(campaignId);
            }

            client.showPanel("Campaign");
        });

        pvpBtn.addActionListener(e -> client.showPanel("Pvp"));
        partyBtn.addActionListener(e -> client.showPanel("PartyView"));
        savedPartiesBtn.addActionListener(e -> client.showPanel("SavedParties"));
        leaderboardBtn.addActionListener(e -> client.showPanel("Leaderboard"));

        logoutBtn.addActionListener(e -> {
            client.setPlayerId(-1);
            client.setUsername("");
            client.setCurrentPartyId(-1);
            client.setCurrentCampaignId(-1);
            client.showPanel("Login");
        });
    }
}
