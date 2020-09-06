package ru.fa.service;

import ru.fa.model.Dimension;

import java.util.Collection;

public class ObservationConflictException extends IllegalStateException {

    public ObservationConflictException(String s) {
        super(s);
    }

    public ObservationConflictException(long id, long anotherId) {
        super("Observations " + id + " and " + anotherId + " are same");
    }

    public ObservationConflictException(long id, long anotherId, Collection<Dimension> dimensionsToResolve) {
        super("Observations " + id + " and " + anotherId + " have conflicts. " +
                "Possible solution: " + dimensionsToResolve
        );
    }
}
