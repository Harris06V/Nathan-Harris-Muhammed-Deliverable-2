package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.legendsofsw.client.GameClient;

// battle screen - turn based combat with activity log
public class BattlePanel extends JPanel implements Refreshable {

    private final GameClient client;
    private final JPanel heroPanel;
    private final JPanel mobPanel;
    private final JTextArea logArea;
    private final JLabel turnLabel;
    private final JLabel queueLabel;
    private final JPanel actionPanel;

    // tracking state
    private long trackedBattleId = -1;
    private int lastLoggedTurn = 0;
    private Map<String, Object> currentState;
    private final Random random = new Random();

    public BattlePanel(GameClient client) {
        this.client = client;
        setName("Battle");
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // top bar with turn info
        JPanel topBar = new JPanel(new BorderLayout());
        turnLabel = new JLabel("Battle");
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        turnLabel.setForeground(new Color(0, 100, 0));
        queueLabel = new JLabel("", SwingConstants.RIGHT);
        queueLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        queueLabel.setForeground(Color.DARK_GRAY);
        topBar.add(turnLabel, BorderLayout.WEST);
        topBar.add(queueLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // left side - your team as HTML cards
        heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        JScrollPane heroScroll = new JScrollPane(heroPanel);
        heroScroll.setPreferredSize(new Dimension(240, 0));
        heroScroll.setBorder(BorderFactory.createTitledBorder("Your Team"));
        add(heroScroll, BorderLayout.WEST);

        // right side - enemy team as HTML cards
        mobPanel = new JPanel();
        mobPanel.setLayout(new BoxLayout(mobPanel, BoxLayout.Y_AXIS));
        JScrollPane mobScroll = new JScrollPane(mobPanel);
        mobScroll.setPreferredSize(new Dimension(240, 0));
        mobScroll.setBorder(BorderFactory.createTitledBorder("Enemies"));
        add(mobScroll, BorderLayout.EAST);

        // center - activity log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Battle Log"));
        add(logScroll, BorderLayout.CENTER);

        // bottom - action buttons
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        add(actionPanel, BorderLayout.SOUTH);
    }

    // log to activity log area
    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    @Override
    public void onShow() {
        long battleId = client.getCurrentBattleId();
        if (battleId != trackedBattleId) {
            trackedBattleId = battleId;
            logArea.setText("");
            lastLoggedTurn = 0;
            currentState = null;
            initBattle();
        }
    }

    // initialize battle display and log enemy stats
    @SuppressWarnings("unchecked")
    private void initBattle() {
        if (client.getCurrentBattleId() < 0) return;

        Map<String, Object> state = client.getApi().getBattleState(client.getCurrentBattleId());
        if (state.containsKey("error")) {
            log("Error: " + state.get("error"));
            return;
        }

        currentState = state;
        log("=== Battle Started! ===");

        // log party stats like reference GamePanel
        List<Map<String, Object>> team1 = (List<Map<String, Object>>) state.get("team1");
        if (team1 != null) {
            log("Your party:");
            for (Map<String, Object> h : team1) {
                log("  " + str(h, "name") + " [Lv" + num(h, "level") + "]"
                    + " HP:" + num(h, "currentHealth") + "/" + num(h, "maxHealth")
                    + " MP:" + num(h, "currentMana") + "/" + num(h, "maxMana")
                    + " ATK:" + num(h, "attack") + " DEF:" + num(h, "defense"));
            }
        }

        // log enemies like reference: "--- N enemy mob(s) appeared! ---"
        List<Map<String, Object>> team2 = (List<Map<String, Object>>) state.get("team2");
        if (team2 != null && !team2.isEmpty()) {
            log("--- " + team2.size() + " enemy mob(s) appeared! ---");
            for (int i = 0; i < team2.size(); i++) {
                Map<String, Object> e = team2.get(i);
                log("  Enemy " + (i + 1) + " Lv" + num(e, "level")
                    + " | HP:" + num(e, "currentHealth")
                    + " ATK:" + num(e, "attack") + " DEF:" + num(e, "defense"));
            }
        }

        updateDisplay(state);
        processNextUnit(state);
    }

    // update hero cards, mob cards, and turn info
    @SuppressWarnings("unchecked")
    private void updateDisplay(Map<String, Object> state) {
        populateTeamCards(heroPanel, (List<Map<String, Object>>) state.get("team1"));
        populateTeamCards(mobPanel, (List<Map<String, Object>>) state.get("team2"));
    }

    // determine whose turn it is and either show buttons or auto-act for enemies
    @SuppressWarnings("unchecked")
    private void processNextUnit(Map<String, Object> state) {
        currentState = state;
        String status = str(state, "status");

        // battle over
        if ("TEAM1_WIN".equals(status) || "TEAM2_WIN".equals(status)) {
            boolean playerWon = "TEAM1_WIN".equals(status);
            log(playerWon ? "\n*** YOU WIN! ***" : "\n*** DEFEATED ***");
            turnLabel.setText(playerWon ? "Victory!" : "Defeat");
            queueLabel.setText(" ");

            actionPanel.removeAll();
            JButton doneBtn = new JButton("Continue");
            doneBtn.addActionListener(e -> handleBattleEnd(playerWon));
            actionPanel.add(doneBtn);
            actionPanel.revalidate();
            actionPanel.repaint();
            return;
        }

        if (!"IN_PROGRESS".equals(status)) return;

        // round marker like reference: "--- New round. ---"
        int turn = num(state, "turnNumber");
        if (turn > lastLoggedTurn) {
            log("--- Round " + turn + " ---");
            lastLoggedTurn = turn;
        }

        Map<String, Object> currentUnit = (Map<String, Object>) state.get("currentUnit");
        if (currentUnit == null) return;

        String unitName = str(currentUnit, "name");
        int unitTeam = num(currentUnit, "team");

        queueLabel.setText("Current: " + unitName + " [Team " + unitTeam + "]");

        // handle stunned units automatically
        if (getBool(currentUnit, "stunned")) {
            Map<String, Object> response = client.getApi().performAction(
                client.getCurrentBattleId(), "WAIT", null);
            if (!response.containsKey("error")) {
                logActionResult(response);
                checkForDeaths(currentState, response);
                updateDisplay(response);
                processNextUnit(response);
            }
            return;
        }

        if (unitTeam == 2 && !client.isBattleIsPvp()) {
            // enemy turn - auto act
            turnLabel.setText(unitName + " (enemy)");
            actionPanel.removeAll();
            actionPanel.add(new JLabel("Enemy acting..."));
            actionPanel.revalidate();
            actionPanel.repaint();
            SwingUtilities.invokeLater(() -> doEnemyTurn(state, currentUnit));
        } else {
            // player turn - show action buttons
            turnLabel.setText(unitName + "'s turn");
            showPlayerActions(currentUnit, state);
        }
    }

    // show action buttons for the current player hero
    @SuppressWarnings("unchecked")
    private void showPlayerActions(Map<String, Object> currentUnit, Map<String, Object> state) {
        actionPanel.removeAll();

        String unitName = str(currentUnit, "name");
        JLabel label = new JLabel(unitName + "'s turn: ");
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(0, 100, 0));
        actionPanel.add(label);

        List<String> actions = (List<String>) state.get("availableActions");
        if (actions != null) {
            for (String action : actions) {
                JButton btn = new JButton(action.replace("CAST_", ""));
                btn.addActionListener(ev -> playerAction(action, state, unitName));
                actionPanel.add(btn);
            }
        }

        actionPanel.revalidate();
        actionPanel.repaint();
    }

