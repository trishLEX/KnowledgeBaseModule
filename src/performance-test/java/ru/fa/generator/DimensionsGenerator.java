package ru.fa.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ru.fa.model.Dimension;

public class DimensionsGenerator {

    private final int components;
    private final int vertices;
    private final int childSize;

    public DimensionsGenerator(int components, int vertices, int childSize) {
        this.components = components;
        this.vertices = vertices;
        this.childSize = childSize;
    }

    public Map<Long, Dimension> createDimensions() {
        Map<Long, Dimension> dimensionMap = new HashMap<>(vertices * components);
        List<Dimension> roots = new ArrayList<>(components);
        List<long[][]> tables = new ArrayList<>(components);
        for (int i = 0; i < components; i++) {
            long[][] table = new long[vertices][vertices];

            long id = dimensionMap.values()
                    .stream()
                    .max(Comparator.comparingLong(Dimension::getId))
                    .map(Dimension::getId)
                    .map(maxId -> maxId + 1)
                    .orElse(0L);
            Dimension root = Dimension.newBuilder()
                    .setId(id)
                    .setStrId("Dimension" + id)
                    .setDimensionType("TYPE_" + i)
                    .setDimensionSubType("SUBTYPE_" + i)
                    .setLevel(0)
                    .setLabel("LABEL_" + id)
                    .build();
            roots.add(root);
            dimensionMap.put(id, root);
            LinkedList<Dimension> dimensions = new LinkedList<>();
            dimensions.push(root);
            createChildren(dimensions, table, i, dimensionMap);

            for (Dimension d: dimensionMap.values()) {
                d.addAllChildrenIds(getAllChildrenIds(d, new HashSet<>(), dimensionMap));
            }
            tables.add(table);
        }
//        System.out.println(tableToString(reshape(tables)));
        return dimensionMap;
    }

    private void createChildren(
            LinkedList<Dimension> dimensions,
            long[][] table,
            int typeId,
            Map<Long, Dimension> dimensionMap
    ) {
        Dimension dimension = dimensions.pollLast();
        if (dimension == null) {
            return;
        }

        List<Dimension> children = new ArrayList<>();
        for (int i = 0; i < childSize; i++) {
            long offset = i + 1;
            for (long adjOffset = offset; adjOffset < vertices; adjOffset++) {
                Optional<Dimension> child = Optional.ofNullable(dimensionMap.get(dimension.getId() + adjOffset));
                int childLevel = child.map(Dimension::getLevel)
                        .orElse(Integer.MAX_VALUE);
                if (childLevel > dimension.getLevel() && child.map(Dimension::getParentId).isEmpty()
                ) {
                    offset = adjOffset;
                    break;
                }
            }
            if (dimension.getId() + offset >= vertices * (typeId + 1)) {
                continue;
            }

            Dimension child = Dimension.newBuilder()
                    .setId(dimension.getId() + offset)
                    .setStrId("Dimension" + (dimension.getId() + offset))
                    .setParentId(dimension.getId())
                    .setLevel(dimension.getLevel() + 1)
                    .setLabel("LABEL_" + (dimension.getId() + offset))
                    .setDimensionType("TYPE_" + typeId)
                    .setDimensionSubType("SUBTYPE_" + typeId)
                    .build();
            dimension.addChildId(child.getId());
            children.add(child);
            table[(int) dimension.getId() % vertices][(int) child.getId() % vertices] = 1;
            dimensionMap.put(child.getId(), child);
        }

        for (var child: children) {
            dimensions.push(child);
        }
        createChildren(dimensions, table, typeId, dimensionMap);
    }

    private static Set<Long> getAllChildrenIds(
            Dimension dimension,
            Set<Long> childrenIds,
            Map<Long, Dimension> dimensionMap
    ) {
        childrenIds.addAll(dimension.getChildrenIds());
        dimension.getChildrenIds()
                .stream()
                .map(dimensionMap::get)
                .map(d -> getAllChildrenIds(d, childrenIds, dimensionMap))
                .flatMap(Collection::stream)
                .forEach(childrenIds::add);
        return childrenIds;
    }

    private String tableToString(long[][] table) {
        String lineSeparator = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        for (long[] row : table) {
            sb.append(Arrays.toString(row)).append(lineSeparator);
        }

        return sb.toString();
    }

    private long[][] reshape(List<long[][]> finalTable) {
        long[][] result = new long[components * vertices][components * vertices];
        for (int i = 0; i < finalTable.size(); i++) {
            long[][] diag = finalTable.get(i);
            for (int row = 0; row < diag.length; row++) {
                System.arraycopy(diag[row], 0, result[vertices * i + row], vertices * i, diag[row].length);
            }
        }
        return result;
    }
}
