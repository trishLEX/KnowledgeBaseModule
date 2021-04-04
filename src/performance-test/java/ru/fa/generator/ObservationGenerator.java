package ru.fa.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.fa.model.Dimension;
import ru.fa.model.Observation;

public class ObservationGenerator {

    private final int components;
    private final int vertices;
    private final int childSize;
    private final Map<Long, Dimension> dimensionMap;
    private final List<Dimension> roots;

    public ObservationGenerator(int components, int vertices, int childSize, Map<Long, Dimension> dimensionMap,
                                List<Dimension> roots) {
        this.components = components;
        this.vertices = vertices;
        this.childSize = childSize;
        this.dimensionMap = dimensionMap;
        this.roots = roots;
    }

    public List<Observation> createObservations() {
        List<Map<String, Dimension>> observationDimensions = createObservations(0);
        List<Observation> observations = new ArrayList<>();
        for (int i = 0; i < observationDimensions.size(); i++) {
            observations.add(new Observation(
                    i,
                    "Observation" + i,
                    observationDimensions.get(i)
            ));
        }
        return observations;
    }

    private List<Map<String, Dimension>> createObservations(int rootIndex) {
        List<Dimension> current = getDimensionsForObservation(roots.get(rootIndex));
        current.add(roots.get(rootIndex));
        if (rootIndex == components - 1) {
            return current.stream()
                    .map(d -> {
                        Map<String, Dimension> map = new HashMap<>();
                        map.put(d.getDimensionSubType(), d);
                        return map;
                    })
                    .collect(Collectors.toList());
        }

        List<Map<String, Dimension>> next = createObservations(rootIndex + 1);

        List<Map<String, Dimension>> result = new ArrayList<>();
        for (Dimension dimension : current) {
            for (Map<String, Dimension> dimensionMap : next) {
                Map<String, Dimension> map = new HashMap<>(dimensionMap);
                map.put(dimension.getDimensionSubType(), dimension);
                result.add(map);
            }
        }
        return result;
    }

    private List<Dimension> getDimensionsForObservation(Dimension root) {
        List<Dimension> dimensions = root.getChildrenIds()
                .stream()
                .limit(childSize - 1)
                .map(dimensionMap::get)
                .collect(Collectors.toList());
        List<Dimension> observationDimensions = new ArrayList<>(dimensions);
        dimensions.stream()
                .map(this::getDimensionsForObservation)
                .forEach(observationDimensions::addAll);
        return observationDimensions;
    }
}
