package ru.fa.model;

public class DimensionSubtype {

    private long id;
    private String subtype;
    private int num;

    public DimensionSubtype(long id, String subtype, int num) {
        this.id = id;
        this.subtype = subtype;
        this.num = num;
    }

    public long getId() {
        return id;
    }

    public String getSubtype() {
        return subtype;
    }

    public int getNum() {
        return num;
    }
}
