package ru.fa.service;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;
import ru.fa.dao.ObservationDao;
import ru.fa.dto.QuestionResponse;
import ru.fa.model.Dimension;
import ru.fa.model.DimensionSubType;
import ru.fa.model.Value;
import ru.fa.model.ValueSubType;
import ru.fa.util.CustomCollectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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

    public QuestionResponse processNotEmptyQuestion(ValueSubType valueSubType, Map<DimensionSubType, Long> dimensions) {
        Set<Long> observationIds = getObservationIds(dimensions);

        if (observationIds.isEmpty()) {
            throw new IllegalStateException("Can't find observation for dimensions " + dimensions);
        } else if (observationIds.size() == 1) {
            Value value = observationDao.getObservationValue(Iterables.getOnlyElement(observationIds), valueSubType);
            return new QuestionResponse.Answer(
                    value.getStrId(),
                    value.getContent().asText(),
                    valueSubType
            );
        } else {
            Multimap<DimensionSubType, Long> subTypesToClarifyRaw =
                    observationDao.getDimensionSubTypesToClarify(observationIds);
            Map<Long, Dimension> dimensionMap = dimensionDao.getDimensionsById(
                    Sets.union(
                            Sets.newHashSet(subTypesToClarifyRaw.values()),
                            Sets.newHashSet(dimensions.values())
                    )
            );

            Multimap<DimensionSubType, Dimension> subtypesToClarify = subTypesToClarifyRaw
                    .entries()
                    .stream()
                    .collect(CustomCollectors.toLinkedHashMultimap(
                            Map.Entry::getKey,
                            entry -> dimensionMap.get(entry.getValue())
                    ));
            Map<DimensionSubType, Dimension> inputDimensions = dimensions.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> dimensionMap.get(entry.getValue()))
                    );

            try {
                DimensionSubType subtypeToClarify = getDimensionSubtypeToClarify(subtypesToClarify, inputDimensions);
                String question = dimensionMap.get(dimensions.get(subtypeToClarify)).getQuestion();
                return new QuestionResponse.Question(question, subtypeToClarify);
            } catch (ObservationConflictException e) {
                throw new IllegalStateException("Observations " + observationIds + " have conflicts");
            }
        }
    }

    private Set<Long> getObservationIds(Map<DimensionSubType, Long> dimensions) {
        Set<Long> observationIds = observationDao.checkExactObservation(dimensions.values());
        if (observationIds.size() == 1) {
            return observationIds;
        }

        return observationDao.getObservationsIdsByDimensions(dimensions.values());
    }

    private DimensionSubType getDimensionSubtypeToClarify(
            Multimap<DimensionSubType, Dimension> subtypesToClarify,
            Map<DimensionSubType, Dimension> inputDimensions
    ) throws ObservationConflictException {
        //todo проверить что keySet -- LinkedHashSet
        for (DimensionSubType subType : subtypesToClarify.keySet()) {
            List<Dimension> upper = new ArrayList<>();
            List<Dimension> equals = new ArrayList<>();
            List<Dimension> lower = new ArrayList<>();
            Dimension input = inputDimensions.get(subType);

            if (input.getAllChildrenIds().isEmpty()) {
                continue;
            }

            for (Dimension dimension : subtypesToClarify.get(subType)) {
                switch (dimension.compareTo(input)) {
                    case -1:
                        lower.add(dimension);
                        break;
                    case 0:
                        equals.add(dimension);
                        break;
                    case 1:
                        upper.add(dimension);
                        break;
                    default:
                        throw new IllegalStateException();
                }
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
