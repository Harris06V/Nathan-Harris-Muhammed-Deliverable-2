package com.legendsofsw.partyservice.model;

public enum HybridClass {
    NONE,
    PRIEST,      // Order + Order 
    HERETIC,     // Order + Chaos
    PALADIN,     // Order + Warrior
    PROPHET,     // Order + Mage
    INVOKER,     // Chaos + Chaos 
    ROGUE,       // Chaos + Warrior
    SORCERER,    // Chaos + Mage
    KNIGHT,      // Warrior + Warrior
    WARLOCK,     // Warrior + Mage
    WIZARD       // Mage + Mage 
}
