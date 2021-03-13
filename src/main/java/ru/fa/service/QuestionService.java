package ru.fa.service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;
import ru.fa.dao.ObservationDao;
import ru.fa.dto.QuestionResponse;
import ru.fa.model.Dimension;
import ru.fa.model.Value;
import ru.fa.util.CustomCollectors;
import ru.fa.util.DimensionsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public QuestionResponse processNotEmptyQuestion(String valueSubType, Map<String, Long> dimensions) {
        Set<Long> observationIds = getObservationIds(dimensions);

        if (observationIds.isEmpty()) {
            throw new IllegalStateException("Can't find observation for dimensions " + dimensions);
        } else if (observationIds.size() == 1) {
            Value value = observationDao.getObservationValue(Iterables.getOnlyElement(observationIds), valueSubType);
            return new QuestionResponse.Answer(
                    value.getStrId(),
                    value.getContent(),
                    valueSubType
            );
        } else {
            Multimap<String, Long> subTypesToClarifyRaw =
                    observationDao.getDimensionSubTypesToClarify(observationIds);
            Map<Long, Dimension> dimensionMap = dimensionDao.getDimensions(
                    Sets.union(
                            Sets.newHashSet(subTypesToClarifyRaw.values()),
                            Sets.newHashSet(dimensions.values())
                    )
            );

            Multimap<String, Dimension> subtypesToClarify = subTypesToClarifyRaw
                    .entries()
                    .stream()
                    .collect(CustomCollectors.toLinkedHashMultimap(
                            Map.Entry::getKey,
                            entry -> dimensionMap.get(entry.getValue())
                    ));
            Map<String, Dimension> inputDimensions = dimensions.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> dimensionMap.get(entry.getValue()))
                    );

            try {
                String subtypeToClarify = getDimensionSubtypeToClarify(subtypesToClarify, inputDimensions);
                String question = dimensionMap.get(dimensions.get(subtypeToClarify)).getQuestion();
                return new QuestionResponse.Question(question, subtypeToClarify);
            } catch (ObservationConflictException e) {
                throw new IllegalStateException("Observations " + observationIds + " have conflicts");
            }
        }
    }

    private Set<Long> getObservationIds(Map<String, Long> dimensions) {
        Set<Long> observationIds = observationDao.checkExactObservation(dimensions.values());
        if (observationIds.size() == 1) {
            return observationIds;
        }

        return observationDao.getObservationsIdsByDimensions(dimensions.values());
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
}
