package ru.fa.dao;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.fa.model.Dimension;
import ru.fa.util.ArraySql;

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

    private static final String UPDATE_DIMENSION = "" +
            "update dimension\n" +
            "set\n" +
            "   str_id = :strId,\n" +
            "   label = :label,\n" +
            "   broader = :broader,\n" +
            "   type = :type,\n" +
            "   subtype = :subtype,\n" +
            "   question = :question,\n" +
            "   level = :level,\n" +
            "   all_narrower = :allNarrower,\n" +
            "   narrower = :narrower\n" +
            "where id = :id";

    private static final String CREATE_DIMENSION = "" +
            "insert into dimension\n" +
            "(str_id, label, broader, type, subtype, question, level, all_narrower, narrower)\n" +
            "values\n" +
            "(:strId, :label, :broader, :type, :subtype, :question, :level, :allNarrower, :narrower)";

    private static final String DELETE_DIMENSION = "delete from dimension where id = :id";

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
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

    public Dimension getDimensionById(long id) {
        return namedJdbcTemplate.query(
                GET_DIMENSIONS_BY_ID,
                new MapSqlParameterSource("ids", Collections.singletonList(id)),
                DimensionDao::mapDimension
        ).stream()
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public String getQuestionById(long id) {
        return namedJdbcTemplate.queryForObject(
                GET_QUESTION_BY_STR_ID,
                new MapSqlParameterSource("id", id),
                String.class
        );
    }

    public Map<String, Long> getDimensionsTopConcepts() {
        Map<String, Long> topConcepts = new HashMap<>();
        namedJdbcTemplate.query(
                GET_DIMENSIONS_TOP_CONCEPTS,
                Collections.emptyMap(),
                rs -> {
                    topConcepts.put(
                            rs.getString("subtype"),
                            rs.getLong("id")
                    );
                }
        );
        return topConcepts;
    }

    public Map<String, Long> getDimensionsValuesByStrIds(Collection<String> strIds) {
        Map<String, Long> dimensions = new HashMap<>();
        namedJdbcTemplate.query(
                GET_DIMENSION_IDS_BY_STR_IDS,
                new MapSqlParameterSource("strIds", strIds),
                rs -> {
                    dimensions.put(
                            rs.getString("subtype"),
                            rs.getLong("id")
                    );
                }
        );
        return dimensions;
    }

    public void updateDimension(Dimension dimension) {
        namedJdbcTemplate.update(
                UPDATE_DIMENSION,
                new MapSqlParameterSource()
                        .addValue("id", dimension.getId())
                        .addValue("strId", dimension.getStrId())
                        .addValue("label", dimension.getLabel())
                        .addValue("broader", dimension.getParentId())
                        .addValue("type", dimension.getDimensionType())
                        .addValue("subtype", dimension.getDimensionSubType())
                        .addValue("question", dimension.getQuestion())
                        .addValue("level", dimension.getLevel())
                        .addValue("all_narrower", ArraySql.create(dimension.getAllChildrenIds(), JDBCType.BIGINT))
                        .addValue("narrower", ArraySql.create(dimension.getChildrenIds(), JDBCType.BIGINT))
        );
    }

    public void createDimension(Dimension dimension) {
        namedJdbcTemplate.update(
                CREATE_DIMENSION,
                new MapSqlParameterSource()
                        .addValue("strId", dimension.getStrId())
                        .addValue("label", dimension.getLabel())
                        .addValue("broader", dimension.getParentId())
                        .addValue("type", dimension.getDimensionType())
                        .addValue("subtype", dimension.getDimensionSubType())
                        .addValue("question", dimension.getQuestion())
                        .addValue("level", dimension.getLevel())
                        .addValue("all_narrower", ArraySql.create(dimension.getAllChildrenIds(), JDBCType.BIGINT))
                        .addValue("narrower", ArraySql.create(dimension.getChildrenIds(), JDBCType.BIGINT))
        );
    }

    public void deleteDimension(long id) {
        namedJdbcTemplate.update(
                DELETE_DIMENSION,
                new MapSqlParameterSource("id", id)
        );
    }

    private static Dimension mapDimension(ResultSet rs, int rn) throws SQLException {
        return Dimension.newBuilder()
                .setId(rs.getLong("id"))
                .setLevel(rs.getInt("level"))
                .setStrId(rs.getString("str_id"))
                .setLabel(rs.getString("label"))
                .setDimensionType(rs.getString("type"))
                .setDimensionSubType(rs.getString("subtype"))
                .setParentId(rs.getLong("broader"))
                .setAllChildrenIds(Arrays.asList((Long[]) rs.getArray("all_narrower").getArray()))
                .setChildrenIds(Arrays.asList((Long[]) rs.getArray("narrower").getArray()))
                .setQuestion(rs.getString("question"))
                .build();
    }
}
