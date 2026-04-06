package com.legendsofsw.partyservice.observer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class ObserverPatternTest {

    // concrete subject for testing
    static class TestSubject implements Subject {
        private final List<GameObserver> observers = new ArrayList<>();
        private String lastHero;
        private int lastLevel;
        private String lastClass;

        @Override
        public void attach(GameObserver observer) {
            observers.add(observer);
        }

        @Override
        public void detach(GameObserver observer) {
            observers.remove(observer);
        }

        @Override
        public void notifyObservers() {
            for (GameObserver obs : observers) {
                obs.onHeroLevelUp(lastHero, lastLevel, lastClass);
            }
        }

        public void levelUpHero(String name, int level, String className) {
            this.lastHero = name;
            this.lastLevel = level;
            this.lastClass = className;
            notifyObservers();
        }
    }

    // concrete observer for testing
    static class TestObserver implements GameObserver {
        String lastLevelUpHero;
        int lastLevelUpLevel;
        String lastJoinedHero;
        String lastDefeatedHero;

        @Override
        public void onHeroLevelUp(String heroName, int newLevel, String className) {
            this.lastLevelUpHero = heroName;
            this.lastLevelUpLevel = newLevel;
        }

        @Override
        public void onHeroJoinedParty(String heroName, String partyName) {
            this.lastJoinedHero = heroName;
        }

        @Override
        public void onHeroDefeated(String heroName) {
            this.lastDefeatedHero = heroName;
        }
    }

    @Test
    void attachAndNotifyObserver() {
        TestSubject subject = new TestSubject();
        TestObserver observer = new TestObserver();

        subject.attach(observer);
        subject.levelUpHero("Knight", 5, "WARRIOR");

        assertEquals("Knight", observer.lastLevelUpHero);
        assertEquals(5, observer.lastLevelUpLevel);
    }

    @Test
    void detachStopsNotifications() {
        TestSubject subject = new TestSubject();
        TestObserver observer = new TestObserver();

        subject.attach(observer);
        subject.detach(observer);
        subject.levelUpHero("Mage", 3, "MAGE");

        assertNull(observer.lastLevelUpHero);
    }

    @Test
    void multipleObserversAllNotified() {
        TestSubject subject = new TestSubject();
        TestObserver obs1 = new TestObserver();
        TestObserver obs2 = new TestObserver();

        subject.attach(obs1);
        subject.attach(obs2);
        subject.levelUpHero("Healer", 2, "ORDER");

        assertEquals("Healer", obs1.lastLevelUpHero);
        assertEquals("Healer", obs2.lastLevelUpHero);
    }

    @Test
    void observerReceivesJoinedEvent() {
        TestObserver observer = new TestObserver();
        observer.onHeroJoinedParty("Recruit", "Alpha Party");
        assertEquals("Recruit", observer.lastJoinedHero);
    }

    @Test
    void observerReceivesDefeatedEvent() {
        TestObserver observer = new TestObserver();
        observer.onHeroDefeated("FallenHero");
        assertEquals("FallenHero", observer.lastDefeatedHero);
    }
}
