package ru.fa.generator;

import java.util.List;
import java.util.stream.Collectors;

import ru.fa.model.Observation;
import ru.fa.model.ObservationValue;

public class ObservationValueGenerator {

    private final List<Observation> observations;

    public ObservationValueGenerator(List<Observation> observations) {
        this.observations = observations;
    }

    public List<ObservationValue> createObservationValues() {
        return observations.stream()
                .map(o -> new ObservationValue(o.getId(), o.getId(), o.getId(), "VALUE_SUBTYPE_1"))
                .collect(Collectors.toList());
    }
}
