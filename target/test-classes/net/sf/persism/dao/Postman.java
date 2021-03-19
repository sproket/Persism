package net.sf.persism.dao;

/**
 * Comments for Postman go here.
 *
 * @author Dan Howard
 * @since 11/16/14 9:46 AM
 */
public final class Postman {

    private  String host;
    private  int port;
    private  String user;
    private  String password;

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
