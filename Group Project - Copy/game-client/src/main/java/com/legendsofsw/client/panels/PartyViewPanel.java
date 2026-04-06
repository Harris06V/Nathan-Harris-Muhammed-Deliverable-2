package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.legendsofsw.client.GameClient;

public class PartyViewPanel extends JPanel implements Refreshable {

    private final GameClient client;
    private final JTextArea displayArea;

    public PartyViewPanel(GameClient client) {
        this.client = client;
        setName("PartyView");
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Party Details");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JButton backBtn = new JButton("Back to Menu");
        backBtn.addActionListener(e -> client.showPanel("Menu"));
        add(backBtn, BorderLayout.SOUTH);
    }

    @Override
    public void onShow() {
        if (client.getCurrentPartyId() < 0) {
            // check if player has any parties
            List<Map<String, Object>> parties = client.getApi().getPartiesByOwner(client.getPlayerId());
            if (parties.isEmpty()) {
                displayArea.setText("No active party. Start a campaign first.");
                return;
            }
            // show the first party
            client.setCurrentPartyId(((Number) parties.get(0).get("id")).longValue());
        }

        Map<String, Object> partyData = client.getApi().getParty(client.getCurrentPartyId());
        if (partyData.containsKey("error")) {
            displayArea.setText("Could not load party.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (partyData.containsKey("party")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> party = (Map<String, Object>) partyData.get("party");
            sb.append("Party ID: ").append(((Number) party.get("id")).longValue()).append("\n");
            sb.append("Gold: ").append(((Number) party.get("gold")).intValue()).append("\n\n");
        }

        List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
        sb.append("Heroes (").append(heroes.size()).append("/5):\n");
        sb.append("=".repeat(40)).append("\n");

        for (Map<String, Object> h : heroes) {
            sb.append(h.get("name")).append(" - Level ").append(((Number) h.get("level")).intValue()).append("\n");
            sb.append("  HP: ").append(((Number) h.get("currentHealth")).intValue())
              .append("/").append(((Number) h.get("maxHealth")).intValue()).append("\n");
            sb.append("  Mana: ").append(((Number) h.get("currentMana")).intValue())
              .append("/").append(((Number) h.get("maxMana")).intValue()).append("\n");
            sb.append("  Attack: ").append(((Number) h.get("attack")).intValue())
              .append("  Defense: ").append(((Number) h.get("defense")).intValue()).append("\n");
            sb.append("  EXP: ").append(((Number) h.get("experience")).intValue())
              .append("/").append(((Number) h.get("experienceToNextLevel")).intValue()).append("\n");

            sb.append("  Class Levels - ");
            sb.append("Order:").append(((Number) h.get("orderLevel")).intValue()).append(" ");
            sb.append("Chaos:").append(((Number) h.get("chaosLevel")).intValue()).append(" ");
            sb.append("Warrior:").append(((Number) h.get("warriorLevel")).intValue()).append(" ");
            sb.append("Mage:").append(((Number) h.get("mageLevel")).intValue()).append("\n");

            if (h.get("hybridClass") != null && !"NONE".equals(h.get("hybridClass").toString())) {
                sb.append("  Hybrid Class: ").append(h.get("hybridClass")).append("\n");
            } else if (h.get("specialization") != null && !h.get("specialization").toString().equals("null")) {
                sb.append("  Specialization: ").append(h.get("specialization")).append("\n");
            }

            boolean alive = h.get("alive") instanceof Boolean ? (Boolean) h.get("alive") : true;
            sb.append("  Status: ").append(alive ? "ALIVE" : "DEAD").append("\n");
            sb.append("-".repeat(40)).append("\n");
        }

        sb.append("\nInventory:\n");
        List<Map<String, Object>> inventory = client.getApi().getInventory(client.getCurrentPartyId());
        if (inventory.isEmpty()) {
            sb.append("  (empty)\n");
        }
        for (Map<String, Object> item : inventory) {
            sb.append("  ").append(item.get("itemType")).append(" x")
              .append(((Number) item.get("quantity")).intValue()).append("\n");
        }

        displayArea.setText(sb.toString());
    }
}
