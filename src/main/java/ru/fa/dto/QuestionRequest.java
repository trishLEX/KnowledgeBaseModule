package ru.fa.dto;

import java.beans.ConstructorProperties;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuestionRequest {

    @JsonProperty("value_subtype")
    private String valueSubType;

    @JsonProperty("dimensions")
    private Map<String, String> dimensions;

    @ConstructorProperties({"value_subtype", "dimensions"})
    public QuestionRequest(String valueSubType, Map<String, String> dimensions) {
        this.valueSubType = valueSubType;
        this.dimensions = dimensions;
    }

    public String getValueSubType() {
        return valueSubType;
    }

    public Map<String, String> getDimensions() {
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
