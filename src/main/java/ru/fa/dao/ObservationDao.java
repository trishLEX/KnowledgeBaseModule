package ru.fa.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.fa.model.Dimension;
import ru.fa.model.Observation;
import ru.fa.model.Value;
import ru.fa.service.ObservationDimensionsToRemove;
import ru.fa.util.ArraySql;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class ObservationDao {

    private static final String GET_OBSERVATION_IDS_BY_DIMENSIONS = "" +
            "select observation_id, array_agg(dimension_subtype)\n" +
            "from (\n" +
            "         select observation_id,\n" +
            "                dimension_subtype,\n" +
            "                array_agg(obs_dimension_id order by obs_dimension_id) dim_ids\n" +
            "         from observation_dimension_v2\n" +
            "         group by observation_id, dimension_subtype\n" +
            "     ) as obs\n" +
            "join (\n" +
            "    select subtype, array_agg(id order by id) dim_ids\n" +
            "    from (\n" +
            "             select id, subtype\n" +
            "             from dimension\n" +
            "             where id in (:ids)\n" +
            "             union\n" +
            "             select unnest(all_narrower) child_id, subtype\n" +
            "             from dimension\n" +
            "             where id in (:ids)\n" +
            "         ) unioned\n" +
            "    group by subtype\n" +
            ") as ids\n" +
            "on obs.dimension_subtype = ids.subtype and obs.dim_ids && ids.dim_ids\n" +
            "group by observation_id\n" +
            "having cardinality(array_agg(dimension_subtype)) = (select count(1) from dimension_subtype);";

    private static final String GET_DIMENSION_SUBTYPES_TO_CLARIFY = "" +
            "select dimension_subtype, dimensions\n" +
            "from (\n" +
            "         select dimension_subtype, array_agg(dimension_id) dimensions\n" +
            "         from (\n" +
            "                  select distinct dimension_subtype, dimension_id\n" +
            "                  from observation_dimension_v2\n" +
            "                  where observation_id in (:observationIds)\n" +
            "              ) od_distinct\n" +
            "         group by dimension_subtype\n" +
            "         having cardinality(array_agg(dimension_id)) > 1\n" +
            ") od\n" +
            "join dimension_subtype ds on ds.subtype = od.dimension_subtype\n" +
            "order by num asc";

    private static final String CHECK_EXACT_OBSERVATION = "" +
            "select observation_id\n" +
            "from observation_dimension_v2\n" +
            "group by observation_id\n" +
            "having array_agg(dimension_id) @> :ids\n" +
            "   and array_agg(dimension_id) <@ :ids";

    private static final String GET_OBSERVATION_VALUE = "" +
            "select id, str_id, content, type\n" +
            "from value v\n" +
            "join observation_value ov on v.id = ov.value_id\n" +
            "where observation_id = :observationId and value_subtype = :valueSubtype";

    private static final String GET_OBSERVATION_BY_IDS = "" +
            "select id, str_id, dimension_id\n" +
            "from observation o\n" +
            "join observation_dimension od on o.id = od.observation_id\n" +
            "where o.id in (:ids)";

    private static final String GET_ALL_OBSERVATIONS = "" +
            "select id, str_id, dimension_id\n" +
            "from observation o\n" +
            "join observation_dimension od on o.id = od.observation_id";

    private static final String DELETE_OBSERVATION_DIMENSIONS_V2 = "" +
            "delete from observation_dimension_v2\n" +
            "where observation_id = :id and obs_dimension_id in (:dimIds)";

    private static final String DELETE_OBSERVATION_DIMENSIONS_V2_BY_OBSERVATION_ID = "" +
            "delete from observation_dimension_v2\n" +
            "where observation_id = :id";

    private static final String INSERT_OBSERVATION = "insert into observation (id, str_id) values (:id, :strId)";

    private static final String UPDATE_OBSERVATION = "update observation set str_id = :strId where id = :id";

    private static final String INSERT_OBSERVATION_DIMENSIONS = "" +
            "insert into observation_dimension (dimension_id, observation_id)\n" +
            "values (:dimensionId, :observationId)";

    private static final String DELETE_OBSERVATION_DIMENSIONS = "" +
            "delete from observation_dimension where observation_id = :observationId";

    private static final String DELETE_OBSERVATION = "delete from observation where id = :id";

    private static final String INSERT_OBSERVATION_DIMENSIONS_V2 = "" +
            "insert into observation_dimension_v2\n" +
            "(dimension_id, obs_dimension_id, observation_id, dimension_subtype)\n" +
            "values (:dimensionId, :obsDimensionId, :observationId, :dimensionSubtype)";

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ObjectMapper objectMapper;
    private final DimensionDao dimensionDao;

    @Autowired
    public ObservationDao(
            NamedParameterJdbcTemplate namedJdbcTemplate,
            ObjectMapper objectMapper,
            DimensionDao dimensionDao
    ) {
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.objectMapper = objectMapper;
        this.dimensionDao = dimensionDao;
    }

    public Set<Long> getObservationsIdsByDimensions(Collection<Long> dimensions) {
        Set<Long> result = new HashSet<>();
        namedJdbcTemplate.query(
                GET_OBSERVATION_IDS_BY_DIMENSIONS,
                new MapSqlParameterSource("ids", dimensions),
                rs -> {result.add(rs.getLong("observation_id"));}
        );
        return result;
    }

    public Multimap<String, Long> getDimensionSubTypesToClarify(Collection<Long> observationIds) {
        Multimap<String , Long> result = LinkedHashMultimap.create();
        namedJdbcTemplate.query(
                GET_DIMENSION_SUBTYPES_TO_CLARIFY,
                new MapSqlParameterSource("observationIds", observationIds),
                rs -> {
                    result.putAll(
                            rs.getString("dimension_subtype"),
                            Arrays.asList((Long[]) rs.getArray("dimensions").getArray())
                    );
                }
        );
        return result;
    }

    public Set<Long> checkExactObservation(Collection<Long> dimensionIds) {
        return new HashSet<>(
                namedJdbcTemplate.queryForList(
                        CHECK_EXACT_OBSERVATION,
                        new MapSqlParameterSource("ids", ArraySql.create(dimensionIds, JDBCType.BIGINT)),
                        Long.class
                )
        );
    }

    public Value getObservationValue(long observationId, String valueSubType) {
        return namedJdbcTemplate.query(
                GET_OBSERVATION_VALUE,
                new MapSqlParameterSource()
                    .addValue("observationId", observationId)
                    .addValue("valueSubtype", valueSubType),
                this::mapValue
        ).stream()
                .findFirst()
                .orElseThrow();
    }

    public Map<Long, Observation> getObservations(Collection<Long> ids) {
        Map<Long, String> strIds = new HashMap<>();
        Multimap<Long, Long> dimIds = HashMultimap.create();
        namedJdbcTemplate.query(
                GET_OBSERVATION_BY_IDS,
                new MapSqlParameterSource("ids", ids),
                rs -> {
                    strIds.put(rs.getLong("id"), rs.getString("str_id"));
                    dimIds.put(rs.getLong("id"), rs.getLong("dimension_id"));
                }
        );

        return getObservationMap(strIds, dimIds);
    }

    public Collection<Observation> getObservations() {
        Map<Long, String> strIds = new HashMap<>();
        Multimap<Long, Long> dimIds = HashMultimap.create();
        namedJdbcTemplate.query(
                GET_ALL_OBSERVATIONS,
                new MapSqlParameterSource(),
                rs -> {
                    strIds.put(rs.getLong("id"), rs.getString("str_id"));
                    dimIds.put(rs.getLong("id"), rs.getLong("dimension_id"));
                }
        );

        return getObservationMap(strIds, dimIds).values();
    }

    public Observation getObservation(long id) {
        return getObservations(Collections.singletonList(id)).get(id);
    }

    public void deleteObservationDimensions(List<ObservationDimensionsToRemove> toRemoveList) {
        namedJdbcTemplate.batchUpdate(
                DELETE_OBSERVATION_DIMENSIONS_V2,
                toRemoveList.stream().map(toRemove -> new MapSqlParameterSource()
                        .addValue("id", toRemove.getObservation().getId())
                        .addValue("dimIds", toRemove.getDimensionIds())
                ).collect(Collectors.toList())
                .toArray(SqlParameterSource[]::new)
        );
    }

    @Transactional
    public void insertObservation(Observation observation) {
        namedJdbcTemplate.update(
                INSERT_OBSERVATION,
                new MapSqlParameterSource()
                    .addValue("id", observation.getId())
                    .addValue("strId", observation.getStrId())
        );

        namedJdbcTemplate.batchUpdate(
                INSERT_OBSERVATION_DIMENSIONS,
                observation.getDimensionMap()
                        .values()
                        .stream()
                        .map(d -> new MapSqlParameterSource()
                                .addValue("dimensionId", d.getId())
                                .addValue("observationId", observation.getId())
                        ).collect(Collectors.toList())
                        .toArray(SqlParameterSource[]::new)
        );

        List<MapSqlParameterSource> params = new ArrayList<>();
        for (Dimension dimension : observation.getDimensionMap().values()) {
            List<MapSqlParameterSource> param = dimension.getAllChildrenIds()
                    .stream()
                    .map(obsDimId -> new MapSqlParameterSource()
                            .addValue("dimensionId", dimension.getId())
                            .addValue("obsDimensionId", obsDimId)
                            .addValue("observationId", observation.getId())
                            .addValue("dimensionSubtype", dimension.getDimensionSubType())
                    ).collect(Collectors.toList());
            params.addAll(param);
        }

        namedJdbcTemplate.batchUpdate(
                INSERT_OBSERVATION_DIMENSIONS_V2,
                params.toArray(SqlParameterSource[]::new)
        );
    }

    @Transactional
    public void updateObservation(Observation observation) {
        namedJdbcTemplate.update(
                UPDATE_OBSERVATION,
                new MapSqlParameterSource()
                        .addValue("id", observation.getId())
                        .addValue("strId", observation.getStrId())
        );

        namedJdbcTemplate.update(
                DELETE_OBSERVATION_DIMENSIONS,
                new MapSqlParameterSource("observationId", observation.getId())
        );
        namedJdbcTemplate.batchUpdate(
                INSERT_OBSERVATION_DIMENSIONS,
                observation.getDimensionMap()
                        .values()
                        .stream()
                        .map(d -> new MapSqlParameterSource()
                                .addValue("dimensionId", d.getId())
                                .addValue("observationId", observation.getId())
                        ).collect(Collectors.toList())
                        .toArray(SqlParameterSource[]::new)
        );

        List<MapSqlParameterSource> params = new ArrayList<>();
        for (Dimension dimension : observation.getDimensionMap().values()) {
            List<MapSqlParameterSource> param = dimension.getAllChildrenIds()
                    .stream()
                    .map(obsDimId -> new MapSqlParameterSource()
                            .addValue("dimensionId", dimension.getId())
                            .addValue("obsDimensionId", obsDimId)
                            .addValue("observationId", observation.getId())
                            .addValue("dimensionSubtype", dimension.getDimensionSubType())
                    ).collect(Collectors.toList());
            params.addAll(param);
        }

        namedJdbcTemplate.update(
                DELETE_OBSERVATION_DIMENSIONS_V2_BY_OBSERVATION_ID,
                new MapSqlParameterSource("observationId", observation.getId())
        );
        namedJdbcTemplate.batchUpdate(
                INSERT_OBSERVATION_DIMENSIONS_V2,
                params.toArray(SqlParameterSource[]::new)
        );
    }

    @Transactional
    public void deleteObservation(long id) {
        namedJdbcTemplate.update(
                DELETE_OBSERVATION_DIMENSIONS,
                new MapSqlParameterSource("observationId", id)
        );
        namedJdbcTemplate.update(
                DELETE_OBSERVATION_DIMENSIONS_V2_BY_OBSERVATION_ID,
                new MapSqlParameterSource("observationId", id)
        );
        namedJdbcTemplate.update(
                DELETE_OBSERVATION,
                new MapSqlParameterSource("id", id)
        );
    }

    private Value mapValue(ResultSet rs, int rn) throws SQLException {
        return new Value(
                rs.getLong("id"),
                rs.getString("str_id"),
                readTreeUnchecked(rs.getString("content")),
                rs.getString("type")
        );
    }

    private JsonNode readTreeUnchecked(String tree) {
        try {
            return objectMapper.readTree(tree);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<Long, Observation> getObservationMap(Map<Long, String> strIds, Multimap<Long, Long> dimIds) {
        Map<Long, Dimension> dimensions = dimensionDao.getDimensions(dimIds.values());
        Map<Long, Observation> observations = new HashMap<>();
        for (Long obsId : strIds.keySet()) {
            Map<String, Dimension> dimensionMap = dimIds.get(obsId)
                    .stream()
                    .map(dimensions::get)
                    .collect(Collectors.toMap(Dimension::getDimensionSubType, Function.identity()));

            Observation observation = new Observation(obsId, strIds.get(obsId), dimensionMap);
            observations.put(obsId, observation);
        }
        return observations;
    }
}
