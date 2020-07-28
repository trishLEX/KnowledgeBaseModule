package ru.fa.model;

public class ObservationDimension {

    private long dimensionId;
    private long observationId;
    private long observationDimensionId;
    private DimensionSubType dimensionSubType;

    public ObservationDimension(
            long dimensionId,
            long observationId,
            long observationDimensionId,
            DimensionSubType dimensionSubType
    ) {
        this.dimensionId = dimensionId;
        this.observationId = observationId;
        this.observationDimensionId = observationDimensionId;
        this.dimensionSubType = dimensionSubType;
    }

    public long getDimensionId() {
        return dimensionId;
    }

    public long getObservationId() {
        return observationId;
    }

    public long getObservationDimensionId() {
        return observationDimensionId;
    }

    public DimensionSubType getDimensionSubType() {
        return dimensionSubType;
    }
}
