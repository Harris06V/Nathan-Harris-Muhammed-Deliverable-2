package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.legendsofsw.client.GameClient;

public class SavedPartiesPanel extends JPanel implements Refreshable {

    private final GameClient client;
    private final JPanel listPanel;

    public SavedPartiesPanel(GameClient client) {
        this.client = client;
        setName("SavedParties");
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel title = new JLabel("Saved Parties");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(listPanel);
        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton loadBtn = new JButton("Load");
        JButton deleteBtn = new JButton("Delete");
        JButton backBtn = new JButton("Back");

        btnPanel.add(loadBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

        loadBtn.addActionListener(e -> {
            List<Map<String, Object>> parties = client.getApi().getSavedParties(client.getPlayerId());
            if (parties.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No saved parties.");
                return;
            }
            String[] names = parties.stream()
                    .map(p -> p.get("name").toString())
                    .toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(this, "Load party:",
                    "Load", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (chosen == null) return;

            int idx = java.util.Arrays.asList(names).indexOf(chosen);
            String partyDataJson = (String) parties.get(idx).get("partyDataJson");
            try {
                com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(partyDataJson).getAsJsonObject();
                long partyId = obj.get("partyId").getAsLong();
                client.setCurrentPartyId(partyId);
                JOptionPane.showMessageDialog(this, "Party loaded!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not parse party data.");
            }
        });

        deleteBtn.addActionListener(e -> {
            List<Map<String, Object>> parties = client.getApi().getSavedParties(client.getPlayerId());
            if (parties.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No saved parties.");
                return;
            }
            String[] names = parties.stream()
                    .map(p -> p.get("name").toString())
                    .toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(this, "Delete party:",
                    "Delete", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (chosen == null) return;

            int idx = java.util.Arrays.asList(names).indexOf(chosen);
            long id = ((Number) parties.get(idx).get("id")).longValue();
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this party?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                client.getApi().deleteParty(client.getPlayerId(), id);
                onShow();
            }
        });

        backBtn.addActionListener(e -> client.showPanel("Menu"));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                onShow();
            }
        });
    }

    @Override
    public void onShow() {
        listPanel.removeAll();

        List<Map<String, Object>> parties = client.getApi().getSavedParties(client.getPlayerId());
        if (parties.isEmpty()) {
            listPanel.add(new JLabel("No saved parties. Complete a campaign to save one."));
        } else {
            listPanel.add(new JLabel("Saved Parties: " + parties.size() + "/5"));
            for (int i = 0; i < parties.size(); i++) {
                Map<String, Object> sp = parties.get(i);
                StringBuilder html = new StringBuilder("<html><b>Party " + (i + 1) + ": " + sp.get("name") + "</b>");

                String json = (String) sp.get("partyDataJson");
                if (json != null) {
                    try {
                        com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                        if (obj.has("heroes")) {
                            com.google.gson.JsonArray heroes = obj.getAsJsonArray("heroes");
                            for (int h = 0; h < heroes.size(); h++) {
                                com.google.gson.JsonObject hero = heroes.get(h).getAsJsonObject();
                                html.append("<br>&nbsp;&nbsp;").append(hero.get("name").getAsString());
                                html.append(" | ").append(hero.get("baseClass").getAsString());
                                if (hero.has("hybridClass") && !hero.get("hybridClass").isJsonNull()
                                        && !"NONE".equals(hero.get("hybridClass").getAsString())) {
                                    html.append("/").append(hero.get("hybridClass").getAsString());
                                }
                                html.append(" | Lv").append(hero.get("level").getAsInt());
                                html.append(" | HP:").append(hero.get("maxHealth").getAsInt());
                                html.append(" ATK:").append(hero.get("attack").getAsInt());
                                html.append(" DEF:").append(hero.get("defense").getAsInt());
                            }
                        }
                    } catch (Exception ex) {
                        html.append("<br>&nbsp;&nbsp;(could not parse)");
                    }
                }
                html.append("</html>");

                JLabel card = new JLabel(html.toString());
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        BorderFactory.createEmptyBorder(4, 4, 4, 4)));
                listPanel.add(card);
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}
