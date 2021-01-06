package ru.fa.model;

public class ObservationValue {

    private long observationId;
    private long valueId;
    private String valueSubtype;

    public ObservationValue(long observationId, long valueId, String valueSubtype) {
        this.observationId = observationId;
        this.valueId = valueId;
        this.valueSubtype = valueSubtype;
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
