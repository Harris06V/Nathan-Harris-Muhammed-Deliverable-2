package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.legendsofsw.client.GameClient;

public class PvpPanel extends JPanel implements Refreshable {

    private final GameClient client;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JTextArea inviteArea;
    private final JTextArea matchArea;
    private final JTextArea leagueArea;

    public PvpPanel(GameClient client) {
        this.client = client;
        setName("Pvp");
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel title = new JLabel("PvP Arena", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        // navigation tabs
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        JButton inviteTab = new JButton("Invitations");
        JButton matchTab = new JButton("Matches");
        JButton leagueTab = new JButton("League");
        JButton backBtn = new JButton("Back");
        tabPanel.add(inviteTab);
        tabPanel.add(matchTab);
        tabPanel.add(leagueTab);
        tabPanel.add(backBtn);
        add(tabPanel, BorderLayout.SOUTH);

        // card layout for the three views
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // invite card
        JPanel invitePanel = new JPanel(new BorderLayout(5, 5));
        inviteArea = new JTextArea();
        inviteArea.setEditable(false);
        inviteArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        invitePanel.add(new JScrollPane(inviteArea), BorderLayout.CENTER);

        JPanel inviteBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 3));
        JTextField targetField = new JTextField(12);
        JButton sendBtn = new JButton("Send Invite");
        JButton acceptBtn = new JButton("Accept");
        JButton declineBtn = new JButton("Decline");
        inviteBtnPanel.add(new JLabel("Username:"));
        inviteBtnPanel.add(targetField);
        inviteBtnPanel.add(sendBtn);
        inviteBtnPanel.add(acceptBtn);
        inviteBtnPanel.add(declineBtn);
        invitePanel.add(inviteBtnPanel, BorderLayout.SOUTH);
        cardPanel.add(invitePanel, "invites");

        // match card
        JPanel matchPanel = new JPanel(new BorderLayout(5, 5));
        matchArea = new JTextArea();
        matchArea.setEditable(false);
        matchArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        matchPanel.add(new JScrollPane(matchArea), BorderLayout.CENTER);
        cardPanel.add(matchPanel, "matches");

        // league card
        JPanel leaguePanel = new JPanel(new BorderLayout(5, 5));
        leagueArea = new JTextArea();
        leagueArea.setEditable(false);
        leagueArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        leaguePanel.add(new JScrollPane(leagueArea), BorderLayout.CENTER);
        cardPanel.add(leaguePanel, "league");

        add(cardPanel, BorderLayout.CENTER);

        // tab actions
        inviteTab.addActionListener(e -> { refreshInvites(); cardLayout.show(cardPanel, "invites"); });
        matchTab.addActionListener(e -> { refreshMatches(); cardLayout.show(cardPanel, "matches"); });
        leagueTab.addActionListener(e -> { refreshLeague(); cardLayout.show(cardPanel, "league"); });
        backBtn.addActionListener(e -> client.showPanel("Menu"));