    // player picks an action and we send it to the server
    @SuppressWarnings("unchecked")
    private void playerAction(String action, Map<String, Object> state, String heroName) {
        Integer targetIndex = null;

        if (action.equals("ATTACK")) {
            targetIndex = findAliveEnemyTarget(state);
            if (targetIndex == null) return;
        } else if (needsTarget(action)) {
            targetIndex = pickTarget(state, action);
            if (targetIndex == null) return; // cancelled
        }

        // log choice like reference: "Hero chose: Attack"
        log(heroName + " chose: " + formatAction(action));

        Map<String, Object> response = client.getApi().performAction(
            client.getCurrentBattleId(), action, targetIndex);

        if (response.containsKey("error")) {
            log("Error: " + response.get("error"));
            return;
        }

        logActionResult(response);
        checkForDeaths(currentState, response);
        updateDisplay(response);
        processNextUnit(response);
    }

    // enemy AI: always attacks
    @SuppressWarnings("unchecked")
    private void doEnemyTurn(Map<String, Object> state, Map<String, Object> enemyUnit) {
        String actionType = "ATTACK";
        Integer targetIndex = findAlivePlayerTarget(state);
        if (targetIndex == null) actionType = "DEFEND";

        Map<String, Object> response = client.getApi().performAction(
            client.getCurrentBattleId(), actionType, targetIndex);

        if (response.containsKey("error")) {
            log("Error: " + response.get("error"));
            return;
        }

        logActionResult(response);
        checkForDeaths(currentState, response);
        updateDisplay(response);
        processNextUnit(response);
    }

    // log the action result from the server response
    private void logActionResult(Map<String, Object> response) {
        Object resultObj = response.get("lastActionResult");
        if (resultObj != null && !resultObj.toString().isEmpty()) {
            log(resultObj.toString());
        }
    }

