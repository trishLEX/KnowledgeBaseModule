package ru.fa.scripts.model;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ru.fa.model.Value;

public class Observation {

    private long id;
    private String strId;
    private Set<Dimension> dimensions;
    private Set<Value> values;
    private Map<DimensionSubType, Dimension> dimensionMap;

    public Observation(long id, String strId, Set<Dimension> dimensions, Set<Value> values) {
        this.id = id;
        this.strId = strId;
        this.dimensions = dimensions;
        this.values = values;
        this.dimensionMap = dimensions.stream().collect(Collectors.toMap(Dimension::getDimensionSubType, Function.identity()));
    }

    public long getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public Set<Dimension> getDimensions() {
        return dimensions;
    }

    public Map<DimensionSubType, Dimension> getDimensionMap() {
        return dimensionMap;
    }

    public Dimension getDimension(DimensionSubType dimensionSubType) {
        return dimensionMap.get(dimensionSubType);
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
