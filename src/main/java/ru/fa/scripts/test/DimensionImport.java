package ru.fa.scripts.test;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import ru.fa.model.Dimension;
import ru.fa.scripts.CommonImport;
import ru.fa.util.ArraySql;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DimensionImport {

    private static final String CREATE_DIMENSION = "" +
            "insert into test.dimension\n" +
            "(id, str_id, label, broader, type, subtype, question, level, all_narrower, narrower)\n" +
            "values\n" +
            "(:id, :strId, :label, :broader, :type, :subtype, :question, :level, :allNarrower, :narrower)";

    public static void main(String[] args) {
        NamedParameterJdbcTemplate namedJdbcTemplate = CommonImport.createNamedJdbcTemplate();

        List<Dimension.Builder> dimensions = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            String strId;
            String dimensionType;
            String dimensionSubType;
            if (i <= 10) {
                strId = "Subtype1";
                dimensionType = "TYPE_1";
                dimensionSubType = "SUBTYPE_1";
            } else if (i <= 20) {
                strId = "Subtype2";
                dimensionType = "TYPE_2";
                dimensionSubType = "SUBTYPE_2";
            } else {
                strId = "Subtype3";
                dimensionType = "TYPE_3";
                dimensionSubType = "SUBTYPE_3";
            }

            Dimension.Builder d = Dimension.newBuilder()
                    .setId(i)
                    .setStrId("Dimension" + i + strId)
                    .setDimensionType(dimensionType)
                    .setDimensionSubType(dimensionSubType)
                    .setLabel("LABEL_" + i);
            dimensions.add(d);
        }

        dimensions.get(0)
                .setLevel(0)
                .setChildrenIds(List.of(1L, 2L, 3L))
                .setAllChildrenIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L))
                .setQuestion("1, 2, 3?");

        dimensions.get(1)
                .setLevel(1)
                .setParentId(0L)
                .setChildrenIds(List.of(4L, 5L))
                .setAllChildrenIds(List.of(4L, 5L))
                .setQuestion("4, 5?");

        dimensions.get(4)
                .setLevel(2)
                .setParentId(1L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(5)
                .setLevel(2)
                .setParentId(1L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(2)
                .setLevel(1)
                .setParentId(0L)
                .setChildrenIds(List.of(6L, 7L, 8L))
                .setAllChildrenIds(List.of(6L, 7L, 8L))
                .setQuestion("6, 7, 8?");

        dimensions.get(6)
                .setLevel(2)
                .setParentId(2L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(7)
                .setLevel(2)
                .setParentId(2L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(8)
                .setLevel(2)
                .setParentId(2L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(3)
                .setLevel(1)
                .setParentId(0L)
                .setChildrenIds(List.of(9L, 10L))
                .setAllChildrenIds(List.of(9L, 10L))
                .setQuestion("9, 10?");

        dimensions.get(9)
                .setLevel(2)
                .setParentId(3L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(10)
                .setLevel(2)
                .setParentId(3L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(11)
                .setLevel(0)
                .setChildrenIds(List.of(12L, 13L))
                .setAllChildrenIds(List.of(12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L))
                .setQuestion("10, 11?");

        dimensions.get(12)
                .setLevel(1)
                .setParentId(11L)
                .setChildrenIds(List.of(14L, 15L))
                .setAllChildrenIds(List.of(14L, 15L, 16L, 17L))
                .setQuestion("14, 15?");

        dimensions.get(13)
                .setLevel(1)
                .setParentId(11L)
                .setChildrenIds(List.of(18L, 19L, 20L))
                .setAllChildrenIds(List.of(18L, 19L, 20L))
                .setQuestion("18, 19, 20?");

        dimensions.get(14)
                .setLevel(2)
                .setParentId(12L)
                .setChildrenIds(List.of(16L, 17L))
                .setAllChildrenIds(List.of(16L, 17L))
                .setQuestion("16, 17?");

        dimensions.get(15)
                .setLevel(2)
                .setParentId(12L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(16)
                .setLevel(3)
                .setParentId(14L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(17)
                .setLevel(3)
                .setParentId(14L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(18)
                .setLevel(2)
                .setParentId(13L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(19)
                .setLevel(2)
                .setParentId(13L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(20)
                .setLevel(2)
                .setParentId(13L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(21)
                .setLevel(0)
                .setChildrenIds(List.of(22L, 23L, 24L))
                .setAllChildrenIds(List.of(22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L))
                .setQuestion("22, 23, 24?");

        dimensions.get(22)
                .setLevel(1)
                .setParentId(21L)
                .setChildrenIds(List.of(25L, 26L, 27L))
                .setAllChildrenIds(List.of(25L, 26L, 27L))
                .setQuestion("25, 26, 27?");

        dimensions.get(23)
                .setLevel(1)
                .setParentId(21L)
                .setChildrenIds(List.of(28L, 29L))
                .setAllChildrenIds(List.of(28L, 29L))
                .setQuestion("28, 29?");

        dimensions.get(24)
                .setLevel(1)
                .setParentId(21L)
                .setChildrenIds(List.of(30L, 31L))
                .setAllChildrenIds(List.of(30L, 31L))
                .setQuestion("30, 31?");

        dimensions.get(25)
                .setLevel(2)
                .setParentId(22L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(26)
                .setLevel(2)
                .setParentId(22L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(27)
                .setLevel(2)
                .setParentId(22L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(28)
                .setLevel(2)
                .setParentId(23L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(29)
                .setLevel(2)
                .setParentId(23L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(30)
                .setLevel(2)
                .setParentId(24L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        dimensions.get(31)
                .setLevel(2)
                .setParentId(24L)
                .setChildrenIds(List.of())
                .setAllChildrenIds(List.of())
                .setQuestion("");

        SqlParameterSource[] params = dimensions.stream().map(Dimension.Builder::build).map(dimension ->
                new MapSqlParameterSource()
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
        ).collect(Collectors.toList()).toArray(SqlParameterSource[]::new);

        namedJdbcTemplate.batchUpdate(
                CREATE_DIMENSION,
                params
        );
    }
}
