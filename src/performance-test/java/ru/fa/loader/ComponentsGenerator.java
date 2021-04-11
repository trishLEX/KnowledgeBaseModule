package ru.fa.loader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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
@Profile("performance")
public class ComponentsGenerator {

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
    public void loadComponents(int vertices, int components, int childSize) {
        DimensionsGenerator dimensionsGenerator = new DimensionsGenerator(components, vertices, childSize);
        Map<Long, Dimension> dimensionMap = dimensionsGenerator.createDimensions();
        List<Dimension> roots = dimensionMap.values()
                .stream()
                .filter(d -> d.getParentId() == null)
                .collect(Collectors.toList());
        ObservationGenerator observationGenerator = new ObservationGenerator(
                components,
                vertices,
                childSize,
                dimensionMap,
                roots
        );
        List<Observation> observations = observationGenerator.createObservations();
        List<Value> values = new ValueGenerator(observations).createValues();
        List<ObservationValue> observationValues = new ObservationValueGenerator(observations).createObservationValues();

        dimensionDao.createDimensionsWithIds(dimensionMap.values());
        dimensionMap.clear();
        AtomicInteger id = new AtomicInteger(1);
        dimensionDao.createDimensionSubtypes(roots.stream()
                .map(r ->
                        new DimensionSubtype(id.get(), r.getDimensionSubType(), id.getAndIncrement())
                ).collect(Collectors.toList()));
        roots.clear();
        observationDao.createObservations(observations);
        observations.clear();
        valueDao.createValues(values);
        values.clear();
        valueDao.createObservationValues(observationValues);
        observationValues.clear();
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