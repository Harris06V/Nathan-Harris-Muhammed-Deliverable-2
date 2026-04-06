package com.legendsofsw.partyservice.model;

public enum ItemType {
    BREAD(200, 20, 0, false),
    CHEESE(500, 50, 0, false),
    STEAK(1000, 200, 0, false),
    WATER(150, 0, 10, false),
    JUICE(400, 0, 30, false),
    WINE(750, 0, 100, false),
    ELIXIR(2000, 0, 0, true);

    private final int cost;
    private final int hpRestore;
    private final int manaRestore;
    private final boolean reviveAndFull;

    ItemType(int cost, int hpRestore, int manaRestore, boolean reviveAndFull) {
        this.cost = cost;
        this.hpRestore = hpRestore;
        this.manaRestore = manaRestore;
        this.reviveAndFull = reviveAndFull;
    }

    public int getCost() {
        return cost;
    }

    public int getHpRestore() {
        return hpRestore;
    }

    public int getManaRestore() {
        return manaRestore;
    }

    public boolean isReviveAndFull() {
        return reviveAndFull;
    }
}
