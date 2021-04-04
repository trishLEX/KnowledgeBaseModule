package ru.fa.generator;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.fa.model.Observation;
import ru.fa.model.Value;

public class ValueGenerator {

    private final List<Observation> observations;

    public ValueGenerator(List<Observation> observations) {
        this.observations = observations;
    }

    public List<Value> createValues() {
        return observations.stream()
                .map(o -> new Value(
                        o.getId(),
                        "Value" + o.getId(),
                        new ObjectNode(JsonNodeFactory.instance)
                                .put("id", o.getId())
                                .put("body", "superBody"),
                        "VALUE_TYPE_1"
                )).collect(Collectors.toList());
    }
}
