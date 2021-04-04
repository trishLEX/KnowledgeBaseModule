package ru.fa.model;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObservationValue {

    @JsonProperty("id")
    private long id;

    @JsonProperty("observationId")
    private long observationId;

    @JsonProperty("valueId")
    private long valueId;

    @JsonProperty("valueSubtype")
    private String valueSubtype;

    @ConstructorProperties({"id", "observationId", "valueId", "valueSubtype"})
    public ObservationValue(long id, long observationId, long valueId, String valueSubtype) {
        this.id = id;
        this.observationId = observationId;
        this.valueId = valueId;
        this.valueSubtype = valueSubtype;
    }

    public long getId() {
        return id;
    }

    public long getObservationId() {
        return observationId;
    }

    public long getValueId() {
        return valueId;
    }

    public String getValueSubtype() {
        return valueSubtype;
    }
}
