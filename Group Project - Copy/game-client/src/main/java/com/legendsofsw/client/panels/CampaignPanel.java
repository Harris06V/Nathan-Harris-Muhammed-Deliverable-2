package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.legendsofsw.client.GameClient;

public class CampaignPanel extends JPanel implements Refreshable {

    private final GameClient client;
    private final JTextArea logArea;
    private final JPanel heroPanel;
    private final JLabel roomLabel;
    private final JLabel goldLabel;

    public CampaignPanel(GameClient client) {
        this.client = client;
        setName("Campaign");
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // top bar with room and gold
        JPanel topBar = new JPanel(new BorderLayout());
        roomLabel = new JLabel("Room: 0/30");
        goldLabel = new JLabel("Gold: 0", SwingConstants.RIGHT);
        topBar.add(roomLabel, BorderLayout.WEST);
        topBar.add(goldLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // left side - hero cards
        heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        JScrollPane heroScroll = new JScrollPane(heroPanel);
        heroScroll.setPreferredSize(new java.awt.Dimension(280, 0));
        heroScroll.setBorder(BorderFactory.createTitledBorder("Party"));
        add(heroScroll, BorderLayout.WEST);

        // center - log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log"));
        add(logScroll, BorderLayout.CENTER);

        // bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton nextRoomBtn = new JButton("Next Room");
        JButton inventoryBtn = new JButton("Use Item");
        JButton levelUpBtn = new JButton("Level Up");
        JButton saveBtn = new JButton("Save & Exit");

        btnPanel.add(nextRoomBtn);
        btnPanel.add(inventoryBtn);
        btnPanel.add(levelUpBtn);
        btnPanel.add(saveBtn);
        add(btnPanel, BorderLayout.SOUTH);

        nextRoomBtn.addActionListener(e -> {
            if (client.getCurrentCampaignId() < 0) return;
            Map<String, Object> result = client.getApi().nextRoom(client.getCurrentCampaignId());

            if (result.containsKey("error")) {
                String err = result.get("error").toString();
                if (err.contains("Campaign has ended")) {
                    Map<String, Object> finalResult = client.getApi().completeCampaign(client.getCurrentCampaignId());
                    String msg = "Campaign Complete!\n";
                    if (finalResult.containsKey("score")) {
                        msg += "Score: " + ((Number) finalResult.get("score")).intValue() + "\n";
                    }
                    if (finalResult.containsKey("message")) {
                        msg += finalResult.get("message");
                    }
                    logArea.append(msg + "\n");
                    handleCampaignEnd();
                } else {
                    JOptionPane.showMessageDialog(this, err);
                }
                return;
            }

            String roomType = result.getOrDefault("roomType", "").toString();
            String message = result.getOrDefault("message", "").toString();
            logArea.append("Room " + result.get("roomNumber") + ": " + message + "\n");

            if ("BATTLE".equals(roomType)) {
                Object battleIdObj = result.get("battleId");
                if (battleIdObj != null) {
                    long battleId = ((Number) battleIdObj).longValue();
                    client.setCurrentBattleId(battleId);
                    client.setBattleIsPvp(false);
                    JOptionPane.showMessageDialog(this, "Battle starting!");
                    client.showPanel("Battle");
                } else {
                    logArea.append("Battle could not be started. Try next room.\n");
                }
            } else if ("INN".equals(roomType)) {
                client.showPanel("Inn");
            }

            refreshPartyDisplay();
        });

        inventoryBtn.addActionListener(e -> {
            if (client.getCurrentPartyId() < 0) return;
            List<Map<String, Object>> inventory = client.getApi().getInventory(client.getCurrentPartyId());
            if (inventory.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No items in inventory.");
                return;
            }

            String[] itemNames = inventory.stream()
                    .map(i -> i.get("itemType") + " x" + ((Number) i.get("quantity")).intValue())
                    .toArray(String[]::new);

            String chosen = (String) JOptionPane.showInputDialog(this, "Select item to use:",
                    "Inventory", JOptionPane.PLAIN_MESSAGE, null, itemNames, itemNames[0]);
            if (chosen == null) return;

            String itemType = chosen.split(" x")[0];

            // pick a hero
            List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
            if (heroes.isEmpty()) return;

            String[] heroNames = heroes.stream()
                    .map(h -> h.get("name") + " (HP:" + ((Number) h.get("currentHealth")).intValue()
                            + "/" + ((Number) h.get("maxHealth")).intValue() + ")")
                    .toArray(String[]::new);

            String chosenHero = (String) JOptionPane.showInputDialog(this, "Use " + itemType + " on:",
                    "Select Hero", JOptionPane.PLAIN_MESSAGE, null, heroNames, heroNames[0]);
            if (chosenHero == null) return;

            int heroIdx = java.util.Arrays.asList(heroNames).indexOf(chosenHero);
            long heroId = ((Number) heroes.get(heroIdx).get("id")).longValue();

            Map<String, Object> result = client.getApi().useItem(client.getCurrentPartyId(), itemType, heroId);
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"));
            } else {
                JOptionPane.showMessageDialog(this, itemType + " used on " + heroes.get(heroIdx).get("name"));
            }
            refreshPartyDisplay();
        });

        levelUpBtn.addActionListener(e -> {
            if (client.getCurrentPartyId() < 0) return;
            List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
            // find heroes that can level up
            java.util.List<Map<String, Object>> canLevel = new java.util.ArrayList<>();
            for (Map<String, Object> h : heroes) {
                int exp = ((Number) h.get("experience")).intValue();
                int needed = ((Number) h.get("experienceToNextLevel")).intValue();
                int level = ((Number) h.get("level")).intValue();
                if (exp >= needed && level < 20) {
                    canLevel.add(h);
                }
            }
            if (canLevel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No heroes ready to level up.");
                return;
            }

            String[] heroNames = canLevel.stream()
                    .map(h -> h.get("name") + " (Lv" + ((Number) h.get("level")).intValue() + ")")
                    .toArray(String[]::new);

            String chosenHero = (String) JOptionPane.showInputDialog(this, "Select hero to level up:",
                    "Level Up", JOptionPane.PLAIN_MESSAGE, null, heroNames, heroNames[0]);
            if (chosenHero == null) return;

            int heroIdx = java.util.Arrays.asList(heroNames).indexOf(chosenHero);
            long heroId = ((Number) canLevel.get(heroIdx).get("id")).longValue();

            String[] classes = {"ORDER", "CHAOS", "WARRIOR", "MAGE"};
            String chosenClass = (String) JOptionPane.showInputDialog(this, "Choose class to level up:",
                    "Class Selection", JOptionPane.PLAIN_MESSAGE, null, classes, classes[0]);
            if (chosenClass == null) return;

            Map<String, Object> result = client.getApi().levelUpHero(heroId, chosenClass);
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"));
            } else {
                JOptionPane.showMessageDialog(this, "Leveled up in " + chosenClass + "!");
            }
            refreshPartyDisplay();
        });

