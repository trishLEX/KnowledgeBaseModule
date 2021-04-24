package ru.fa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.fa.dao.DimensionDao;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Service
public class DimensionService {

    private final DimensionDao dimensionDao;

    @Autowired
    public DimensionService(DimensionDao dimensionDao) {
        this.dimensionDao = dimensionDao;
    }

    public Map<String, Long> getRequestDimensions(Map<String, String> input) {
        Map<String, Long> topConcepts = dimensionDao.getDimensionsTopConcepts();
        Map<String, Long> dimensions = dimensionDao.getDimensionsByStrIds(input.values());
        topConcepts.putAll(dimensions);
        return topConcepts;
    }

    public Set<Long> getFactDimensions(Collection<Long> input) {
        return dimensionDao.getFactDimensions(input);
    }
}
