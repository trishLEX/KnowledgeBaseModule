package ru.fa.service;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.fa.FunctionalTest;
import ru.fa.dao.ObservationDao;
import ru.fa.model.Observation;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class ObservationServiceTest extends FunctionalTest {

    @Autowired
    private ObservationService observationService;

    @Autowired
    private ObservationDao observationDao;

    @Test
    void testDifBranches() {
        Map<Long, Observation> observations = observationDao.getObservationsByIds(Arrays.asList(0L, 1L));
        Optional<Pair<Observation, Set<Long>>> dimsToRemove = observationService.dimensionsToRemove(
                observations.get(0L),
                observations.get(1L)
        );
        Assertions.assertTrue(dimsToRemove.isEmpty());
    }

    @Test
    void testSimple() {
        Map<Long, Observation> observations = observationDao.getObservationsByIds(Arrays.asList(1L, 2L));
        Optional<Pair<Observation, Set<Long>>> dimsToRemove = observationService.dimensionsToRemove(
                observations.get(1L),
                observations.get(2L)
        );
        Assertions.assertFalse(dimsToRemove.isEmpty());
        Assertions.assertEquals(2, dimsToRemove.get().getKey().getId());
        Assertions.assertEquals(Set.of(16L, 25L), dimsToRemove.get().getValue());
    }

    @Test
    void testOnlyOne() {
        Map<Long, Observation> observations = observationDao.getObservationsByIds(Arrays.asList(2L, 3L));
        Optional<Pair<Observation, Set<Long>>> dimsToRemove = observationService.dimensionsToRemove(
                observations.get(2L),
                observations.get(3L)
        );
        Assertions.assertFalse(dimsToRemove.isEmpty());
        Assertions.assertEquals(3, dimsToRemove.get().getKey().getId());
        Assertions.assertEquals(Set.of(16L, 17L, 14L), dimsToRemove.get().getValue());
    }
}
