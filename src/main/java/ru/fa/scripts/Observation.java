package ru.fa.scripts;

import java.util.Objects;
import java.util.Set;

public class Observation {

    private int id;
    private String strId;
    private Set<Dimension> dimensions;
    private Set<Value> values;

    public Observation(int id, String strId, Set<Dimension> dimensions, Set<Value> values) {
        this.id = id;
        this.strId = strId;
        this.dimensions = dimensions;
        this.values = values;
    }

    public int getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public Set<Dimension> getDimensions() {
        return dimensions;
    }

    public Set<Value> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Observation that = (Observation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Observation{" +
                "id=" + id +
                ", strId='" + strId + '\'' +
                ", dimensions=" + dimensions +
                ", values=" + values +
                '}';
    }
}
