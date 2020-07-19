package ru.fa.scripts;

import org.springframework.jdbc.support.SqlValue;

import java.sql.Array;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ArraySql<T> implements SqlValue {
    private final T[] array;
    private final JDBCType type;

    private ArraySql(T[] array, JDBCType type) {
        this.array = array;
        this.type = type;
    }

    public static <T> ArraySql<T> create(T[] array, JDBCType type) {
        return new ArraySql<>(array, type);
    }

    public static <T> ArraySql<T> create(List<T> list, JDBCType type) {
        return new ArraySql<>(list.toArray((T[]) new Object[list.size()]), type);
    }

    @Override
    public void setValue(PreparedStatement ps, int paramIndex) throws SQLException {
        Array array = ps.getConnection().createArrayOf(type.getName(), this.array);
        ps.setArray(paramIndex, array);
    }

    @Override
    public void cleanup() {

    }
}
