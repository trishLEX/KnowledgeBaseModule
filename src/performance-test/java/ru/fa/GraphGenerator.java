package ru.fa;

import ru.fa.model.Dimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GraphGenerator {

    private static final int VERTICES = 5;
    private static final int COMPONENTS = 1;

    private static final int CHILD_SIZE = 2;

    private static final Map<Long, Dimension> DIMENSION_MAP = new HashMap<>();

    public static void main (String[] args) {
        for (int i = 0; i < COMPONENTS; i++) {
            long[][] table = new long[VERTICES][VERTICES];

            Dimension root = Dimension.newBuilder()
                    .setId(0)
                    .setLevel(0)
                    .setLabel("LABEL_" + 0)
                    .build();
            DIMENSION_MAP.put(0L, root);
            createChildren(root, table);

            for (Dimension d: DIMENSION_MAP.values()) {
                d.addAllChildrenIds(getAllChildrenIds(d, new HashSet<>()));
            }
            System.out.println(tableToString(table));
        }
    }

    private static void createChildren(Dimension dimension, long[][] table) {
        if (dimension.getLevel() == VERTICES - 1) {
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
            if (dimension.getId() + offset >= VERTICES) {
                continue;
            }

            Dimension child = Dimension.newBuilder()
                    .setId(dimension.getId() + offset)
                    .setParentId(dimension.getId())
                    .setLevel(dimension.getLevel() + 1)
                    .setLabel("LABEL_" + (dimension.getId() + offset))
                    .build();
            dimension.addChildId(child.getId());
            children.add(child);
            table[(int) dimension.getId()][(int) child.getId()] = 1;
            DIMENSION_MAP.put(child.getId(), child);
        }

        for (var child: children) {
            createChildren(child, table);
        }
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
