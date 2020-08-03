package ru.fa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;
import ru.fa.dto.QuestionRequest;
import ru.fa.model.DimensionSubType;

import java.util.Map;

@Service
public class DimensionService {

    private final DimensionDao dimensionDao;

    @Autowired
    public DimensionService(DimensionDao dimensionDao) {
        this.dimensionDao = dimensionDao;
    }

    public Map<DimensionSubType, Long> getRequestDimensions(QuestionRequest questionRequest) {
        Map<DimensionSubType, Long> topConcepts = dimensionDao.getDimensionsTopConcepts();
        Map<DimensionSubType, Long> dimensions = dimensionDao.getDimensionsValuesByStrIds(
                questionRequest.getDimensions().values()
        );
        topConcepts.putAll(dimensions);
        return topConcepts;
    }

    public Map<DimensionSubType, Long> getRequestDimensions() {
        return dimensionDao.getDimensionsTopConcepts();
    }
}
