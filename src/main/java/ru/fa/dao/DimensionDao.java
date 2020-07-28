package ru.fa.dao;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.fa.model.Dimension;
import ru.fa.model.DimensionSubType;
import ru.fa.model.DimensionType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class DimensionDao {

    private static final String GET_DIMENSIONS_BY_STR_ID = "" +
            "select id, str_id, label, broader, type, subtype, question, level, all_narrower, narrower\n" +
            "from dimension where str_id in (:strIds)";

    private static final String GET_DIMENSIONS_BY_ID = "" +
            "select id, str_id, label, broader, type, subtype, question, level, all_narrower, narrower\n" +
            "from dimension where id in (:ids)";

    private static final String GET_QUESTION_BY_STR_ID = "select question from dimension where id = :id";

    private static final String GET_DIMENSIONS_TOP_CONCEPTS = "select subtype, id from dimension where broader is null";

    private static final String GET_DIMENSION_IDS_BY_STR_IDS = "" +
            "select subtype, id\n" +
            "from dimension\n" +
            "where str_id in (:strIds)";

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public DimensionDao(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    public List<Dimension> getDimensionsByStrId(Collection<String> strIds) {
        return namedJdbcTemplate.query(
                GET_DIMENSIONS_BY_STR_ID,
                new MapSqlParameterSource("strIds", strIds),
                DimensionDao::mapDimension
        );
    }

    public Map<Long, Dimension> getDimensionsById(Collection<Long> ids) {
        return namedJdbcTemplate.query(
                GET_DIMENSIONS_BY_ID,
                new MapSqlParameterSource("ids", ids),
                DimensionDao::mapDimension
        ).stream().collect(Collectors.toMap(Dimension::getId, Function.identity()));
    }

    public String getQuestionById(long id) {
        return namedJdbcTemplate.queryForObject(
                GET_QUESTION_BY_STR_ID,
                new MapSqlParameterSource("id", id),
                String.class
        );
    }

    public Map<DimensionSubType, Long> getDimensionsTopConcepts() {
        Map<DimensionSubType, Long> topConcepts = new HashMap<>();
        namedJdbcTemplate.query(
                GET_DIMENSIONS_TOP_CONCEPTS,
                Collections.emptyMap(),
                rs -> {
                    topConcepts.put(
                            DimensionSubType.valueOf(rs.getString("subtype")),
                            rs.getLong("id")
                    );
                }
        );
        return topConcepts;
    }

    public Map<DimensionSubType, Long> getDimensionsValuesByStrIds(Collection<String> strIds) {
        Map<DimensionSubType, Long> dimensions = new HashMap<>();
        namedJdbcTemplate.query(
                GET_DIMENSION_IDS_BY_STR_IDS,
                new MapSqlParameterSource("strIds", strIds),
                rs -> {
                    dimensions.put(
                            DimensionSubType.valueOf(rs.getString("subtype")),
                            rs.getLong("id")
                    );
                }
        );
        return dimensions;
    }

    private static Dimension mapDimension(ResultSet rs, int rn) throws SQLException {
        return Dimension.newBuilder()
                .setId(rs.getLong("id"))
                .setLevel(rs.getInt("level"))
                .setStrId(rs.getString("str_id"))
                .setLabel(rs.getString("label"))
                .setDimensionType(DimensionType.valueOf(rs.getString("type")))
                .setDimensionSubType(DimensionSubType.valueOf(rs.getString("subtype")))
                .setParentId(rs.getLong("broader"))
                .setAllChildrenIds(Arrays.asList((Long[]) rs.getArray("all_narrower").getArray()))
                .setChildrenIds(Arrays.asList((Long[]) rs.getArray("narrower").getArray()))
                .setQuestion(rs.getString("question"))
                .build();
    }
}
