package ru.fa.scripts.test;

import java.util.List;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.fa.scripts.CommonImport;
import ru.fa.util.JsonSql;

public class ObservationImport {

    private static final String INSERT_VALUE = "" +
            "insert into test.value (id, str_id, content, type)\n" +
            "values (:id, :strId, :content, :type)";

    private static final String INSERT_OBSERVATION_VALUE = "" +
            "insert into test.observation_value (observation_id, value_id, value_subtype)\n" +
            "values (:oId, :vId, :vSubType)";

    private static final String INSERT_OBSERVATION = "" +
            "insert into test.observation (id, str_id)\n" +
            "values (:id, :strId)";

    private static final String INSERT_OBSERVATION_DIMENSION = "" +
            "insert into test.observation_dimension (dimension_id, observation_id)\n" +
            "values (:dId, :oId)";

    public static void main(String[] args) {
        NamedParameterJdbcTemplate namedJdbcTemplate = CommonImport.createNamedJdbcTemplate();

        for (int i = 0; i < 5; i++) {
            namedJdbcTemplate.update(
                    INSERT_OBSERVATION,
                    new MapSqlParameterSource()
                        .addValue("id", i)
                        .addValue("strId", "Observation" + i)
            );

            //language=json
            String body = "" +
                    "{\n" +
                    "  \"body\": \"superBody\",\n" +
                    "  \"id\": " + i + "\n" +
                    "}";

            namedJdbcTemplate.update(
                    INSERT_VALUE,
                    new MapSqlParameterSource()
                            .addValue("id", i)
                            .addValue("strId", "Value" + i)
                            .addValue("content", JsonSql.create(body))
                            .addValue("type", "VALUE_TYPE_1")
            );

            namedJdbcTemplate.update(
                    INSERT_OBSERVATION_VALUE,
                    new MapSqlParameterSource()
                        .addValue("oId", i)
                        .addValue("vId", i)
                        .addValue("vSubType", "VALUE_SUBTYPE_1")
            );
        }

        Multimap<Long, Long> obsDims = LinkedListMultimap.create();
        obsDims.putAll(0L, List.of(4L, 16L, 25L));
        obsDims.putAll(1L, List.of(5L, 16L, 25L));
        obsDims.putAll(2L, List.of(5L, 14L, 22L));
        obsDims.putAll(3L, List.of(5L, 10L, 22L));

        for (long oId : obsDims.keySet()) {
            for (long dId : obsDims.get(oId)) {
                namedJdbcTemplate.update(
                        INSERT_OBSERVATION_DIMENSION,
                        new MapSqlParameterSource()
                                .addValue("oId", oId)
                                .addValue("dId", dId)
                );
            }
        }
    }
}
