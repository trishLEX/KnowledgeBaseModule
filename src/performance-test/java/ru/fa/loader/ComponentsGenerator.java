package ru.fa.loader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.dao.DimensionDao;
import ru.fa.dao.ObservationDao;
import ru.fa.dao.ValueDao;
import ru.fa.generator.DimensionsGenerator;
import ru.fa.generator.ObservationGenerator;
import ru.fa.generator.ObservationValueGenerator;
import ru.fa.generator.ValueGenerator;
import ru.fa.model.Dimension;
import ru.fa.model.DimensionSubtype;
import ru.fa.model.Observation;
import ru.fa.model.ObservationValue;
import ru.fa.model.Value;

@Service
public class ComponentsGenerator {

    private static final int VERTICES = 15;
    private static final int COMPONENTS = 3;
    private static final int CHILD_SIZE = 2;

    private final DimensionDao dimensionDao;
    private final ObservationDao observationDao;
    private final ValueDao valueDao;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ComponentsGenerator(DimensionDao dimensionDao, ObservationDao observationDao, ValueDao valueDao,
                               NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.dimensionDao = dimensionDao;
        this.observationDao = observationDao;
        this.valueDao = valueDao;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Transactional
    public void loadComponents() {
        DimensionsGenerator dimensionsGenerator = new DimensionsGenerator(COMPONENTS, VERTICES, CHILD_SIZE);
        Map<Long, Dimension> dimensionMap = dimensionsGenerator.createDimensions();
        List<Dimension> roots = dimensionMap.values()
                .stream()
                .filter(d -> d.getParentId() == null)
                .collect(Collectors.toList());
        ObservationGenerator observationGenerator = new ObservationGenerator(
                COMPONENTS,
                VERTICES,
                CHILD_SIZE,
                dimensionMap,
                roots
        );
        List<Observation> observations = observationGenerator.createObservations();
        List<Value> values = new ValueGenerator(observations).createValues();
        List<ObservationValue> observationValues = new ObservationValueGenerator(observations).createObservationValues();

        dimensionDao.createDimensions(dimensionMap.values());
        AtomicInteger id = new AtomicInteger(1);
        dimensionDao.createDimensionSubtypes(roots.stream()
                .map(r ->
                        new DimensionSubtype(id.get(), r.getDimensionSubType(), id.getAndIncrement())
                ).collect(Collectors.toList()));
        observationDao.createObservations(observations);
        valueDao.createValues(values);
        valueDao.createObservationValues(observationValues);
    }

    @Transactional
    public void clearDb() {
        namedParameterJdbcTemplate.update("truncate performance.observation_dimension_v2", new MapSqlParameterSource());
        namedParameterJdbcTemplate.update("truncate performance.observation_value", new MapSqlParameterSource());
        namedParameterJdbcTemplate.update("truncate performance.dimension", new MapSqlParameterSource());
        namedParameterJdbcTemplate.update("truncate performance.dimension_subtype", new MapSqlParameterSource());
        namedParameterJdbcTemplate.update("truncate performance.observation", new MapSqlParameterSource());
        namedParameterJdbcTemplate.update("truncate performance.value", new MapSqlParameterSource());
    }
}
