package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.legendsofsw.client.GameClient;

public class LeaderboardPanel extends JPanel implements Refreshable {

    private final GameClient client;
    private final JTextArea displayArea;

    public LeaderboardPanel(GameClient client) {
        this.client = client;
        setName("Leaderboard");
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Hall of Fame");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton backBtn = new JButton("Back");
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);

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
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-6s %-20s %s%n", "Rank", "Username", "Score"));
        sb.append("-".repeat(40)).append("\n");

        List<Map<String, Object>> leaderboard = client.getApi().getLeaderboard();
        if (leaderboard.isEmpty()) {
            sb.append("  No scores yet.\n");
        }
        for (int i = 0; i < leaderboard.size(); i++) {
            Map<String, Object> entry = leaderboard.get(i);
            sb.append(String.format("%-6d %-20s %d%n",
                    i + 1,
                    entry.getOrDefault("playerName", "???"),
                    ((Number) entry.getOrDefault("score", 0)).intValue()));
        }

        sb.append("\n");
        sb.append(String.format("%-6s %-20s %s%n", "", "=== My Scores ===", ""));
        sb.append("-".repeat(40)).append("\n");

        List<Map<String, Object>> myScores = client.getApi().getPlayerScores(client.getPlayerId());
        if (myScores.isEmpty()) {
            sb.append("  No scores yet.\n");
        }
        for (Map<String, Object> entry : myScores) {
            sb.append("  ").append(((Number) entry.getOrDefault("score", 0)).intValue()).append(" pts\n");
        }

        sb.append("\n");
        sb.append(String.format("%-6s %-20s %s%n", "", "=== PvP Record ===", ""));
        sb.append("-".repeat(40)).append("\n");

        Map<String, Object> pvpRecord = client.getApi().getPvpRecord(client.getPlayerId());
        if (pvpRecord.containsKey("wins")) {
            sb.append("  Wins:   ").append(((Number) pvpRecord.get("wins")).intValue()).append("\n");
            sb.append("  Losses: ").append(((Number) pvpRecord.get("losses")).intValue()).append("\n");
        }

        sb.append("\n");
        sb.append(String.format("%-6s %-20s %s%n", "Rank", "=== League ===", "W / L"));
        sb.append("-".repeat(40)).append("\n");

        List<Map<String, Object>> league = client.getApi().getLeagueStandings();
        for (int i = 0; i < league.size(); i++) {
            Map<String, Object> record = league.get(i);
            sb.append(String.format("%-6d Player %-13s %d / %d%n",
                    i + 1,
                    record.getOrDefault("playerId", "?"),
                    ((Number) record.getOrDefault("wins", 0)).intValue(),
                    ((Number) record.getOrDefault("losses", 0)).intValue()));
        }

        displayArea.setText(sb.toString());
        displayArea.setCaretPosition(0);
    }
}
