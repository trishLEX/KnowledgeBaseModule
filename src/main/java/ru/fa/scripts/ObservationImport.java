package ru.fa.scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.fa.scripts.CommonImport.createNamedJdbcTemplate;
import static ru.fa.scripts.CommonImport.owlIdToStrId;

public class ObservationImport {

    public static void main(String[] args) throws Exception {
        NamedParameterJdbcTemplate namedJdbcTemplate = createNamedJdbcTemplate();

        XSSFWorkbook owlContent = new XSSFWorkbook(new FileInputStream("E:\\Sorry\\Documents\\IdeaProjects\\KnBase\\src\\main\\resources\\ru\\fa\\OWL_content.xlsm"));
        XSSFSheet operationKind = owlContent.getSheet("Observations");

        HashMultimap<String, Dimension> cards = getCardsDimensions(owlContent, namedJdbcTemplate);
        HashMultimap<String, Dimension> clients = getClientsDimensions(owlContent, namedJdbcTemplate);


        Iterator<Row> rows = operationKind.rowIterator();
        rows.next();

        int id = 1;
        while (rows.hasNext()) {
            Row row = rows.next();
            if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isBlank()) {
                continue;
            }
            long obsNum = Long.parseLong(
                    owlIdToStrId(
                            row.getCell(0).getStringCellValue()
                    ).replace("O", "")
            );
            if (obsNum <= 2010) {
                id++;
                if (id % 100 == 0) {
                    System.out.println("ID " + id);
                }
                continue;
            }
            String strId = row.getCell(0).getStringCellValue();
            Set<Dimension> clientsDimensions = clients.get(row.getCell(1).getStringCellValue());
            Set<Dimension> cardsDimensions = cards.get(row.getCell(2).getStringCellValue());
            Dimension opKind = findDimensionById(row.getCell(3).getStringCellValue(), namedJdbcTemplate);
            Dimension opPlaceGeo = findDimensionById(row.getCell(4).getStringCellValue(), namedJdbcTemplate);
            Dimension opPlaceBank = findDimensionById(row.getCell(5).getStringCellValue(), namedJdbcTemplate);
            Dimension opChannel = findDimensionById(row.getCell(6).getStringCellValue(), namedJdbcTemplate);
            Dimension opCurrency = findDimensionById(row.getCell(7).getStringCellValue(), namedJdbcTemplate);

            Set<Dimension> dimensions = new HashSet<>(clientsDimensions);
            dimensions.addAll(cardsDimensions);
            dimensions.add(opKind);
            dimensions.add(opPlaceGeo);
            dimensions.add(opPlaceBank);
            dimensions.add(opChannel);
            dimensions.add(opCurrency);

            if (dimensions.size() != 22) {
                throw new IllegalStateException();
            }

            Value feeWithinLimit = findValueById(
                    row.getCell(8).getStringCellValue(),
                    ValueType.FEE_VALUE,
                    namedJdbcTemplate
            );
            Value feeAboveLimit = findValueById(
                    row.getCell(9).getStringCellValue(),
                    ValueType.FEE_VALUE,
                    namedJdbcTemplate
            );
            Value limitPerMonth = findValueById(
                    row.getCell(10).getStringCellValue(),
                    ValueType.LIMIT_VALUE,
                    namedJdbcTemplate
            );
            Value limitPerDay = findValueById(
                    row.getCell(11).getStringCellValue(),
                    ValueType.LIMIT_VALUE,
                    namedJdbcTemplate
            );
            Value limitPerOperation = findValueById(
                    row.getCell(12).getStringCellValue(),
                    ValueType.LIMIT_VALUE,
                    namedJdbcTemplate
            );
            Value tariff = findValueById(
                    row.getCell(13).getStringCellValue(),
                    ValueType.TARIFF_VALUE,
                    namedJdbcTemplate
            );
            Value documents = findValueById(
                    row.getCell(14).getStringCellValue(),
                    ValueType.DOCUMENTS_VALUE,
                    namedJdbcTemplate
            );
            Value channels = findValueById(
                    row.getCell(15).getStringCellValue(),
                    ValueType.CHANNEL_VALUE,
                    namedJdbcTemplate
            );
            Value information = findValueById(
                    row.getCell(16).getStringCellValue(),
                    ValueType.INFORMATION_VALUE,
                    namedJdbcTemplate
            );
            Value creditLimit = findValueById(
                    row.getCell(17).getStringCellValue(),
                    ValueType.LIMIT_VALUE,
                    namedJdbcTemplate
            );
            Value period = findValueById(
                    row.getCell(18).getStringCellValue(),
                    ValueType.PERIOD_VALUE,
                    namedJdbcTemplate
            );
            Map<ValueSubType, Value> values = ImmutableMap.<ValueSubType, Value>builder()
                    .put(ValueSubType.TRANSACTION_FEE_WITHIN_LIMIT, feeWithinLimit)
                    .put(ValueSubType.TRANSACTION_FEE_ABOVE_LIMIT, feeAboveLimit)
                    .put(ValueSubType.TRANSACTION_LIMIT_PER_MONTH, limitPerMonth)
                    .put(ValueSubType.TRANSACTION_LIMIT_PER_DAY, limitPerDay)
                    .put(ValueSubType.TRANSACTION_LIMIT_PER_OPERATION, limitPerOperation)
                    .put(ValueSubType.TARIFF, tariff)
                    .put(ValueSubType.REQUIRED_DOCUMENTS, documents)
                    .put(ValueSubType.CHANNELS, channels)
                    .put(ValueSubType.INFORMATION, information)
                    .put(ValueSubType.CREDIT_LIMIT, creditLimit)
                    .put(ValueSubType.PERIOD, period)
                    .build();

            Observation observation = new Observation(obsNum, strId, dimensions, new HashSet<>(values.values()));
            List<ObservationConflict> conflicts = validateObservation(observation, namedJdbcTemplate);
            if (!conflicts.isEmpty()) {
                throw new IllegalStateException(conflicts.toString());
            }

            namedJdbcTemplate.update(
                    "" +
                            "insert into observation\n" +
                            "(id, str_id)\n" +
                            "values (:id, :strId)\n",
                    new MapSqlParameterSource()
                            .addValue("id", obsNum)
                            .addValue("strId", strId)
            );

            for (Map.Entry<ValueSubType, Value> valueEntry : values.entrySet()) {
                namedJdbcTemplate.update(
                        "" +
                                "insert into observation_value (observation_id, value_id, value_subtype)\n" +
                                "values (:observationId, :valueId, :valueSubtype)",
                        new MapSqlParameterSource()
                            .addValue("observationId", obsNum)
                            .addValue("valueId", valueEntry.getValue().getId())
                            .addValue("valueSubtype", valueEntry.getKey().name())
                );
            }

            for (Dimension dimension : dimensions) {
                namedJdbcTemplate.update(
                        "" +
                                "insert into observation_dimension (dimension_id, observation_id)\n" +
                                "values (:dimensionId, :observationId)",
                        new MapSqlParameterSource()
                            .addValue("dimensionId", dimension.getId())
                            .addValue("observationId", obsNum)
                );
            }

            if (id % 100 == 0) {
                System.out.println("ID " + id);
            }
            id++;
        }
    }

    private static HashMultimap<String, Dimension> getCardsDimensions(
            XSSFWorkbook owlContent,
            NamedParameterJdbcTemplate namedJdbcTemplate
    ) {
        XSSFSheet cards = owlContent.getSheet("Cards");
        Iterator<Row> rows = cards.rowIterator();
        rows.next();
        rows.next();
        HashMultimap<String, Dimension> result = HashMultimap.create();

        while (rows.hasNext()) {
            Row row = rows.next();
            if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isBlank()) {
                continue;
            }

            String id = row.getCell(0).getStringCellValue();

            for (int i = 3; i < 16; i++) {
                String xlsId = row.getCell(i).getStringCellValue();
                Dimension dimension = findDimensionById(xlsId, namedJdbcTemplate);
                result.put(id, dimension);
            }
        }

        return result;
    }

    private static HashMultimap<String, Dimension> getClientsDimensions(
            XSSFWorkbook owlContent,
            NamedParameterJdbcTemplate namedJdbcTemplate
    ) {
        XSSFSheet cards = owlContent.getSheet("Clients");
        Iterator<Row> rows = cards.rowIterator();
        rows.next();
        rows.next();
        HashMultimap<String, Dimension> result = HashMultimap.create();

        while (rows.hasNext()) {
            Row row = rows.next();
            if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isBlank()) {
                continue;
            }
            String id = row.getCell(0).getStringCellValue();

            for (int i = 2; i < 6; i++) {
                String xlsId = row.getCell(i).getStringCellValue();
                Dimension dimension = findDimensionById(xlsId, namedJdbcTemplate);
                result.put(id, dimension);
            }
        }

        return result;
    }

    private static Dimension findDimensionById(String xlsId, NamedParameterJdbcTemplate namedJdbcTemplate) {
        String strId = owlIdToStrId(xlsId);

        return namedJdbcTemplate.query(
                "select * from dimension where str_id = :strId",
                new MapSqlParameterSource("strId", strId),
                CommonImport::mapDimension
        ).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No dimension with id " + strId));
    }

    private static Value findValueById(String xlsId, ValueType type, NamedParameterJdbcTemplate namedJdbcTemplate) {
        String strId = owlIdToStrId(xlsId);

        return namedJdbcTemplate.query(
                "select * from value where str_id = :strId and type = :type",
                new MapSqlParameterSource("strId", strId).addValue("type", type.name()),
                ObservationImport::mapValue
        ).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No value with id: " + strId + " and type: " + type));
    }

    private static List<Dimension> findDimensionByIds(Collection<Long> ids, NamedParameterJdbcTemplate namedJdbcTemplate) {
        return namedJdbcTemplate.query(
                "select * from dimension where id in (:ids)",
                new MapSqlParameterSource("ids", ids),
                CommonImport::mapDimension
        );
    }

    private static List<Value> findValuesByIds(Collection<Long> ids, NamedParameterJdbcTemplate namedJdbcTemplate) {
        return namedJdbcTemplate.query(
                "select * from value where id in (:ids)",
                new MapSqlParameterSource("ids", ids),
                ObservationImport::mapValue
        );
    }

    private static Value mapValue(ResultSet rs, int rn) throws SQLException {
        try {
            return new Value(
                    rs.getLong("id"),
                    rs.getString("str_id"),
                    new ObjectMapper().readTree(rs.getString("content")),
                    ValueType.valueOf(rs.getString("type"))
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Observation> getAllObservations(NamedParameterJdbcTemplate namedJdbcTemplate) {
        List<Long> obsIds = namedJdbcTemplate.queryForList(
                "select id from observation",
                Collections.emptyMap(),
                Long.class
        );

        Map<Long, Set<Dimension>> obsDimensions = new HashMap<>();
        Map<Long, Set<Value>> obsValues = new HashMap<>();
        for (long obsId : obsIds) {
            Set<Dimension> dimensions = new HashSet<>(namedJdbcTemplate.query(
                    "select * from dimension d join observation_dimension od on d.id = od.dimension_id where observation_id = :obsId",
                    new MapSqlParameterSource("obsId", obsId),
                    CommonImport::mapDimension
            ));
            obsDimensions.put(obsId, dimensions);

            Set<Value> values = new HashSet<>(namedJdbcTemplate.query(
                    "select * from value v join observation_value ov on v.id = ov.value_id where observation_id = :obsId",
                    new MapSqlParameterSource("obsId", obsId),
                    ObservationImport::mapValue
            ));
            obsValues.put(obsId, values);
        }

        return namedJdbcTemplate.query(
                "select * from observation",
                Collections.emptyMap(),
                (rs, rn) -> new Observation(
                        rs.getLong("id"),
                        rs.getString("str_id"),
                        obsDimensions.get(rs.getLong("id")),
                        obsValues.get(rs.getLong("id"))
                )
        );
    }

    public static List<ObservationConflict> validateObservation(Observation observation, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        List<Observation> observations = getAllObservations(namedParameterJdbcTemplate);
        Map<Observation, Set<Dimension>> intersections = observations.stream()
                .filter(o -> isPotentialIntersect(o, observation))
                .map(o -> Pair.of(o, createIntersection(o, observation, namedParameterJdbcTemplate)))
                .filter(pair -> CollectionUtils.isNotEmpty(pair.getRight()))
                .collect(
                        Collectors.toMap(
                                Pair::getKey,
                                Pair::getValue
                        )
                );

        if (intersections.isEmpty()) {
            return Collections.emptyList();
        }

        List<ObservationConflict> conflicts = new ArrayList<>();
        for (Map.Entry<Observation, Set<Dimension>> intersection : intersections.entrySet()) {
            if (observations.stream().noneMatch(o -> isSame(o, intersection.getValue()))) {
                conflicts.add(
                        new ObservationConflict(
                                ImmutableSet.of(observation, intersection.getKey()),
                                intersection.getValue())
                );
            }
        }
        return conflicts;
    }

    private static boolean isPotentialIntersect(Observation node1, Observation node2) {
        if (node1.equals(node2)) {
            return false;
        }
        Set<DimensionSubType> topConcepts = node1.getDimensionMap().keySet();

        boolean isAllNegative = false;
        boolean isAllPositive = false;

        for (DimensionSubType topConcept : topConcepts) {
            int level1 = node1.getDimension(topConcept) == null ? 0 :node1.getDimension(topConcept).getLevel();
            int level2 = node2.getDimension(topConcept) == null ? 0 :node2.getDimension(topConcept).getLevel();

            if (level1 < level2) {
                isAllNegative = true;
            } else if (level1 > level2) {
                isAllPositive = true;
            } else {
                return true;
            }

            if (isAllNegative && isAllPositive) {
                return true;
            }
        }

        return false;
    }

    private static Set<Dimension> createIntersection(
            Observation node1,
            Observation node2,
            NamedParameterJdbcTemplate namedJdbcTemplate
    ) {
        Set<DimensionSubType> topConcepts = node1.getDimensionMap().keySet();
        Set<Dimension> intersection = new HashSet<>();
        for (DimensionSubType topConcept : topConcepts) {
            Dimension lowerDimValue = node1.getDimension(topConcept);
            Dimension higherDimValue = node2.getDimension(topConcept);

//            if (lowerDimValue == null) {
//                lowerDimValue = new Dimension(topConcept.getTopConceptNode(), topConcept, 0);
//            }
//            if (higherDimValue == null) {
//                higherDimValue = new DimensionValueNode(topConcept.getTopConceptNode(), topConcept, 0);
//            }

            if (lowerDimValue.getLevel() < higherDimValue.getLevel()) {
                Dimension temp = lowerDimValue;
                lowerDimValue = higherDimValue;
                higherDimValue = temp;
            }

            List<Dimension> lowerDimValueParents = getAllParentWithNode(lowerDimValue, namedJdbcTemplate);
            if (!lowerDimValueParents.contains(higherDimValue)) {
                return Collections.emptySet();
            }

            intersection.add(lowerDimValue);
        }

        return intersection;
    }

    private static boolean isSame(Observation observation, Set<Dimension> dimensionValues) {
        return Sets.newHashSet(observation.getDimensionMap().values()).equals(dimensionValues);
    }

    private static List<Dimension> getAllParentWithNode(
            Dimension dimension,
            NamedParameterJdbcTemplate namedJdbcTemplate
    ) {
        List<Dimension> result = new ArrayList<>();

        List<Long> parentIds = namedJdbcTemplate.queryForList(
                "select search_dimension_parents(:dimId)",
                new MapSqlParameterSource("dimId", dimension.getId()),
                Long.class
        );

        return namedJdbcTemplate.query(
                "select * from dimension where id in (:ids)",
                new MapSqlParameterSource("ids", parentIds),
                CommonImport::mapDimension
        );
    }
}
