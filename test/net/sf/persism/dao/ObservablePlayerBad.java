package net.sf.persism.dao;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.sf.persism.InitializeEvent;
import net.sf.persism.PersistableObject;
import net.sf.persism.annotations.Table;

/*
    This version doesn't work for UPDATE because setters mutate existing properties.
    PersistableObject uses clone() by default which is a shallow copy.
    @See ObservablePlayer for proper implementation for observable type classes.
 */
@Table("Players")
public final class ObservablePlayerBad extends PersistableObject<ObservablePlayerBad> implements InitializeEvent {

    private final IntegerProperty playerId = new SimpleIntegerProperty(this, "playerId");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final IntegerProperty hitPoints = new SimpleIntegerProperty(this, "hitPoints");

    public ObservablePlayerBad() {
    }

    public ObservablePlayerBad(int playerId, String name, int hitPoints) {
        this.playerId.set(playerId);
        this.name.set(name);
        this.hitPoints.set(hitPoints);
    }

    public ObservablePlayerBad(ObservablePlayerBad other) {
        this.playerId.set(other.playerId.get());
        this.name.set(other.name.get());
        this.hitPoints.set(other.hitPoints.get());
    }

    public int getPlayerId() {
        return playerId.get();
    }

    public IntegerProperty playerIdProperty() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId.set(playerId);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getHitPoints() {
        return hitPoints.get();
    }

    public IntegerProperty hitPointsProperty() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints.set(hitPoints);
    }

    @Override
    public String toString() {
        return "Player{" +
                "playerId=" + playerId.get() +
                ", name=" + name.get() +
                ", hitPoints=" + hitPoints.get() +
                '}';
    }
}