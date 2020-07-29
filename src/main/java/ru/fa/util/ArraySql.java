package ru.fa.util;

import org.springframework.jdbc.support.SqlValue;

import java.sql.Array;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

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

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> ArraySql<T> create(@Nullable List<T> list, JDBCType type) {
        if (list == null) {
            return null;
        }
        return new ArraySql<>(list.toArray((T[]) new Object[list.size()]), type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(@Nullable Array array) throws SQLException {
        if (array == null) {
            return null;
        }

        return Arrays.asList((T[]) array.getArray());
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
