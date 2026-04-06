package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.legendsofsw.client.GameClient;

public class NewCampaignPanel extends JPanel {

    private final List<String> selectedClasses = new ArrayList<>();
    private final JLabel partyLabel;
    private long pendingPartyId = -1;

    public NewCampaignPanel(GameClient client) {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Select class for your hero (1-5 heroes):", SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel classGrid = new JPanel(new GridLayout(2, 2, 10, 10));

        JButton warriorBtn = new JButton("<html><b>Warrior</b><br>Balanced fighter with<br>high HP and solid defense</html>");
        JButton mageBtn = new JButton("<html><b>Mage</b><br>Devastating magical power<br>but fragile in combat</html>");
        JButton orderBtn = new JButton("<html><b>Order</b><br>Disciplined tank<br>strong defense and shield</html>");
        JButton chaosBtn = new JButton("<html><b>Chaos</b><br>High speed and power<br>but reckless and fragile</html>");

        classGrid.add(warriorBtn);
        classGrid.add(mageBtn);
        classGrid.add(orderBtn);
        classGrid.add(chaosBtn);
        add(classGrid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        partyLabel = new JLabel("Party: 0/5", SwingConstants.CENTER);
        bottomPanel.add(partyLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel();
        JButton undoBtn = new JButton("Undo");
        JButton startBtn = new JButton("Start Campaign");
        JButton backBtn = new JButton("Back");
        btnPanel.add(undoBtn);
        btnPanel.add(startBtn);
        btnPanel.add(backBtn);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        warriorBtn.addActionListener(e -> addClass("WARRIOR"));
        mageBtn.addActionListener(e -> addClass("MAGE"));
        orderBtn.addActionListener(e -> addClass("ORDER"));
        chaosBtn.addActionListener(e -> addClass("CHAOS"));

        undoBtn.addActionListener(e -> {
            if (!selectedClasses.isEmpty()) {
                selectedClasses.remove(selectedClasses.size() - 1);
                updatePartyLabel();
            }
        });

        startBtn.addActionListener(e -> {
            if (selectedClasses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select at least one hero class.");
                return;
            }

            Map<String, Object> partyResult = client.getApi().createParty(client.getPlayerId());
            if (partyResult.containsKey("error")) {
                JOptionPane.showMessageDialog(this, "Failed to create party: " + partyResult.get("error"));
                return;
            }
            long partyId = ((Number) partyResult.get("id")).longValue();

            for (int i = 0; i < selectedClasses.size(); i++) {
                String heroClass = selectedClasses.get(i);
                String heroName = heroClass.charAt(0) + heroClass.substring(1).toLowerCase() + " " + (i + 1);
                Map<String, Object> heroResult = client.getApi().createHero(partyId, heroName, heroClass);
                if (heroResult.containsKey("error")) {
                    JOptionPane.showMessageDialog(this, "Failed to create hero: " + heroResult.get("error"));
                    return;
                }
            }

            Map<String, Object> campaignResult = client.getApi().startCampaign(client.getPlayerId(), partyId);
            if (campaignResult.containsKey("error")) {
                JOptionPane.showMessageDialog(this, "Failed to start campaign: " + campaignResult.get("error"));
                return;
            }

            long campaignId = ((Number) campaignResult.get("id")).longValue();
            client.setCurrentPartyId(partyId);
            client.setCurrentCampaignId(campaignId);
            client.setBattleIsPvp(false);

            JOptionPane.showMessageDialog(this, "Campaign started with " + selectedClasses.size() + " hero(es)!");
            selectedClasses.clear();
            updatePartyLabel();
            client.showPanel("Campaign");
        });

        backBtn.addActionListener(e -> {
            selectedClasses.clear();
            updatePartyLabel();
            client.showPanel("Menu");
        });
    }

    private void addClass(String heroClass) {
        if (selectedClasses.size() >= 5) {
            JOptionPane.showMessageDialog(this, "Party is full (5/5).");
            return;
        }
        selectedClasses.add(heroClass);
        updatePartyLabel();
    }

    private void updatePartyLabel() {
        StringBuilder sb = new StringBuilder("Party: " + selectedClasses.size() + "/5");
        if (!selectedClasses.isEmpty()) {
            sb.append(" [ ");
            for (String c : selectedClasses) {
                sb.append(c).append(" ");
            }
            sb.append("]");
        }
        partyLabel.setText(sb.toString());
    }
}
