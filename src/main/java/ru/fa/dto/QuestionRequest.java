package ru.fa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.fa.model.DimensionSubType;
import ru.fa.model.ValueSubType;

import java.beans.ConstructorProperties;
import java.util.Map;

public class QuestionRequest {

    @JsonProperty("value_subtype")
    private ValueSubType valueSubType;

    @JsonProperty("dimensions")
    private Map<DimensionSubType, String> dimensions;

    @ConstructorProperties({"value_subtype", "dimensions"})
    public QuestionRequest(ValueSubType valueSubType, Map<DimensionSubType, String> dimensions) {
        this.valueSubType = valueSubType;
        this.dimensions = dimensions;
    }

    public ValueSubType getValueSubType() {
        return valueSubType;
    }

    public Map<DimensionSubType, String> getDimensions() {
        return dimensions;
    }

    @Override
    public String toString() {
        return "QuestionRequest{" +
                "valueSubType=" + valueSubType +
                ", dimensions=" + dimensions +
                '}';
    }
}
