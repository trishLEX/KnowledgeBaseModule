package ru.fa.service;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.fa.FunctionalTest;
import ru.fa.dao.DimensionDao;
import ru.fa.dao.ObservationDao;
import ru.fa.model.Dimension;
import ru.fa.model.Observation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class ObservationServiceTest extends FunctionalTest {

    @Autowired
    private ObservationService observationService;

    @Autowired
    private ObservationDao observationDao;

    @Autowired
    private DimensionDao dimensionDao;

    @Test
    void testDifBranches() {
        Map<Long, Observation> observations = observationDao.getObservations(Arrays.asList(0L, 1L));
        Optional<ObservationDimensionsToRemove> dimsToRemove = observationService.dimensionsToRemove(
                observations.get(0L),
                observations.get(1L)
        );
        Assertions.assertTrue(dimsToRemove.isEmpty());
    }

    @Test
    void testSimple() {
        Map<Long, Observation> observations = observationDao.getObservations(Arrays.asList(1L, 2L));
        Optional<ObservationDimensionsToRemove> dimsToRemove = observationService.dimensionsToRemove(
                observations.get(1L),
                observations.get(2L)
        );
        Assertions.assertFalse(dimsToRemove.isEmpty());
        Assertions.assertEquals(2, dimsToRemove.get().getObservation().getId());
        Assertions.assertEquals(Set.of(16L, 25L), dimsToRemove.get().getDimensionIds());
    }

    @Test
    void testOnlyOne() {
        Map<Long, Observation> observations = observationDao.getObservations(Arrays.asList(2L, 3L));
        Optional<ObservationDimensionsToRemove> dimsToRemove = observationService.dimensionsToRemove(
                observations.get(2L),
                observations.get(3L)
        );
        Assertions.assertFalse(dimsToRemove.isEmpty());
        Assertions.assertEquals(3, dimsToRemove.get().getObservation().getId());
        Assertions.assertEquals(Set.of(16L, 17L, 14L), dimsToRemove.get().getDimensionIds());
    }

    @Test
    void testConflictObservation() {
        Observation observation = observationDao.getObservation(2);

        Map<Long, Dimension> dimensions = dimensionDao.getDimensions(List.of(1L, 16L, 22L));

        Observation observationToConflict = new Observation(
                100500,
                "conflict",
                ImmutableMap.of(
                        "SUBTYPE_1", dimensions.get(1L),
                        "SUBTYPE_2", dimensions.get(16L),
                        "SUBTYPE_3", dimensions.get(22L)
                )
        );
        Assertions.assertThrows(
                ObservationConflictException.class,
                () -> observationService.checkObservationsLevel(observation, observationToConflict)
        );
    }

    @Test
    void testSameObservation() {
        Observation observation = observationDao.getObservation(2);

        Assertions.assertThrows(
                ObservationConflictException.class,
                () -> observationService.checkObservationsLevel(observation, observation)
        );
    }

    @Test
    void testValidIntersection() {
        Map<Long, Observation> observations = observationDao.getObservations(List.of(0L, 1L));

        Assertions.assertDoesNotThrow(
                () -> observationService.checkObservationsLevel(observations.get(0L), observations.get(1L))
        );
    }

    @Test
    void testCreatingObservations() {
        observationDao.deleteObservation(0);
        observationDao.deleteObservation(1);
        observationDao.deleteObservation(2);
        observationDao.deleteObservation(3);

        Multimap<Long, Long> obsDims = LinkedListMultimap.create();
        obsDims.putAll(0L, List.of(4L, 16L, 25L));
        obsDims.putAll(1L, List.of(5L, 16L, 25L));
        obsDims.putAll(2L, List.of(5L, 14L, 22L));
        obsDims.putAll(3L, List.of(5L, 10L, 22L));

        Observation observation0 = new Observation(0, "Observation0", getDimensionMap(obsDims.get(0L)));
        Observation observation1 = new Observation(1, "Observation1", getDimensionMap(obsDims.get(1L)));
        Observation observation2 = new Observation(2, "Observation2", getDimensionMap(obsDims.get(2L)));
        Observation observation3 = new Observation(3, "Observation3", getDimensionMap(obsDims.get(3L)));

        observationService.insertObservation(observation0);
        observationService.insertObservation(observation1);
        observationService.insertObservation(observation2);
        observationService.insertObservation(observation3);
    }

    private Map<String, Dimension> getDimensionMap(Collection<Long> dimIds) {
        return dimensionDao.getDimensions(dimIds).values()
                .stream()
                .collect(Collectors.toMap(
                        Dimension::getDimensionSubType,
                        Functions.identity()
                ));
    }
}
