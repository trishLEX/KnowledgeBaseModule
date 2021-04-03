package ru.fa;

import ru.fa.model.Dimension;

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

public class GraphGenerator {

    private static final int VERTICES = 30;
    private static final int COMPONENTS = 1;

    private static final int CHILD_SIZE = 2;

    private static final Map<Long, Dimension> DIMENSION_MAP = new HashMap<>();

    public static void main (String[] args) {
        for (int i = 0; i < COMPONENTS; i++) {
            long[][] table = new long[VERTICES][VERTICES];

            long id = DIMENSION_MAP.values()
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
            DIMENSION_MAP.put(id, root);
            LinkedList<Dimension> dimensions = new LinkedList<>();
            dimensions.push(root);
            createChildren(dimensions, table, i);

            for (Dimension d: DIMENSION_MAP.values()) {
                d.addAllChildrenIds(getAllChildrenIds(d, new HashSet<>()));
            }
            System.out.println(tableToString(table));
        }
    }

    private static void createChildren(LinkedList<Dimension> dimensions, long[][] table, int typeId) {
        Dimension dimension = dimensions.pollLast();
        if (dimension == null) {
            return;
        }

        List<Dimension> children = new ArrayList<>();
        for (int i = 0; i < CHILD_SIZE; i++) {
            long offset = i + 1;
            for (long adjOffset = offset; adjOffset < VERTICES; adjOffset++) {
                Optional<Dimension> child = Optional.ofNullable(DIMENSION_MAP.get(dimension.getId() + adjOffset));
                int childLevel = child.map(Dimension::getLevel)
                        .orElse(Integer.MAX_VALUE);
                if (childLevel > dimension.getLevel() && child.map(Dimension::getParentId).isEmpty()
                ) {
                    offset = adjOffset;
                    break;
                }
            }
            if (dimension.getId() + offset >= VERTICES * (typeId + 1)) {
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
            table[(int) dimension.getId() % VERTICES][(int) child.getId() % VERTICES] = 1;
            DIMENSION_MAP.put(child.getId(), child);
        }

        for (var child: children) {
            dimensions.push(child);
        }
        createChildren(dimensions, table, typeId);
    }

    private static Set<Long> getAllChildrenIds(Dimension dimension, Set<Long> childrenIds) {
        childrenIds.addAll(dimension.getChildrenIds());
        dimension.getChildrenIds()
                .stream()
                .map(DIMENSION_MAP::get)
                .map(d -> getAllChildrenIds(d, childrenIds))
                .flatMap(Collection::stream)
                .forEach(childrenIds::add);
        return childrenIds;
    }

    private static String tableToString(long[][] table) {
        String lineSeparator = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        for (long[] row : table) {
            sb.append(Arrays.toString(row)).append(lineSeparator);
        }

        return sb.toString();
    }
}