    // check if any units died between old and new state
    @SuppressWarnings("unchecked")
    private void checkForDeaths(Map<String, Object> oldState, Map<String, Object> newState) {
        if (oldState == null || newState == null) return;
        compareTeamDeaths(
            (List<Map<String, Object>>) oldState.get("team1"),
            (List<Map<String, Object>>) newState.get("team1"));
        compareTeamDeaths(
            (List<Map<String, Object>>) oldState.get("team2"),
            (List<Map<String, Object>>) newState.get("team2"));
    }

    private void compareTeamDeaths(List<Map<String, Object>> oldTeam, List<Map<String, Object>> newTeam) {
        if (oldTeam == null || newTeam == null) return;
        for (int i = 0; i < Math.min(oldTeam.size(), newTeam.size()); i++) {
            boolean wasAlive = getBool(oldTeam.get(i), "alive");
            boolean isAlive = getBool(newTeam.get(i), "alive");
            if (wasAlive && !isAlive) {
                log(str(newTeam.get(i), "name") + " has been defeated!");
            }
        }
    }

    // handle battle end - log rewards and navigate back
    private void handleBattleEnd(boolean playerWon) {
        if (client.isBattleIsPvp()) {
            if (client.getPvpMatchId() > 0) {
                long winnerId = playerWon ? client.getPlayerId() : -1;
                client.getApi().completePvpMatch(client.getPvpMatchId(), winnerId);
            }
            log(playerWon ? "=== PvP Victory! ===" : "=== PvP Defeat ===");
            JOptionPane.showMessageDialog(this,
                playerWon ? "You won the PvP match!" : "You lost the PvP match.");
            client.showPanel("Menu");
        } else {
            Map<String, Object> result = client.getApi().completeBattle(
                client.getCurrentCampaignId(), playerWon);

            // log rewards like reference GamePanel
            if (playerWon) {
                Object xpObj = result.get("experienceGained");
                Object goldObj = result.get("goldGained");
                int xp = xpObj != null ? ((Number) xpObj).intValue() : 0;
                int gold = goldObj != null ? ((Number) goldObj).intValue() : 0;
                log("--- Victory! +" + xp + " XP, +" + gold + " Gold ---");
            } else {
                log("--- Defeated! Lost 10% gold. ---");
            }

            Object msgObj = result.get("message");
            String msg = msgObj != null ? msgObj.toString() : (playerWon ? "Victory!" : "Defeat.");
            JOptionPane.showMessageDialog(this, msg);
            client.showPanel("Campaign");
        }
    }

    // target selection

    private boolean needsTarget(String action) {
        return action.equals("CAST_FIREBALL")
            || action.equals("CAST_CHAIN_LIGHTNING") || action.equals("CAST_BERSERKER_ATTACK");
    }

