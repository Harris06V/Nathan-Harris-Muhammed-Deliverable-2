package com.legendsofsw.partyservice.observer;

public interface GameObserver {

    void onHeroLevelUp(String heroName, int newLevel, String className);

    void onHeroJoinedParty(String heroName, String partyName);

    void onHeroDefeated(String heroName);
}
