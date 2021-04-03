package ru.fa;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ru.fa.model.Dimension;
import ru.fa.model.Observation;
import ru.fa.model.Value;

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
import java.util.stream.Collectors;

public class GraphGenerator {

    //скорость роста наблюдений: в большую сторону log_2(VERTICES)^COMPONENTS

    private static final int VERTICES = 15;
    private static final int COMPONENTS = 3;

    private static final int CHILD_SIZE = 2;

    private static final Map<Long, Dimension> DIMENSION_MAP = new HashMap<>(VERTICES * COMPONENTS);
    private static final List<Dimension> ROOTS = new ArrayList<>(COMPONENTS);

    public static void main (String[] args) {
        List<long[][]> tables = new ArrayList<>();
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
            ROOTS.add(root);
            DIMENSION_MAP.put(id, root);
            LinkedList<Dimension> dimensions = new LinkedList<>();
            dimensions.push(root);
            createChildren(dimensions, table, i);

            for (Dimension d: DIMENSION_MAP.values()) {
                d.addAllChildrenIds(getAllChildrenIds(d, new HashSet<>()));
            }
            tables.add(table);
        }
//        System.out.println(tableToString(reshape(tables)));


        List<Map<String, Dimension>> observationDimensions = createObservations(0);
        List<Observation> observations = new ArrayList<>();
        for (int i = 0; i < observationDimensions.size(); i++) {
            observations.add(new Observation(
                    i,
                    "Observation" + i,
                    observationDimensions.get(i)
            ));
        }

        List<Value> values = observations.stream()
                .map(o -> new Value(
                        o.getId(),
                        "Value" + o.getId(),
                        new ObjectNode(JsonNodeFactory.instance)
                                .put("id", o.getId())
                                .put("body", "superBody"),
                        "VALUE_TYPE_1"
                )).collect(Collectors.toList());
    }

    private static List<Map<String, Dimension>> createObservations(int rootIndex) {
        List<Dimension> current = getDimensionsForObservation(ROOTS.get(rootIndex));
        current.add(ROOTS.get(rootIndex));
        if (rootIndex == COMPONENTS - 1) {
            return current.stream()
                    .map(d -> {
                        Map<String, Dimension> map = new HashMap<>();
                        map.put(d.getDimensionSubType(), d);
                        return map;
                    })
                    .collect(Collectors.toList());
        }

        List<Map<String, Dimension>> next = createObservations(rootIndex + 1);

        List<Map<String, Dimension>> result = new ArrayList<>();
        for (Dimension dimension : current) {
            for (Map<String, Dimension> dimensionMap : next) {
                Map<String, Dimension> map = new HashMap<>(dimensionMap);
                map.put(dimension.getDimensionSubType(), dimension);
                result.add(map);
            }
        }
        return result;
    }

    private static List<Dimension> getDimensionsForObservation(Dimension root) {
        List<Dimension> dimensions = root.getChildrenIds()
                .stream()
                .limit(CHILD_SIZE - 1)
                .map(DIMENSION_MAP::get)
                .collect(Collectors.toList());
        List<Dimension> observationDimensions = new ArrayList<>(dimensions);
        dimensions.stream()
                .map(GraphGenerator::getDimensionsForObservation)
                .forEach(observationDimensions::addAll);
        return observationDimensions;
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

    private static long[][] reshape(List<long[][]> finalTable) {
        long[][] result = new long[COMPONENTS * VERTICES][COMPONENTS * VERTICES];
        for (int i = 0; i < finalTable.size(); i++) {
            long[][] diag = finalTable.get(i);
            for (int row = 0; row < diag.length; row++) {
                System.arraycopy(diag[row], 0, result[VERTICES * i + row], VERTICES * i, diag[row].length);
            }
        }
        return result;
    }
}
