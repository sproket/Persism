## ![](img/logo2.png) Cookbook: Using Persism with JavaFX Observable Objects

Persism follows the common getter/setter conventions so when you write a <abbr title="Data Access Object">DAO</abbr> 
type class you can read it from the database.

Example:
~~~java

import net.sf.persism.annotations.Table;

@Table("Players")
public class Player {
    private int playerId;
    private String name;
    private int hitPoints;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public void setHitPoints(int hitPoints) {
        this.hitPoints = hitPoints;
    }
}
~~~

So how would we turn this into an Observable Player instead?

Pretty simple. We can change the basic fields to JavaFX properties instead.

*Note: In IntelliJ when you generate getter/setters for JavaFX properties it will generate the property accessors for you as well. Not sure about other IDEs.*

~~~java
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.sf.persism.annotations.Table;

@Table("Players")
public final class ObservablePlayer {

    private IntegerProperty playerId = new SimpleIntegerProperty(this, "playerId");
    private StringProperty name = new SimpleStringProperty(this, "name");
    private IntegerProperty hitPoints = new SimpleIntegerProperty(this, "hitPoints");

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
}
~~~

Straight forward. This will work fine for querying Player objects from the database. 

### Persistable interface vs PersistableObject base class

An issue you'll see with the above Player class is that when you change fields and save, the UPDATE statement will include all fields.

~~~
ObservablePlayer player = session.fetch(ObservablePlayer.class, params(1));
player.setHitPoints(11);
session.update(player);
~~~

If you have [logging enabled](manual2.md#logging) you'll see something like this:

~~~
UPDATE PLAYERS SET NAME = ?, HIT_POINTS = ? WHERE PLAYER_ID = ? params: [Fred, 11, 1]
~~~

Persism can optionally keep track of changed fields either by extending PersistableObject or implementing the Persistable interface.

So let's change ObservablePlayer class to extend PersistableObject and see what happens. (First change net.sf.persism logging level to INFO)

~~~
@Table("Players")
public final class ObservablePlayer extends PersistableObject<ObservablePlayer> {
...
ObservablePlayer player = session.fetch(ObservablePlayer.class, params(1));
player.setHitPoints(11);
session.update(player);
~~~

In this case you should see:

~~~
No properties changed. No update required for Object: Player{playerId=1, name=Fred, hitPoints=11} class: net.sf.persism.dao.ObservablePlayer
~~~

**What's going on?!!**

The issue is that the default implementation of PersistableObject uses clone() to keep a copy of the original object and then uses this to compare to see if any changes were made.  

Since property objects are indirect, the default clone copies the property *references* and not the *VALUES*.

This doesn't occur with other object types since they are directly changed.

**So how do we handle this?**

Instead of extending PersistableObject we can implement the Persistable interface instead. This interface
has one method to clone the current data object and one method to retrieve this object. Internally Persism
uses this to compare the original the current object to decide what fields were changed. 

In order to do this we'll need to add a default constructor and either a copy constructor or a field constructor.

*Note: These other constructors don't have to be public if you only need them to be used in this context.*

~~~java
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.sf.persism.PersismException;
import net.sf.persism.Persistable;
import net.sf.persism.annotations.Table;

@Table("Players")
public final class ObservablePlayer implements Persistable<ObservablePlayerGood> {

    private final IntegerProperty playerId = new SimpleIntegerProperty(this, "playerId");
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final IntegerProperty hitPoints = new SimpleIntegerProperty(this, "hitPoints");

    private transient ObservablePlayer original;

    public ObservablePlayer() {
    }

    public ObservablePlayer(int playerId, String name, int hitPoints) {
        this.playerId.set(playerId);
        this.name.set(name);
        this.hitPoints.set(hitPoints);
    }

    // note that you can sometimes get false positives for IncompleteCopyConstructor
    public ObservablePlayer(ObservablePlayerGood other) {
        this.playerId.set(other.playerId.get());
        this.name.set(other.name.get());
        this.hitPoints.set(other.hitPoints.get());
        this.original = other.original;
    }

    @Override
    public void saveReadState() throws PersismException {
        original = new ObservablePlayerGood(this);
        // or use this form
        // original = new ObservablePlayerGood(getPlayerId(), getName(), getHitPoints());
    }

    @Override
    public ObservablePlayerGood readOriginalValue() {
        return original;
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
}
~~~

*[See Implementing Persistable interface for further details](cookbook-persistable.md)*


[Sample source code available here](https://github.com/sproket/persismfx)

The sample project should be run with assertions enabled -ea as JVM option.

