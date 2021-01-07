package ru.fa.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Objects;

public class Observation {

    @JsonProperty("id")
    private long id;

    @JsonProperty("strId")
    private String strId;

    @JsonProperty("dimensionMap")
    private Map<String, Dimension> dimensionMap;

    @ConstructorProperties({"id", "strId", "dimensionMap"})
    public Observation(long id, String strId, Map<String, Dimension> dimensionMap) {
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

    public Map<String, Dimension> getDimensionMap() {
        return dimensionMap;
    }

    public Dimension getDimension(String subType) {
        return dimensionMap.get(subType);
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
