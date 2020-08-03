package ru.fa.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.fa.model.DimensionSubType;
import ru.fa.model.Value;
import ru.fa.model.ValueSubType;
import ru.fa.model.ValueType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
            "having array_agg(dimension_id) @> (:ids)\n" +
            "   and array_agg(dimension_id) <@ (:ids)";

    private static final String GET_OBSERVATION_VALUE = "" +
            "select id, str_id, content, type\n" +
            "from value v\n" +
            "join observation_value ov on v.id = ov.value_id\n" +
            "where observation_id = :observationId and value_subtype = :valueSubtype";

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ObservationDao(NamedParameterJdbcTemplate namedJdbcTemplate, ObjectMapper objectMapper) {
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.objectMapper = objectMapper;
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

    public Multimap<DimensionSubType, Long> getDimensionSubTypesToClarify(Collection<Long> observationIds) {
        Multimap<DimensionSubType, Long> result = LinkedHashMultimap.create();
        namedJdbcTemplate.query(
                GET_DIMENSION_SUBTYPES_TO_CLARIFY,
                new MapSqlParameterSource("observationIds", observationIds),
                rs -> {
                    result.putAll(
                            DimensionSubType.valueOf(rs.getString("dimension_subtype")),
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
                        new MapSqlParameterSource("ids", dimensionIds),
                        Long.class
                )
        );
    }

    public Value getObservationValue(long observationId, ValueSubType valueSubType) {
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

    private Value mapValue(ResultSet rs, int rn) throws SQLException {
        return new Value(
                rs.getLong("id"),
                rs.getString("str_id"),
                readTreeUnchecked(rs.getString("content")),
                ValueType.valueOf(rs.getString("type"))
        );
    }

    private JsonNode readTreeUnchecked(String tree) {
        try {
            return objectMapper.readTree(tree);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
