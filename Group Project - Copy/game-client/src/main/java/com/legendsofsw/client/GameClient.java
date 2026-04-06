package com.legendsofsw.client;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.legendsofsw.client.panels.BattlePanel;
import com.legendsofsw.client.panels.CampaignPanel;
import com.legendsofsw.client.panels.InnPanel;
import com.legendsofsw.client.panels.LeaderboardPanel;
import com.legendsofsw.client.panels.LoginPanel;
import com.legendsofsw.client.panels.MainMenuPanel;
import com.legendsofsw.client.panels.NewCampaignPanel;
import com.legendsofsw.client.panels.PartyViewPanel;
import com.legendsofsw.client.panels.PvpPanel;
import com.legendsofsw.client.panels.Refreshable;
import com.legendsofsw.client.panels.RegisterPanel;
import com.legendsofsw.client.panels.SavedPartiesPanel;

// main entry point for the game client
// uses CardLayout to switch between panels
public class GameClient extends JFrame {

    private final CardLayout cl = new CardLayout();
    private final JPanel container = new JPanel(cl);
    private final ApiClient api;

    // session state
    private long playerId = -1;
    private String username = "";
    private long currentPartyId = -1;
    private long currentCampaignId = -1;
    private long currentBattleId = -1;
    private boolean battleIsPvp = false;
    private long pvpMatchId = -1;

    public GameClient(String gatewayUrl) {
        super("Legends of Sword and Wand");
        this.api = new ApiClient(gatewayUrl);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        buildPanels();
        add(container);
        cl.show(container, "Login");
    }

    private void buildPanels() {
        container.add(new LoginPanel(this), "Login");
        container.add(new RegisterPanel(this), "Register");
        container.add(new MainMenuPanel(this), "Menu");
        container.add(new NewCampaignPanel(this), "NewCampaign");
        container.add(new CampaignPanel(this), "Campaign");
        container.add(new BattlePanel(this), "Battle");
        container.add(new InnPanel(this), "Inn");
        container.add(new PartyViewPanel(this), "PartyView");
        container.add(new LeaderboardPanel(this), "Leaderboard");
        container.add(new PvpPanel(this), "Pvp");
        container.add(new SavedPartiesPanel(this), "SavedParties");
    }

    public void showPanel(String name) {
        // refresh panels when switching to them
        for (Component c : container.getComponents()) {
            if (c instanceof Refreshable) {
                Refreshable r = (Refreshable) c;
                if (c.getName() != null && c.getName().equals(name)) {
                    r.onShow();
                }
            }
        }
        cl.show(container, name);
    }

    // getters and setters for session state
    public ApiClient getApi() { return api; }

    public long getPlayerId() { return playerId; }
    public void setPlayerId(long playerId) { this.playerId = playerId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public long getCurrentPartyId() { return currentPartyId; }
    public void setCurrentPartyId(long currentPartyId) { this.currentPartyId = currentPartyId; }

    public long getCurrentCampaignId() { return currentCampaignId; }
    public void setCurrentCampaignId(long currentCampaignId) { this.currentCampaignId = currentCampaignId; }

    public long getCurrentBattleId() { return currentBattleId; }
    public void setCurrentBattleId(long currentBattleId) { this.currentBattleId = currentBattleId; }

    public boolean isBattleIsPvp() { return battleIsPvp; }
    public void setBattleIsPvp(boolean battleIsPvp) { this.battleIsPvp = battleIsPvp; }

    public long getPvpMatchId() { return pvpMatchId; }
    public void setPvpMatchId(long pvpMatchId) { this.pvpMatchId = pvpMatchId; }

    public static void main(String[] args) {
        String url = "http://localhost:8080";
        if (args.length > 0) {
            url = args[0];
        }

        String gatewayUrl = url;
        SwingUtilities.invokeLater(() -> {
            GameClient client = new GameClient(gatewayUrl);
            client.setVisible(true);
        });
    }
}
