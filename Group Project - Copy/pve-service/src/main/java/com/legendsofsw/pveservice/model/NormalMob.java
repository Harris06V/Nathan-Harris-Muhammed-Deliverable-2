package com.legendsofsw.pveservice.model;

public class NormalMob extends Mob {

    public NormalMob() {
        super();
    }

    public NormalMob(String name, int level) {
        super(name, level,
              5 + level * 2,    // power: base 5 + 2 per level
              5 + level,        // defense: base 5 + 1 per level
              100 + level * 10  // hp: base 100 + 10 per level
        );
    }

    @Override
    public double getDamageMultiplier() {
        return 1.0;
    }
}
