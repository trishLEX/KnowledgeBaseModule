package ru.fa.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.support.SqlValue;

public class JsonSql implements SqlValue {

    private final String json;

    private JsonSql(String json) {
        this.json = json;
    }

    public static JsonSql create(String json) {
        return new JsonSql(json);
    }

    public static JsonSql create(JsonNode json) {
        return new JsonSql(json.toString());
    }

    @Override
    public void setValue(PreparedStatement ps, int paramIndex) throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("json");
        pgObject.setValue(json);
        ps.setObject(paramIndex, pgObject);
    }

    @Override
    public void cleanup() {

    }
}
