package net.sf.persism;

/**
 * InitializeEvent provides a hook method which is called by Persism whenever it reads an instance
 * of a POJO class from the database.
 *
 * @author Dan Howard
 * @since 2025-08-05 5:41 a.m.
 */
public interface InitializeEvent {

    void onInitialized();

}
