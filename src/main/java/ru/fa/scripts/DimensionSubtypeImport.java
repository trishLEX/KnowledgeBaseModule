package ru.fa.scripts;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.fa.scripts.model.DimensionSubType;

public class DimensionSubtypeImport {

    public static void main(String[] args) {
        NamedParameterJdbcTemplate namedJdbcTemplate = CommonImport.createNamedJdbcTemplate();

        for (DimensionSubType subType : DimensionSubType.values()) {
            namedJdbcTemplate.update("" +
                    "insert into dimension_subtype (subtype, num)\n" +
                    "values (:subtype, :num)",
                    new MapSqlParameterSource()
                        .addValue("subtype", subType.name())
                        .addValue("num", subType.ordinal())
            );
        }
    }
}
