package ru.fa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.ObservationDao;
import ru.fa.model.Dimension;
import ru.fa.model.Observation;
import ru.fa.util.DimensionsUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ParametersAreNonnullByDefault
public class ObservationService {

    private final ObservationDao observationDao;

    @Autowired
    public ObservationService(ObservationDao observationDao) {
        this.observationDao = observationDao;
    }

    public Optional<DimensionsToRemove> dimensionsToRemove(
            Observation observation,
            Observation anotherObservation
    ) {
        switch (checkObservationsLevel(observation, anotherObservation)) {
            case DIFFERENT_BRANCHES:
                return Optional.empty();
            case ONE_HIGHER_ANOTHER:
                return Optional.of(
                        new DimensionsToRemove(
                                observation,
                                getDimensionsToRemove(observation, anotherObservation)
                        )
                );
            case ONE_LOWER_ANOTHER:
                return Optional.of(
                        new DimensionsToRemove(
                                anotherObservation,
                                getDimensionsToRemove(anotherObservation, observation)
                        )
                );
            default:
                throw new UnsupportedOperationException();
        }
    }

    private Set<Long> getDimensionsToRemove(Observation higher, Observation lower) {
        Set<Long> lowerDims = lower.getDimensionMap()
                .values()
                .stream()
                .flatMap(d -> Stream.concat(Stream.of(d.getId()), d.getAllChildrenIds().stream()))
                .collect(Collectors.toSet());
        return higher.getDimensionMap()
                .values()
                .stream()
                .filter(d -> !lowerDims.contains(d.getId()))
                .flatMap(d -> d.getAllChildrenIds().stream())
                .filter(lowerDims::contains)
                .collect(Collectors.toSet());
    }

    private ObservationCompareResult checkObservationsLevel(long observationId, long anotherObservationId) {
        Map<Long, Observation> observations = observationDao.getObservations(
                Arrays.asList(
                        observationId,
                        anotherObservationId
                )
        );
        return checkObservationsLevel(observations.get(observationId), observations.get(anotherObservationId));
    }

    public ObservationCompareResult checkObservationsLevel(Observation observation, Observation anotherObservation) {
        List<Dimension> upper = new ArrayList<>();
        List<Dimension> equals = new ArrayList<>();
        List<Dimension> lower = new ArrayList<>();
        for (String dimSubType : observation.getDimensionMap().keySet()) {
            Dimension dimension = observation.getDimension(dimSubType);
            Dimension anotherDimension = anotherObservation.getDimension(dimSubType);

            boolean isOneBranch = DimensionsUtil.isOneBranch(dimension, anotherDimension);
            if (!isOneBranch) {
                return ObservationCompareResult.DIFFERENT_BRANCHES;
            }
            DimensionsUtil.compareDimensions(upper, equals, lower, anotherDimension, dimension);
        }

        if (equals.size() == observation.getDimensionMap().keySet().size()) {
            throw new ObservationConflictException(observation.getId(), anotherObservation.getId());
        }

        if (!upper.isEmpty() && !lower.isEmpty()) {
            List<Dimension> solution = upper.stream()
                    .map(Dimension::getDimensionSubType)
                    .map(observation::getDimension)
                    .collect(Collectors.toList());
            solution.addAll(lower);
            solution.addAll(equals);
            throw new ObservationConflictException(observation.getId(), anotherObservation.getId(), solution);
        }

        if (lower.isEmpty()) {
            return ObservationCompareResult.ONE_LOWER_ANOTHER;
        } else {
            return ObservationCompareResult.ONE_HIGHER_ANOTHER;
        }
    }

    enum ObservationCompareResult {
        DIFFERENT_BRANCHES,
        ONE_HIGHER_ANOTHER,
        ONE_LOWER_ANOTHER
    }
}
