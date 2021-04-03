package net.sf.persism;

public class Convertor<T extends Types> {

    Types source;

    public Convertor(T source) {
        this.source = source;
    }

    public Object to(Types target, Object value) {
        return value;
    }
}
