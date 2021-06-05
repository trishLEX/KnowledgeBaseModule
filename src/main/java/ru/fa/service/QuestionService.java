package ru.fa.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;
import ru.fa.dto.QuestionResponse;
import ru.fa.model.Dimension;
import ru.fa.model.Observation;
import ru.fa.model.Value;
import ru.fa.util.CustomCollectors;
import ru.fa.util.DimensionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final DimensionDao dimensionDao;
    private final ObservationService observationService;

    @Autowired
    public QuestionService(DimensionDao dimensionDao, ObservationService observationService) {
        this.dimensionDao = dimensionDao;
        this.observationService = observationService;
    }

    public QuestionResponse processNotEmptyQuestion(
            String valueSubType,
            Map<String, Long> dimensions,
            Set<Long> factDimensions
    ) {
        //получаем возможные наблюдения
        Set<Long> observationIds = observationService.getObservationsIdsByDimensions(factDimensions);
        if (observationIds.isEmpty()) {
            throw new IllegalStateException("Can't find observation for dimensions " + dimensions);
        }

        //проверяем возможный ответ
        Optional<QuestionResponse> possibleAnswer = checkAnswer(observationIds, valueSubType);
        if (possibleAnswer.isPresent()) {
            return possibleAnswer.get();
        }

        Collection<Observation> observations = observationService.getObservationsByIds(observationIds);
        Map<Long, Dimension> dimensionMap = dimensionDao.getDimensions(dimensions.values());
        Map<String, Dimension> inputDimensions = dimensions.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> dimensionMap.get(entry.getValue()))
                );

        //считаем расстояние от input'a до верних наблюдений
        var minDistance = Integer.MAX_VALUE;
        var observationsDistance = new HashMap<Observation, Integer>();
        var crossObservations = new ArrayList<Observation>();
        for (Observation o : observations) {
            int distance = getDistanceToObservation(inputDimensions, o);
            observationsDistance.put(o, distance);
            if (distance == -2) {
                crossObservations.add(o);
            } else if (distance < minDistance && distance >= 0) {
                minDistance = distance;
            }
        }



        for (Observation o : observations) {
            //оставляем только наблюдения, которые ниже input'a и ближайшие наблюдения над input'ом
            if (observationsDistance.get(o) > minDistance) {
                observationIds.remove(o.getId());
            }
        }

        getCrossObservationsToRemove(crossObservations).forEach(o -> observationIds.remove(o.getId()));

        //возможно осталось только одно значение
        possibleAnswer = checkAnswer(observationIds, valueSubType);
        return possibleAnswer.orElseGet(
                () -> getClarifyingQuestion(dimensions, observationIds, dimensionMap, inputDimensions)
        );
    }

    private List<Observation> getCrossObservationsToRemove(List<Observation> crossObservations) {
        if (crossObservations.size() < 2) {
            return Collections.emptyList();
        }
        var observationsToRemove = new ArrayList<Observation>();
        var diffBranchesObservations = new ArrayList<Observation>();
        var current = crossObservations.get(0);
        for (var o : crossObservations.subList(1, crossObservations.size())) {
            ObservationCompareResult compareResult = observationService.checkObservationsLevel(current, o);
            switch (compareResult) {
                case ONE_HIGHER_ANOTHER:
                    observationsToRemove.add(current);
                    current = o;
                    break;
                case ONE_LOWER_ANOTHER:
                    observationsToRemove.add(o);
                    break;
                case DIFFERENT_BRANCHES:
                    diffBranchesObservations.add(o);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported result: " + compareResult);
            }
        }
        if (!diffBranchesObservations.isEmpty()) {
            observationsToRemove.addAll(getCrossObservationsToRemove(diffBranchesObservations));
        }
        return observationsToRemove;
    }

    private QuestionResponse.Question getClarifyingQuestion(
            Map<String, Long> dimensions,
            Set<Long> observationIds,
            Map<Long, Dimension> dimensionMap,
            Map<String, Dimension> inputDimensions
    ) {
        //вид измерения -> список измерений, по которым есть пересечения
        Multimap<String, Long> subTypesToClarifyRaw = observationService.getDimensionSubTypesToClarify(observationIds);
        dimensionMap.putAll(dimensionDao.getDimensions(subTypesToClarifyRaw.values()));
        Multimap<String, Dimension> subtypesToClarify = subTypesToClarifyRaw
                .entries()
                .stream()
                .collect(CustomCollectors.toLinkedHashMultimap(
                        Map.Entry::getKey,
                        entry -> dimensionMap.get(entry.getValue())
                ));

        try {
            String subtypeToClarify = getDimensionSubtypeToClarify(subtypesToClarify, inputDimensions);
            String question = dimensionMap.get(dimensions.get(subtypeToClarify)).getQuestion();
            return new QuestionResponse.Question(question, subtypeToClarify);
        } catch (ObservationConflictException e) {
            throw new IllegalStateException("Observations " + observationIds + " have conflicts");
        }
    }

    private String getDimensionSubtypeToClarify(
            Multimap<String, Dimension> subtypesToClarify,
            Map<String, Dimension> inputDimensions
    ) throws ObservationConflictException {
        for (String subType : subtypesToClarify.keySet()) {
            List<Dimension> upper = new ArrayList<>();
            List<Dimension> equals = new ArrayList<>();
            List<Dimension> lower = new ArrayList<>();
            Dimension input = inputDimensions.get(subType);

            if (input.getAllChildrenIds().isEmpty()) {
                continue;
            }

            for (Dimension dimension : subtypesToClarify.get(subType)) {
                DimensionsUtil.compareDimensions(upper, equals, lower, dimension, input);
            }

            if (upper.isEmpty() && equals.isEmpty() && !lower.isEmpty()
                    || upper.isEmpty() && !equals.isEmpty() && !lower.isEmpty()
                    || !upper.isEmpty() && equals.isEmpty() && !lower.isEmpty()
                    || !upper.isEmpty() && !equals.isEmpty() && !lower.isEmpty()
            ) {
                return subType;
            }
        }

        throw new ObservationConflictException("Seems to be conflicts of observations");
    }

    private int getDistanceToObservation(Map<String, Dimension> input, Observation observation) {
        int distance = 0;
        for (var subtype : input.keySet()) {
            var dimension = input.get(subtype);
            var observationDimension = observation.getDimension(subtype);
            var currentDistance = dimension.getLevel() - observationDimension.getLevel();
            if (distance * currentDistance < 0) {
                return -2;
            }
            if (currentDistance < 0) {
                distance = -1;
                continue;
            }
            distance += currentDistance;
        }
        return distance;
    }

    private Optional<QuestionResponse> checkAnswer(Collection<Long> observationIds, String valueSubType) {
        if (observationIds.size() == 1) {
            Value value = observationService.getObservationValue(Iterables.getOnlyElement(observationIds), valueSubType);
            return Optional.of(new QuestionResponse.Answer(
                    value.getStrId(),
                    value.getContent(),
                    valueSubType
            ));
        }

        Set<Value> values = observationService.getObservationsValues(observationIds, valueSubType);
        if (values.size() == 1) {
            return Optional.of(new QuestionResponse.Answer(
                    Iterables.getOnlyElement(values).getStrId(),
                    Iterables.getOnlyElement(values).getContent(),
                    valueSubType
            ));
        }

        return Optional.empty();
    }
}
