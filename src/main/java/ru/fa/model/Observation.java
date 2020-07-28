package ru.fa.model;

import java.util.Map;
import java.util.Objects;

public class Observation {

    private long id;
    private String strId;
    private Map<DimensionSubType, Dimension> dimensionMap;

    public Observation(long id, String strId, Map<DimensionSubType, Dimension> dimensionMap) {
        this.id = id;
        this.strId = strId;
        this.dimensionMap = dimensionMap;
    }

    public long getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public Map<DimensionSubType, Dimension> getDimensionMap() {
        return dimensionMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
                ", dimensionMap=" + dimensionMap +
                '}';
    }
}
