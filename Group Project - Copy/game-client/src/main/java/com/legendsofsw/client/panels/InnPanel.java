package com.legendsofsw.client.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.legendsofsw.client.GameClient;

public class InnPanel extends JPanel implements Refreshable {

    private final GameClient client;
    private final JPanel heroPanel;
    private final JLabel goldLabel;
    private final JPanel inventoryPanel;

    private static final String[][] ITEM_DEFS = {
        {"BREAD",  "200",  "+20 HP"},
        {"CHEESE", "500",  "+50 HP"},
        {"STEAK",  "1000", "+200 HP"},
        {"WATER",  "150",  "+10 MP"},
        {"JUICE",  "400",  "+30 MP"},
        {"WINE",   "750",  "+100 MP"},
        {"ELIXIR", "2000", "Revive+Full"}
    };

    public InnPanel(GameClient client) {
        this.client = client;
        setName("Inn");
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // top bar
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Welcome to the Inn!");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        goldLabel = new JLabel("Gold: 0", SwingConstants.RIGHT);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(goldLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // left - hero cards
        heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        JScrollPane heroScroll = new JScrollPane(heroPanel);
        heroScroll.setPreferredSize(new java.awt.Dimension(250, 0));
        heroScroll.setBorder(BorderFactory.createTitledBorder("Party"));
        add(heroScroll, BorderLayout.WEST);

        // center - shop grid + inventory
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        JPanel shopPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        shopPanel.setBorder(BorderFactory.createTitledBorder("Shop"));
        for (String[] item : ITEM_DEFS) {
            JButton buyBtn = new JButton("<html>" + item[0] + " <b>" + item[1] + "g</b><br>" + item[2] + "</html>");
            buyBtn.addActionListener(e -> buyItem(item[0]));
            shopPanel.add(buyBtn);
        }
        centerPanel.add(shopPanel, BorderLayout.CENTER);

        inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Inventory"));
        centerPanel.add(inventoryPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        JButton recruitBtn = new JButton("Recruit Hero");
        JButton useItemBtn = new JButton("Use Item");
        JButton continueBtn = new JButton("Continue");

        btnPanel.add(recruitBtn);
        btnPanel.add(useItemBtn);
        btnPanel.add(continueBtn);
        add(btnPanel, BorderLayout.SOUTH);

        recruitBtn.addActionListener(e -> {
            if (client.getCurrentPartyId() < 0) return;

            List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
            if (heroes.size() >= 5) {
                JOptionPane.showMessageDialog(this, "Party is full (5/5).");
                return;
            }

            if (client.getCurrentCampaignId() < 0) return;
            Map<String, Object> campaign = client.getApi().getCampaign(client.getCurrentCampaignId());
            int room = ((Number) campaign.get("currentRoom")).intValue();
            if (room > 10) {
                JOptionPane.showMessageDialog(this, "No recruits available after room 10.");
                return;
            }

            String[] classes = {"ORDER", "CHAOS", "WARRIOR", "MAGE"};
            int level = 1 + (int)(Math.random() * 4);
            String heroClass = classes[(int)(Math.random() * classes.length)];
            int cost = level == 1 ? 0 : 200 * level;

            String msg = heroClass + " recruit Lv" + level + " - Cost: " + cost + "g\nRecruit?";
            int choice = JOptionPane.showConfirmDialog(this, msg, "Recruit Hero", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;

            if (cost > 0) {
                Map<String, Object> partyData = client.getApi().getParty(client.getCurrentPartyId());
                @SuppressWarnings("unchecked")
                Map<String, Object> party = (Map<String, Object>) partyData.get("party");
                int gold = ((Number) party.get("gold")).intValue();
                if (gold < cost) {
                    JOptionPane.showMessageDialog(this, "Not enough gold!");
                    return;
                }
                client.getApi().addGold(client.getCurrentPartyId(), -cost);
            }

            String name = heroClass.charAt(0) + heroClass.substring(1).toLowerCase() + " Recruit";
            Map<String, Object> heroResult = client.getApi().createHero(client.getCurrentPartyId(), name, heroClass);
            if (heroResult.containsKey("error")) {
                JOptionPane.showMessageDialog(this, heroResult.get("error"));
            } else {
                long heroId = ((Number) heroResult.get("id")).longValue();
                for (int i = 1; i < level; i++) {
                    client.getApi().addExperience(heroId, 99999);
                    client.getApi().levelUpHero(heroId, heroClass);
                }
                JOptionPane.showMessageDialog(this, "Recruited " + name + " Lv" + level + "!");
                refreshDisplay();
            }
        });

        useItemBtn.addActionListener(e -> {
            if (client.getCurrentPartyId() < 0) return;
            List<Map<String, Object>> inventory = client.getApi().getInventory(client.getCurrentPartyId());
            if (inventory.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No items.");
                return;
            }
            String[] itemNames = inventory.stream()
                    .map(i -> i.get("itemType") + " x" + ((Number) i.get("quantity")).intValue())
                    .toArray(String[]::new);
            String chosen = (String) JOptionPane.showInputDialog(this, "Select item:", "Use Item",
                    JOptionPane.PLAIN_MESSAGE, null, itemNames, itemNames[0]);
            if (chosen == null) return;

            String itemType = chosen.split(" x")[0];
            List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
            if (heroes.isEmpty()) return;
            String[] heroNames = heroes.stream()
                    .map(h -> h.get("name") + " HP:" + ((Number) h.get("currentHealth")).intValue())
                    .toArray(String[]::new);
            String chosenHero = (String) JOptionPane.showInputDialog(this, "Use on:", "Hero",
                    JOptionPane.PLAIN_MESSAGE, null, heroNames, heroNames[0]);
            if (chosenHero == null) return;
            int idx = java.util.Arrays.asList(heroNames).indexOf(chosenHero);
            long heroId = ((Number) heroes.get(idx).get("id")).longValue();

            Map<String, Object> result = client.getApi().useItem(client.getCurrentPartyId(), itemType, heroId);
            if (result.containsKey("error")) {
                JOptionPane.showMessageDialog(this, result.get("error"));
            } else {
                JOptionPane.showMessageDialog(this, itemType + " used!");
            }
            refreshDisplay();
        });

        continueBtn.addActionListener(e -> client.showPanel("Campaign"));
    }

    private void buyItem(String itemType) {
        if (client.getCurrentPartyId() < 0) return;
        Map<String, Object> result = client.getApi().buyItem(client.getCurrentPartyId(), itemType);
        if (result.containsKey("error")) {
            JOptionPane.showMessageDialog(this, result.get("error"));
        } else {
            int cost = getItemCost(itemType);
            if (client.getCurrentCampaignId() > 0) {
                client.getApi().recordItemPurchase(client.getCurrentCampaignId(), cost);
            }
            JOptionPane.showMessageDialog(this, "Bought " + itemType + "!");
            refreshDisplay();
        }
    }

    private void refreshDisplay() {
        heroPanel.removeAll();
        inventoryPanel.removeAll();

        if (client.getCurrentPartyId() > 0) {
            Map<String, Object> partyData = client.getApi().getParty(client.getCurrentPartyId());
            int gold = 0;
            if (partyData.containsKey("party")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> party = (Map<String, Object>) partyData.get("party");
                gold = ((Number) party.get("gold")).intValue();
            }
            goldLabel.setText("Gold: " + gold);

            List<Map<String, Object>> heroes = client.getApi().getHeroesByParty(client.getCurrentPartyId());
            for (Map<String, Object> h : heroes) {
                boolean alive = h.get("alive") instanceof Boolean ? (Boolean) h.get("alive") : true;
                int hp = ((Number) h.get("currentHealth")).intValue();
                int maxHp = ((Number) h.get("maxHealth")).intValue();
                int mp = ((Number) h.get("currentMana")).intValue();
                int maxMp = ((Number) h.get("maxMana")).intValue();
                String color = alive ? "green" : "red";
                String html = "<html><b>" + h.get("name") + "</b> Lv" + ((Number) h.get("level")).intValue()
                        + "<br>HP: <font color='" + color + "'>" + hp + "/" + maxHp + "</font>"
                        + "<br>MP: " + mp + "/" + maxMp
                        + "<br>" + (alive ? "ALIVE" : "DEAD") + "</html>";
                JLabel card = new JLabel(html);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(java.awt.Color.GRAY),
                        BorderFactory.createEmptyBorder(3, 3, 3, 3)));
                heroPanel.add(card);
            }

            List<Map<String, Object>> inventory = client.getApi().getInventory(client.getCurrentPartyId());
            if (inventory.isEmpty()) {
                inventoryPanel.add(new JLabel("(empty)"));
            }
            for (Map<String, Object> item : inventory) {
                inventoryPanel.add(new JLabel(item.get("itemType") + " x" + ((Number) item.get("quantity")).intValue()));
            }
        }

        heroPanel.revalidate();
        heroPanel.repaint();
        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    private int getItemCost(String itemType) {
        return switch (itemType) {
            case "BREAD" -> 200;
            case "CHEESE" -> 500;
            case "STEAK" -> 1000;
            case "WATER" -> 150;
            case "JUICE" -> 400;
            case "WINE" -> 750;
            case "ELIXIR" -> 2000;
            default -> 0;
        };
    }

    @Override
    public void onShow() {
        refreshDisplay();
    }
}
