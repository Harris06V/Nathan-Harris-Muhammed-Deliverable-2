package com.legendsofsw.partyservice.observer;

public interface Subject {

    void attach(GameObserver observer);

    void detach(GameObserver observer);

    void notifyObservers();
}
