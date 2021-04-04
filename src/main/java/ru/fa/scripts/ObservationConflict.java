package ru.fa.scripts;

import java.util.Set;

import ru.fa.scripts.model.Dimension;
import ru.fa.scripts.model.Observation;

public class ObservationConflict {
    /**
     * Пара конфликтующих правил
     */
    private final Set<Observation> conflictedNodes;
    /**
     * Пересечение правил (оно же решение)
     */
    private final Set<Dimension> intersection;

    public ObservationConflict(Set<Observation> conflictedNodes, Set<Dimension> intersection) {
        this.conflictedNodes = conflictedNodes;
        this.intersection = intersection;
    }

    public Set<Observation> getConflictedNodes() {
        return conflictedNodes;
    }

    public Set<Dimension> getIntersection() {
        return intersection;
    }
}
