package ru.fa.scripts;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import ru.fa.util.ArraySql;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.fa.scripts.CommonImport.createNamedJdbcTemplate;

public class NarrowerImport {

    public static void main(String[] args) {
        NamedParameterJdbcTemplate namedJdbcTemplate = createNamedJdbcTemplate();

        Map<Long, Dimension> idMap = namedJdbcTemplate.query(
                "select * from dimension",
                Collections.emptyMap(),
                CommonImport::mapDimension
        ).stream().collect(Collectors.toMap(Dimension::getId, Function.identity()));
        ArrayListMultimap<Long, Long> dimChildren = getMap(idMap);

        for (Long id : idMap.keySet()) {
            idMap.get(id).setChildrenIds(dimChildren.get(id));
        }

        List<Dimension> roots = idMap.values().stream().filter(d -> d.getParentId() == 0L).collect(Collectors.toList());
        for (Dimension root : roots) {
            List<Long> allChilds = traverse(root, idMap);
            root.setAllChildrenIds(allChilds);
        }

        SqlParameterSource[] params = idMap.values()
                .stream()
                .map(d -> d.getAllChildrenIds() == null ? d.setAllChildrenIds(Collections.emptyList()) : d)
                .map(d -> d.getChildrenIds() == null ? d.setChildrenIds(Collections.emptyList()) : d)
                .map(
                        entry -> new MapSqlParameterSource()
                                .addValue("all_children", ArraySql.create(entry.getAllChildrenIds(), JDBCType.BIGINT))
                                .addValue("children", ArraySql.create(entry.getChildrenIds(), JDBCType.BIGINT))
                                .addValue("id", entry.getId())
                ).toArray(MapSqlParameterSource[]::new);


        namedJdbcTemplate.batchUpdate(
                "update dimension set\n" +
                        "narrower = :children,\n" +
                        "all_narrower = :all_children\n" +
                        "where id = :id",
                params
        );
    }

    public static List<Long> traverse(Dimension dimension, Map<Long, Dimension> idMap) {
        if (dimension.getChildrenIds().isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Long> res = new ArrayList<>(dimension.getChildrenIds());
            for (Long child : dimension.getChildrenIds()) {
                res.addAll(traverse(idMap.get(child), idMap));
            }
            dimension.setAllChildrenIds(res);
            dimension.getAllChildrenIds().sort(Long::compareTo);
            return res;
        }
    }

    private static ArrayListMultimap<Long, Long> getMap(Map<Long, Dimension> idMap) {
        Multimap<Long, Long> dimChildren = HashMultimap.create();
        for (Long id : idMap.keySet()) {
            dimChildren.putAll(putInMap(idMap.get(id), idMap));
        }

        ArrayListMultimap<Long, Long> result = ArrayListMultimap.create(dimChildren);
        for (Long id : idMap.keySet()) {
            if (result.containsKey(id)) {
                result.get(id).sort(Long::compareTo);
            }
        }
        return result;
    }

    private static Multimap<Long, Long> putInMap(Dimension dimension, Map<Long, Dimension> idMap) {
        Multimap<Long, Long> dimChildren = HashMultimap.create();
        if (dimension.getParentId() == null || dimension.getParentId() == 0L) {
            return dimChildren;
        }
        dimChildren.put(dimension.getParentId(), dimension.getId());
        dimChildren.putAll(putInMap(idMap.get(dimension.getParentId()), idMap));
        return dimChildren;
    }
}
