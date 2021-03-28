package ru.fa.model;

public class ObservationValue {

    private long id;
    private long observationId;
    private long valueId;
    private String valueSubtype;

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
