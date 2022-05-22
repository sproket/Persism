package net.sf.persism.dao;

import net.sf.persism.annotations.Column;

/**
 * Comments for Postman go here.
 *
 * @author Dan Howard
 * @since 11/16/14 9:46 AM
 */
public final class Postman {

    @Column(primary = true, autoIncrement = true)
    private String auto;

    private String host;
    private int port;
    private String user;
    private String password;
    private int missingGetter;

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String user() {
        return user;
    }

    public String password() {
        return password;
    }

    public Postman missingGetter(int missingGetter) {
        this.missingGetter = missingGetter;
        return this;
    }

    public Postman host(String host) {
        this.host = host;
        return this;
    }

    public Postman port(int port) {
        this.port = port;
        return this;
    }

    public Postman user(String user) {
        this.user = user;
        return this;
    }

    public Postman password(String password) {
        this.password = password;
        return this;
    }

    public String auto() {
        return auto;
    }

    public Postman setAuto(String auto) {
        this.auto = auto;
        return this;
    }

    @Override
    public String toString() {
        return "Postman{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