    @SuppressWarnings("unchecked")
    private Integer pickTarget(Map<String, Object> state, String action) {
        Map<String, Object> currentUnit = (Map<String, Object>) state.get("currentUnit");
        int currentTeam = num(currentUnit, "team");
        int enemyTeam = currentTeam == 1 ? 2 : 1;

        List<Map<String, Object>> t1 = (List<Map<String, Object>>) state.get("team1");
        List<Map<String, Object>> t2 = (List<Map<String, Object>>) state.get("team2");
        List<Map<String, Object>> enemies = enemyTeam == 1 ? t1 : t2;

        List<String> targetNames = new ArrayList<>();
        List<Integer> targetIndices = new ArrayList<>();

        for (int i = 0; i < enemies.size(); i++) {
            Map<String, Object> enemy = enemies.get(i);
            if (getBool(enemy, "alive")) {
                targetNames.add(str(enemy, "name") + " HP:" + num(enemy, "currentHealth"));
                targetIndices.add(i);
            }
        }

        if (targetNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No valid targets!");
            return null;
        }

        String[] names = targetNames.toArray(new String[0]);
        String chosen = (String) JOptionPane.showInputDialog(this, "Choose target:",
            "Target", JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (chosen == null) return null;

        int chosenIdx = Arrays.asList(names).indexOf(chosen);
        int teamLocalIdx = targetIndices.get(chosenIdx);

        return computeGlobalIndex(t1, t2, enemyTeam, teamLocalIdx);
    }

    // find a random alive player unit for enemy targeting
    @SuppressWarnings("unchecked")
    private Integer findAlivePlayerTarget(Map<String, Object> state) {
        List<Map<String, Object>> t1 = (List<Map<String, Object>>) state.get("team1");
        List<Map<String, Object>> t2 = (List<Map<String, Object>>) state.get("team2");

        List<Integer> aliveIndices = new ArrayList<>();
        for (int i = 0; i < t1.size(); i++) {
            if (getBool(t1.get(i), "alive")) {
                aliveIndices.add(i);
            }
        }

        if (aliveIndices.isEmpty()) return null;

        int teamLocalIdx = aliveIndices.get(random.nextInt(aliveIndices.size()));
        return computeGlobalIndex(t1, t2, 1, teamLocalIdx);
    }

    // auto-target first alive enemy for hero ATTACK
    @SuppressWarnings("unchecked")
    private Integer findAliveEnemyTarget(Map<String, Object> state) {
        List<Map<String, Object>> t1 = (List<Map<String, Object>>) state.get("team1");
        List<Map<String, Object>> t2 = (List<Map<String, Object>>) state.get("team2");

        for (int i = 0; i < t2.size(); i++) {
            if (getBool(t2.get(i), "alive")) {
                return computeGlobalIndex(t1, t2, 2, i);
            }
        }
        return null;
    }

    // reconstruct the global index in the server's flat turn-order list
    private int computeGlobalIndex(List<Map<String, Object>> t1, List<Map<String, Object>> t2,
                                    int targetTeam, int teamLocalIdx) {
        int totalT1 = t1.size();
        int totalT2 = t2.size();

        boolean t1First = true;
        if (!t1.isEmpty() && !t2.isEmpty()) {
            int l1 = num(t1.get(0), "level");
            int l2 = num(t2.get(0), "level");
            int a1 = num(t1.get(0), "attack");
            int a2 = num(t2.get(0), "attack");
            if (l2 > l1 || (l2 == l1 && a2 > a1)) {
                t1First = false;
            }
        }

        int i1 = 0, i2 = 0;
        int globalIdx = 0;

        while (i1 < totalT1 || i2 < totalT2) {
            if (t1First) {
                if (i1 < totalT1) {
                    if (targetTeam == 1 && i1 == teamLocalIdx) return globalIdx;
                    i1++; globalIdx++;
                }
                if (i2 < totalT2) {
                    if (targetTeam == 2 && i2 == teamLocalIdx) return globalIdx;
                    i2++; globalIdx++;
                }
            } else {
                if (i2 < totalT2) {
                    if (targetTeam == 2 && i2 == teamLocalIdx) return globalIdx;
                    i2++; globalIdx++;
                }
                if (i1 < totalT1) {
                    if (targetTeam == 1 && i1 == teamLocalIdx) return globalIdx;
                    i1++; globalIdx++;
                }
            }
        }

        return 0;
    }

    // format action name for the log
    private String formatAction(String action) {
        return switch (action) {
            case "ATTACK" -> "Attack";
            case "DEFEND" -> "Defend";
            case "WAIT" -> "Wait";
            case "CAST_PROTECT" -> "Protect";
            case "CAST_HEAL" -> "Heal";
            case "CAST_FIREBALL" -> "Fireball";
            case "CAST_CHAIN_LIGHTNING" -> "Chain Lightning";
            case "CAST_BERSERKER_ATTACK" -> "Berserker Attack";
            case "CAST_REPLENISH" -> "Replenish";
            default -> action;
        };
    }

    // render team cards in a panel
    private void populateTeamCards(JPanel panel, List<Map<String, Object>> team) {
        panel.removeAll();
        if (team == null) {
            panel.add(new JLabel("No data"));
            panel.revalidate();
            panel.repaint();
            return;
        }
        for (Map<String, Object> u : team) {
            boolean alive = getBool(u, "alive");
            int hp = num(u, "currentHealth");
            int maxHp = num(u, "maxHealth");
            int mp = num(u, "currentMana");
            int maxMp = num(u, "maxMana");
            int atk = num(u, "attack");
            int def = num(u, "defense");
            int lvl = num(u, "level");
            int shield = num(u, "shield");
            boolean stunned = getBool(u, "stunned");

            String color = alive ? "green" : "red";
            String html = "<html><b>" + str(u, "name") + "</b> Lv" + lvl
                    + "<br>HP: <font color='" + color + "'>" + hp + "/" + maxHp + "</font>"
                    + "<br>MP: " + mp + "/" + maxMp
                    + "<br>ATK:" + atk + " DEF:" + def;
            if (shield > 0) html += " SH:" + shield;
            if (stunned) html += " <font color='orange'>[STUNNED]</font>";
            html += "<br>" + (alive ? "ALIVE" : "<font color='red'>DEAD</font>") + "</html>";

            JLabel card = new JLabel(html);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(alive ? Color.DARK_GRAY : Color.RED),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            panel.add(card);
        }
        panel.revalidate();
        panel.repaint();
    }

    // helper to safely get string from map
    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    // helper to safely get int from map
    private int num(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return 0;
    }

    // helper to safely get boolean from map
    private boolean getBool(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        return false;
    }
}
