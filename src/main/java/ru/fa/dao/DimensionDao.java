package ru.fa.dao;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.fa.model.Dimension;
import ru.fa.model.DimensionSubtype;
import ru.fa.util.ArraySql;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
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

    private static final String GET_DIMENSIONS = "" +
            "select id, str_id, label, broader, type, subtype, question, level, all_narrower, narrower\n" +
            "from dimension";

    private static final String GET_QUESTION_BY_ID = "select question from dimension where id = :id";

    private static final String GET_DIMENSIONS_TOP_CONCEPTS = "select subtype, id from dimension where broader is null";

    private static final String GET_FACT_DIMENSIONS = "" +
            "select id from dimension\n" +
            "where id in (\n" +
            "    select id\n" +
            "    from dimension\n" +
            "    where id in (:ids)\n" +
            "        union\n" +
            "        select unnest(all_narrower) id\n" +
            "        from dimension\n" +
            "        where id in (:ids)\n" +
            ") and cardinality(all_narrower) = 0";

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

    private static final String CREATE_DIMENSION_WITH_ID = "" +
            "insert into dimension\n" +
            "(id, str_id, label, broader, type, subtype, question, level, all_narrower, narrower)\n" +
            "values\n" +
            "(:id, :strId, :label, :broader, :type, :subtype, :question, :level, :allNarrower, :narrower)";

    private static final String DELETE_DIMENSION = "delete from dimension where id = :id";

    private static final String CREATE_SUBTYPE = "" +
            "insert into dimension_subtype (id, subtype, num)\n" +
            "values (:id, :subtype, :num)";

    private static final String COUNT_DIMENSIONS = "select count(1) from dimension";

    private static final String GET_ALL_SUBTYPES = "select subtype from dimension_subtype";

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

    public Map<Long, Dimension> getDimensions(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return namedJdbcTemplate.query(
                GET_DIMENSIONS_BY_ID,
                new MapSqlParameterSource("ids", ids),
                DimensionDao::mapDimension
        ).stream().collect(Collectors.toMap(Dimension::getId, Function.identity()));
    }

    public List<Dimension> getDimensions() {
        return namedJdbcTemplate.query(
                GET_DIMENSIONS,
                Map.of(),
                DimensionDao::mapDimension
        );
    }

    public Dimension getDimension(long id) {
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
                GET_QUESTION_BY_ID,
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

    public Set<Long> getFactDimensions(Collection<Long> ids) {
        return new HashSet<>(namedJdbcTemplate.queryForList(
                GET_FACT_DIMENSIONS,
                new MapSqlParameterSource("ids", ids),
                Long.class
        ));
    }

    public Map<String, Long> getDimensionsByStrIds(Collection<String> strIds) {
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
                        .addValue("allNarrower", ArraySql.create(dimension.getAllChildrenIds(), JDBCType.BIGINT))
                        .addValue("narrower", ArraySql.create(dimension.getChildrenIds(), JDBCType.BIGINT))
        );
    }

    public void createDimensions(Collection<Dimension> dimensions) {
        SqlParameterSource[] params = dimensions
                .stream()
                .map(dimension -> new MapSqlParameterSource()
                        .addValue("strId", dimension.getStrId())
                        .addValue("label", dimension.getLabel())
                        .addValue("broader", dimension.getParentId())
                        .addValue("type", dimension.getDimensionType())
                        .addValue("subtype", dimension.getDimensionSubType())
                        .addValue("question", dimension.getQuestion())
                        .addValue("level", dimension.getLevel())
                        .addValue("allNarrower", ArraySql.create(dimension.getAllChildrenIds(), JDBCType.BIGINT))
                        .addValue("narrower", ArraySql.create(dimension.getChildrenIds(), JDBCType.BIGINT))
                ).toArray(SqlParameterSource[]::new);
        namedJdbcTemplate.batchUpdate(CREATE_DIMENSION, params);
    }

    @VisibleForTesting
    public void createDimensionsWithIds(Collection<Dimension> dimensions) {
        SqlParameterSource[] params = dimensions
                .stream()
                .map(dimension -> new MapSqlParameterSource()
                        .addValue("id", dimension.getId())
                        .addValue("strId", dimension.getStrId())
                        .addValue("label", dimension.getLabel())
                        .addValue("broader", dimension.getParentId())
                        .addValue("type", dimension.getDimensionType())
                        .addValue("subtype", dimension.getDimensionSubType())
                        .addValue("question", dimension.getQuestion())
                        .addValue("level", dimension.getLevel())
                        .addValue("allNarrower", ArraySql.create(dimension.getAllChildrenIds(), JDBCType.BIGINT))
                        .addValue("narrower", ArraySql.create(dimension.getChildrenIds(), JDBCType.BIGINT))
                ).toArray(SqlParameterSource[]::new);
        namedJdbcTemplate.batchUpdate(CREATE_DIMENSION_WITH_ID, params);
    }


    public void createDimensionSubtypes(Collection<DimensionSubtype> dimensionSubtypes) {
        SqlParameterSource[] params = dimensionSubtypes
                .stream()
                .map(dimensionSubtype -> new MapSqlParameterSource()
                        .addValue("id", dimensionSubtype.getId())
                        .addValue("subtype", dimensionSubtype.getSubtype())
                        .addValue("num", dimensionSubtype.getNum())
                ).toArray(SqlParameterSource[]::new);
        namedJdbcTemplate.batchUpdate(CREATE_SUBTYPE, params);
    }

    public void deleteDimension(long id) {
        namedJdbcTemplate.update(
                DELETE_DIMENSION,
                new MapSqlParameterSource("id", id)
        );
    }

    public int countDimensions() {
        return namedJdbcTemplate.queryForObject(COUNT_DIMENSIONS, Map.of(), Integer.class);
    }

    public List<String> getAllSubtypes() {
        return namedJdbcTemplate.queryForList(GET_ALL_SUBTYPES, Map.of(), String.class);
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
                .setAllChildrenIds(Set.of((Long[]) rs.getArray("all_narrower").getArray()))
                .setChildrenIds(Set.of((Long[]) rs.getArray("narrower").getArray()))
                .setQuestion(rs.getString("question"))
                .build();
    }
}
