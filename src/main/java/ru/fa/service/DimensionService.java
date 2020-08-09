package ru.fa.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;
import ru.fa.dto.QuestionRequest;

@Service
public class DimensionService {

    private final DimensionDao dimensionDao;

    @Autowired
    public DimensionService(DimensionDao dimensionDao) {
        this.dimensionDao = dimensionDao;
    }

    public Map<String, Long> getRequestDimensions(QuestionRequest questionRequest) {
        Map<String, Long> topConcepts = dimensionDao.getDimensionsTopConcepts();
        Map<String, Long> dimensions = dimensionDao.getDimensionsValuesByStrIds(
                questionRequest.getDimensions().values()
        );
        topConcepts.putAll(dimensions);
        return topConcepts;
    }

    public Map<String, Long> getRequestDimensions() {
        return dimensionDao.getDimensionsTopConcepts();
    }
}
