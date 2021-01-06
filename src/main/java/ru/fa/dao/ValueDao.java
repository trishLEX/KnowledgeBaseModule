package ru.fa.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.fa.model.ObservationValue;
import ru.fa.model.Value;
import ru.fa.util.JsonSql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ValueDao {

    private static final String INSERT_VALUE = "" +
            "insert into value (id, str_id, content, type)\n" +
            "values (:id, :strId, :content, :type)";

    private static final String UPDATE_VALUE = "" +
            "update value\n" +
            "set\n" +
            "   str_id = :strId,\n" +
            "   content = :content,\n" +
            "   type = :type\n" +
            "where id = :id";

    private static final String DELETE_VALUE = "delete from value where id = :id";

    private static final String SELECT_VALUES = "select id, str_id, content, type from value";

    private static final String SELECT_VALUE = "select id, str_id, content, type from value where id = :id";

    private static final String INSERT_OBSERVATION_VALUE = "" +
            "insert into observation_value (observation_id, value_id, value_subtype)\n" +
            "values (:observationId, :valueId, :valueSubtype)";

    private static final String DELETE_OBSERVATION_VALUE = "delete from observation_value where value_id = :id";

    private static final String SELECT_OBSERVATION_VALUES = "" +
            "select observation_id, value_id, value_subtype\n" +
            "from observation_value";

    private static final String SELECT_OBSERVATION_VALUE = "" +
            "select observation_id, value_id, value_subtype\n" +
            "from observation_value\n" +
            "where value_id = :id";

    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ValueDao(NamedParameterJdbcTemplate namedJdbcTemplate, ObjectMapper objectMapper) {
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void insertValue(Value value) {
        namedJdbcTemplate.update(
                INSERT_VALUE,
                new MapSqlParameterSource()
                        .addValue("id", value.getId())
                        .addValue("strId", value.getStrId())
                        .addValue("content", JsonSql.create(value.getContent()))
                        .addValue("type", value.getType())
        );
    }

    public void insertObservationValue(ObservationValue observationValue) {
        namedJdbcTemplate.update(
                INSERT_OBSERVATION_VALUE,
                new MapSqlParameterSource()
                        .addValue("observationId", observationValue.getObservationId())
                        .addValue("valueId", observationValue.getValueId())
                        .addValue("valueSubtype", observationValue.getValueSubtype())
        );
    }

    public void updateValue(Value value) {
        namedJdbcTemplate.update(
                UPDATE_VALUE,
                new MapSqlParameterSource()
                        .addValue("id", value.getId())
                        .addValue("strId", value.getStrId())
                        .addValue("content", JsonSql.create(value.getContent()))
                        .addValue("type", value.getType())
        );
    }

    public void updateObservationValues(long valueId, List<ObservationValue> observationValues) {
        namedJdbcTemplate.update(DELETE_OBSERVATION_VALUE, new MapSqlParameterSource("id", valueId));

        SqlParameterSource[] params = observationValues.stream()
                .map(
                        value -> new MapSqlParameterSource()
                                .addValue("observationId", value.getObservationId())
                                .addValue("valueId", value.getValueId())
                                .addValue("valueSubtype", value.getValueSubtype())
                ).collect(Collectors.toList())
                .toArray(SqlParameterSource[]::new);
        namedJdbcTemplate.batchUpdate(INSERT_OBSERVATION_VALUE, params);
    }

    public void deleteValue(long id) {
        namedJdbcTemplate.update(DELETE_VALUE, new MapSqlParameterSource("id", id));
    }

    public void deleteObservationValues(long id) {
        namedJdbcTemplate.update(DELETE_OBSERVATION_VALUE, new MapSqlParameterSource("id", id));
    }

    public Value getValue(long id) {
        return namedJdbcTemplate.queryForObject(
                SELECT_VALUE,
                new MapSqlParameterSource("id", id),
                this::mapValue
        );
    }

    public List<ObservationValue> getObservationValues(long id) {
        return namedJdbcTemplate.query(
                SELECT_OBSERVATION_VALUE,
                new MapSqlParameterSource("id", id),
                this::mapObservationValue
        );
    }

    public List<Value> getValues() {
        return namedJdbcTemplate.query(
                SELECT_VALUES,
                new MapSqlParameterSource(),
                this::mapValue
        );
    }

    public List<ObservationValue> getObservationValues() {
        return namedJdbcTemplate.query(
                SELECT_OBSERVATION_VALUES,
                new MapSqlParameterSource(),
                this::mapObservationValue
        );
    }

    private JsonNode readTreeUnchecked(String tree) {
        try {
            return objectMapper.readTree(tree);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Value mapValue(ResultSet rs, int rn) throws SQLException {
        return new Value(
                rs.getLong("id"),
                rs.getString("str_id"),
                readTreeUnchecked(rs.getString("content")),
                rs.getString("type")
        );
    }

    private ObservationValue mapObservationValue(ResultSet rs, int rn) throws SQLException {
        return new ObservationValue(
                rs.getLong("observation_id"),
                rs.getLong("value_id"),
                rs.getString("value_subtype")
        );
    }
}
