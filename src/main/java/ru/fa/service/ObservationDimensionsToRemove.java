package ru.fa.service;

import ru.fa.model.Observation;

import java.util.Set;

public class ObservationDimensionsToRemove {

    private Observation observation;
    private Set<Long> dimensionIds;

    public ObservationDimensionsToRemove(Observation observation, Set<Long> dimensionIds) {
        this.observation = observation;
        this.dimensionIds = dimensionIds;
    }

    public Observation getObservation() {
        return observation;
    }

    public Set<Long> getDimensionIds() {
        return dimensionIds;
    }
}
