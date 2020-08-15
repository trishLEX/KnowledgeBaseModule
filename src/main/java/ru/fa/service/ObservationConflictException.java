package ru.fa.service;

public class ObservationConflictException extends IllegalStateException {

    public ObservationConflictException(String s) {
        super(s);
    }

    public ObservationConflictException(long id, long anotherId) {
        super("Observations " + id + " and " + anotherId + " have conflicts");
    }
}