        // invite actions
        sendBtn.addActionListener(e -> {
            String target = targetField.getText().trim();
            if (target.isEmpty()) return;
            Map<String, Object> result = client.getApi().sendPvpInvite(
                    client.getPlayerId(), client.getUsername(), target);
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"));
            } else {
                JOptionPane.showMessageDialog(this, "Invitation sent to " + target);
                targetField.setText("");
            }
            refreshInvites();
        });

        acceptBtn.addActionListener(e -> {
            List<Map<String, Object>> invites = client.getApi().getReceivedInvites(client.getPlayerId());
            if (invites.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No pending invitations.");
                return;
            }
            String[] names = invites.stream()
                    .map(i -> "From: " + i.get("senderUsername"))
                    .toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(this, "Accept invite:",
                    "PvP", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (chosen == null) return;

            int idx = java.util.Arrays.asList(names).indexOf(chosen);
            long inviteId = ((Number) invites.get(idx).get("id")).longValue();
            Map<String, Object> result = client.getApi().acceptInvite(inviteId);
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"));
            } else {
                // pick a saved party
                List<Map<String, Object>> savedParties = client.getApi().getSavedParties(client.getPlayerId());
                if (savedParties.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No saved parties!");
                    return;
                }
                String[] partyNames = savedParties.stream()
                        .map(p -> p.get("name").toString())
                        .toArray(String[]::new);
                String chosenParty = (String) JOptionPane.showInputDialog(this, "Select your party:",
                        "PvP Party", JOptionPane.PLAIN_MESSAGE, null, partyNames, partyNames[0]);
                if (chosenParty != null) {
                    // Get selected party data
                    int partyIdx = java.util.Arrays.asList(partyNames).indexOf(chosenParty);
                    String myPartyJson = (String) savedParties.get(partyIdx).get("partyDataJson");

                    // Get opponent's saved party (sender of invite)
                    long opponentId = ((Number) invites.get(idx).get("senderId")).longValue();
                    List<Map<String, Object>> oppParties = client.getApi().getSavedParties(opponentId);
                    if (oppParties.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Opponent has no saved parties!");
                        return;
                    }
                    String oppPartyJson = (String) oppParties.get(0).get("partyDataJson");

                    // Parse hero lists from party data
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType();
                    com.google.gson.JsonObject myObj = com.google.gson.JsonParser.parseString(myPartyJson).getAsJsonObject();
                    com.google.gson.JsonObject oppObj = com.google.gson.JsonParser.parseString(oppPartyJson).getAsJsonObject();
                    List<Map<String, Object>> myTeam = gson.fromJson(myObj.getAsJsonArray("heroes"), listType);
                    List<Map<String, Object>> oppTeam = gson.fromJson(oppObj.getAsJsonArray("heroes"), listType);

                    // Create PvP battle
                    Map<String, Object> battleResult = client.getApi().createPvpBattle(myTeam, oppTeam);
                    if (battleResult.containsKey("error")) {
                        JOptionPane.showMessageDialog(this, "Failed to start battle: " + battleResult.get("error"));
                        return;
                    }

                    long battleId = ((Number) battleResult.get("battleId")).longValue();

                    // Find match ID
                    List<Map<String, Object>> matches = client.getApi().getPvpMatches(client.getPlayerId());
                    long matchId = -1;
                    for (Map<String, Object> m : matches) {
                        boolean completed = m.get("completed") instanceof Boolean ? (Boolean) m.get("completed") : false;
                        if (!completed) {
                            matchId = ((Number) m.get("id")).longValue();
                            break;
                        }
                    }

                    // Set client state and launch battle
                    client.setCurrentBattleId(battleId);
                    client.setBattleIsPvp(true);
                    client.setPvpMatchId(matchId);
                    client.showPanel("Battle");
                }
            }
            refreshInvites();
        });

        declineBtn.addActionListener(e -> {
            List<Map<String, Object>> invites = client.getApi().getReceivedInvites(client.getPlayerId());
            if (invites.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No pending invitations.");
                return;
            }
            String[] names = invites.stream()
                    .map(i -> "From: " + i.get("senderUsername"))
                    .toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(this, "Decline invite:",
                    "PvP", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (chosen == null) return;

            int idx = java.util.Arrays.asList(names).indexOf(chosen);
            long inviteId = ((Number) invites.get(idx).get("id")).longValue();
            client.getApi().declineInvite(inviteId);
            JOptionPane.showMessageDialog(this, "Declined.");
            refreshInvites();
        });
    }

    private void refreshInvites() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Received Invitations ===\n");
        List<Map<String, Object>> received = client.getApi().getReceivedInvites(client.getPlayerId());
        if (received.isEmpty()) sb.append("  None\n");
        for (Map<String, Object> inv : received) {
            sb.append("  From: ").append(inv.get("senderUsername")).append("\n");
        }
        sb.append("\n=== Sent Invitations ===\n");
        List<Map<String, Object>> sent = client.getApi().getSentInvites(client.getPlayerId());
        if (sent.isEmpty()) sb.append("  None\n");
        for (Map<String, Object> inv : sent) {
            sb.append("  To: ").append(inv.get("receiverUsername"))
              .append(" [").append(inv.get("status")).append("]\n");
        }
        inviteArea.setText(sb.toString());
    }

    private void refreshMatches() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-10s %-10s %s%n", "Match", "Player1", "Player2", "Status"));
        sb.append("-".repeat(45)).append("\n");
        List<Map<String, Object>> matches = client.getApi().getPvpMatches(client.getPlayerId());
        if (matches.isEmpty()) sb.append("  No matches yet.\n");
        for (Map<String, Object> m : matches) {
            boolean completed = m.get("completed") instanceof Boolean ? (Boolean) m.get("completed") : false;
            String status = completed ? "Winner:" + ((Number) m.get("winnerId")).longValue() : "In Progress";
            sb.append(String.format("%-8d %-10d %-10d %s%n",
                    ((Number) m.get("id")).longValue(),
                    ((Number) m.get("player1Id")).longValue(),
                    ((Number) m.get("player2Id")).longValue(),
                    status));
        }

        sb.append("\n=== My Record ===\n");
        Map<String, Object> record = client.getApi().getPvpRecord(client.getPlayerId());
        if (record.containsKey("wins")) {
            sb.append("  Wins:   ").append(((Number) record.get("wins")).intValue()).append("\n");
            sb.append("  Losses: ").append(((Number) record.get("losses")).intValue()).append("\n");
        }
        matchArea.setText(sb.toString());
    }

    private void refreshLeague() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-12s %-6s %-6s%n", "Rank", "Player", "Wins", "Losses"));
        sb.append("-".repeat(35)).append("\n");
        List<Map<String, Object>> league = client.getApi().getLeagueStandings();
        if (league.isEmpty()) sb.append("  No standings yet.\n");
        for (int i = 0; i < league.size(); i++) {
            Map<String, Object> r = league.get(i);
            sb.append(String.format("%-6d %-12s %-6d %-6d%n",
                    i + 1,
                    "Player " + r.getOrDefault("playerId", "?"),
                    ((Number) r.getOrDefault("wins", 0)).intValue(),
                    ((Number) r.getOrDefault("losses", 0)).intValue()));
        }
        leagueArea.setText(sb.toString());
    }

    @Override
    public void onShow() {
        refreshInvites();
        cardLayout.show(cardPanel, "invites");
    }
}
