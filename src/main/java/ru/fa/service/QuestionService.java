package ru.fa.service;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;
import ru.fa.dao.ObservationDao;
import ru.fa.dto.QuestionResponse;
import ru.fa.model.Dimension;
import ru.fa.model.DimensionSubType;
import ru.fa.model.ValueSubType;
import ru.fa.util.CustomCollectors;

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
        //todo здесь косяк, observation'ов меньше не становится
        Set<Long> observationIds = observationDao.getObservationsIdsByDimensions(dimensions.values());

        if (observationIds.isEmpty()) {
            throw new IllegalStateException("Can't find observation for dimensions " + dimensions);
        } else if (observationIds.size() == 1) {
            //todo create answer
            return new QuestionResponse.Answer();
        } else {
            Multimap<DimensionSubType, Long> subTypesToClarify =
                    observationDao.getDimensionSubTypesToClarify(observationIds);
            Map<Long, Dimension> dimensionMap = dimensionDao.getDimensionsById(
                    Sets.union(
                            Sets.newHashSet(subTypesToClarify.values()),
                            Sets.newHashSet(dimensions.values())
                    )
            );
            Map<DimensionSubType, Dimension> inputDimensions = dimensions.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> dimensionMap.get(entry.getValue()))
                    );

            Multimap<DimensionSubType, Dimension> dimensionsToClarify = subTypesToClarify
                    .entries()
                    .stream()
                    .collect(CustomCollectors.toLinkedHashMultimap(
                            Map.Entry::getKey,
                            entry -> dimensionMap.get(entry.getValue())
                    ));
            dimensionsToClarify = filterDimensions(dimensionsToClarify, inputDimensions);
            if (dimensionsToClarify.isEmpty()) {
                throw new IllegalStateException("WTF");
            }

            DimensionSubType subtypeToClarify = dimensionsToClarify.entries()
                    .stream()
                    .findFirst()
                    .orElseThrow()
                    .getKey();
            String question = dimensionMap.get(dimensions.get(subtypeToClarify)).getQuestion();
            return new QuestionResponse.Question(question, subtypeToClarify);
        }
    }

    private Multimap<DimensionSubType, Dimension> filterDimensions(
            Multimap<DimensionSubType, Dimension> dimensionsToClarify,
            Map<DimensionSubType, Dimension> inputDimensions
    ) {
        Multimap<DimensionSubType, Dimension> result = LinkedHashMultimap.create();
        for (DimensionSubType dimensionSubType : dimensionsToClarify.keySet()) {
            if (!isOnOneBranch(dimensionsToClarify.get(dimensionSubType), inputDimensions.get(dimensionSubType))) {
                result.putAll(dimensionSubType, dimensionsToClarify.get(dimensionSubType));
            }
        }

        return result;
    }

    private boolean isOnOneBranch(Collection<Dimension> dimensions, Dimension inputDimension) {
        List<Dimension> sortedByLevel = dimensions.stream()
                .sorted(Comparator.comparingLong(Dimension::getLevel))
                .collect(Collectors.toList());

        sortedByLevel.removeIf(d -> d.getLevel() <= inputDimension.getLevel() || !isOnOneBranch(d, inputDimension));
        for (int i = 0; i < sortedByLevel.size() - 1; i++) {
            Dimension current = sortedByLevel.get(i);
            List<Dimension> other = sortedByLevel.subList(i + 1, sortedByLevel.size());
            if (!dimensionContainsOther(current, other)) {
                return false;
            }
        }
        return true;
    }

    private boolean dimensionContainsOther(Dimension dimension, List<Dimension> dimensions) {
        List<Long> ids = dimensions.stream()
                .map(Dimension::getId)
                .collect(Collectors.toList());
        return dimension.getAllChildrenIds().containsAll(ids);
    }

    private boolean isOnOneBranch(Dimension one, Dimension another) {
        return one.getAllChildrenIds().contains(another.getId()) || another.getAllChildrenIds().contains(one.getId());
    }
}