        saveBtn.addActionListener(e -> {
            if (client.getCurrentCampaignId() < 0) return;
            Map<String, Object> result = client.getApi().saveCampaign(client.getCurrentCampaignId());
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"));
            } else {
                JOptionPane.showMessageDialog(this, "Campaign saved!");
                client.showPanel("Menu");
            }
        });
    }

    private void refreshAll() {
        if (client.getCurrentCampaignId() < 0) return;
        Map<String, Object> campaign = client.getApi().getCampaign(client.getCurrentCampaignId());
        if (campaign.containsKey("error")) return;

        int room = ((Number) campaign.get("currentRoom")).intValue();
        int total = ((Number) campaign.get("totalRooms")).intValue();
        String status = campaign.get("status").toString();
        roomLabel.setText("Room: " + room + "/" + total + "  [" + status + "]");

        logArea.setText("");
        if (campaign.get("lastRoomType") != null) {
            logArea.append("Last room: " + campaign.get("lastRoomType") + "\n");
        }

        refreshPartyDisplay();
    }

    private void refreshPartyDisplay() {
        heroPanel.removeAll();
        if (client.getCurrentPartyId() < 0) {
            heroPanel.add(new JLabel("No party"));
            heroPanel.revalidate();
            heroPanel.repaint();
            return;
        }
        List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
        Map<String, Object> partyData = client.getApi().getParty(client.getCurrentPartyId());

        int gold = 0;
        if (partyData.containsKey("party")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> party = (Map<String, Object>) partyData.get("party");
            gold = ((Number) party.get("gold")).intValue();
        }
        goldLabel.setText("Gold: " + gold);

        for (Map<String, Object> h : heroes) {
            boolean alive = h.get("alive") instanceof Boolean ? (Boolean) h.get("alive") : true;
            int hp = ((Number) h.get("currentHealth")).intValue();
            int maxHp = ((Number) h.get("maxHealth")).intValue();
            int mp = ((Number) h.get("currentMana")).intValue();
            int maxMp = ((Number) h.get("maxMana")).intValue();
            int atk = ((Number) h.get("attack")).intValue();
            int def = ((Number) h.get("defense")).intValue();
            int lvl = ((Number) h.get("level")).intValue();
            int exp = ((Number) h.get("experience")).intValue();
            int expNext = ((Number) h.get("experienceToNextLevel")).intValue();

            String color = alive ? "green" : "red";
            String html = "<html><b>" + h.get("name") + "</b> Lv" + lvl + "<br>"
                    + "HP: <font color='" + color + "'>" + hp + "/" + maxHp + "</font><br>"
                    + "MP: " + mp + "/" + maxMp + "<br>"
                    + "ATK:" + atk + " DEF:" + def + "<br>"
                    + "EXP: " + exp + "/" + expNext;
            if (h.get("hybridClass") != null && !"NONE".equals(h.get("hybridClass").toString())) {
                html += "<br>Hybrid: " + h.get("hybridClass");
            }
            html += "<br>" + (alive ? "ALIVE" : "DEAD") + "</html>";

            JLabel heroLabel = new JLabel(html);
            heroLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(java.awt.Color.GRAY),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)));
            heroPanel.add(heroLabel);
        }

        heroPanel.revalidate();
        heroPanel.repaint();
    }

    private void handleCampaignEnd() {
        // ask if they want to save the party for PvP
        int choice = JOptionPane.showConfirmDialog(this,
                "Campaign complete! Save this party for PvP?",
                "Save Party", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            List<Map<String, Object>> savedParties = client.getApi().getSavedParties(client.getPlayerId());

            String partyName = JOptionPane.showInputDialog(this, "Enter a name for this party:", "My Party");
            if (partyName == null || partyName.trim().isEmpty()) partyName = "Campaign Party";

            // serialize party data
            List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
            String partyJson = new com.google.gson.Gson().toJson(Map.of(
                    "partyId", client.getCurrentPartyId(), "heroes", heroes));

            if (savedParties.size() >= 5) {
                // need to replace one
                String[] names = savedParties.stream()
                        .map(p -> p.get("name") + " (ID:" + ((Number) p.get("id")).longValue() + ")")
                        .toArray(String[]::new);

                String toReplace = (String) JOptionPane.showInputDialog(this,
                        "You have 5 saved parties. Replace one:",
                        "Replace Party", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
                if (toReplace != null) {
                    int idx = java.util.Arrays.asList(names).indexOf(toReplace);
                    long oldId = ((Number) savedParties.get(idx).get("id")).longValue();
                    client.getApi().replaceParty(client.getPlayerId(), oldId, partyName, partyJson);
                    JOptionPane.showMessageDialog(this, "Party saved!");
                }
            } else {
                client.getApi().saveParty(client.getPlayerId(), partyName, partyJson);
                JOptionPane.showMessageDialog(this, "Party saved!");
            }
        }
        client.showPanel("Menu");
    }

    @Override
    public void onShow() {
        refreshAll();
    }
}
