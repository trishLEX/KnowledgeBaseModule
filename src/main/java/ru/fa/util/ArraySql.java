package ru.fa.util;

import org.springframework.jdbc.support.SqlValue;

import javax.annotation.Nullable;
import java.sql.Array;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
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

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> ArraySql<T> create(@Nullable Collection<T> collection, JDBCType type) {
        if (collection == null) {
            return null;
        }
        return new ArraySql<>(collection.toArray((T[]) new Object[collection.size()]), type);
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
