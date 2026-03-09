package net.sf.persism;

public interface InitializeEvent {
    default void onInitialized() {
        System.out.println("PersismEvents.onInitialized " + this.getClass().getName() + " " + this);
    }
}
