package ru.fa.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;
import ru.fa.dao.ObservationDao;
import ru.fa.dto.QuestionResponse;
import ru.fa.model.Dimension;
import ru.fa.model.Observation;
import ru.fa.model.Value;
import ru.fa.util.CustomCollectors;
import ru.fa.util.DimensionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final ObservationDao observationDao;
    private final DimensionDao dimensionDao;

    @Autowired
    public QuestionService(ObservationDao observationDao, DimensionDao dimensionDao) {
        this.observationDao = observationDao;
        this.dimensionDao = dimensionDao;
    }

    public QuestionResponse processNotEmptyQuestion(
            String valueSubType,
            Map<String, Long> dimensions,
            Set<Long> factDimensions
    ) {
        //получаем возможные наблюдения
        Set<Long> observationIds = observationDao.getObservationsIdsByDimensions(factDimensions);
        if (observationIds.isEmpty()) {
            throw new IllegalStateException("Can't find observation for dimensions " + dimensions);
        }

        //проверяем возможный ответ
        Optional<QuestionResponse.Answer> possibleAnswer = checkAnswer(observationIds, valueSubType);
        if (possibleAnswer.isPresent()) {
            return possibleAnswer.get();
        }

        Collection<Observation> observations = observationDao.getObservations(observationIds).values();
        Map<Long, Dimension> dimensionMap = dimensionDao.getDimensions(dimensions.values());
        Map<String, Dimension> inputDimensions = dimensions.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> dimensionMap.get(entry.getValue()))
                );

        //считаем расстояние от input'a до верних наблюдений
        int minDistance = Integer.MAX_VALUE;
        Map<Observation, Integer> observationsDistance = new HashMap<>();
        for (Observation o : observations) {
            int distance = getDistanceToObservation(inputDimensions, o);
            observationsDistance.put(o, distance);
            if (distance < minDistance && distance >= 0) {
                minDistance = distance;
            }
        }
        for (Observation o : observations) {
            //оставляем только наблюдения, которые ниже input'a и ближайшие наблюдения над input'ом
            if (observationsDistance.get(o) > minDistance) {
                observationIds.remove(o.getId());
            }
        }

        //возможно осталось только одно значение
        possibleAnswer = checkAnswer(observationIds, valueSubType);
        if (possibleAnswer.isPresent()) {
            return possibleAnswer.get();
        }

        //находим вид измерения для уточнения
        return getClarifyingQuestion(dimensions, observationIds, dimensionMap, inputDimensions);
    }

    private QuestionResponse.Question getClarifyingQuestion(Map<String, Long> dimensions, Set<Long> observationIds, Map<Long, Dimension> dimensionMap, Map<String, Dimension> inputDimensions) {
        Multimap<String, Long> subTypesToClarifyRaw = observationDao.getDimensionSubTypesToClarify(observationIds);
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
            if (currentDistance < 0 && distance == 0) {
                distance = -1;
                continue;
            }
            distance = Math.max(distance, 0);
            distance += Math.max(currentDistance, 0);
        }
        return distance;
    }

    private Optional<QuestionResponse.Answer> checkAnswer(Collection<Long> observationIds, String valueSubType) {
        if (observationIds.size() == 1) {
            Value value = observationDao.getObservationValue(Iterables.getOnlyElement(observationIds), valueSubType);
            return Optional.of(new QuestionResponse.Answer(
                    value.getStrId(),
                    value.getContent(),
                    valueSubType
            ));
        }

        Set<Value> values = observationDao.getObservationsValues(observationIds, valueSubType);
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
