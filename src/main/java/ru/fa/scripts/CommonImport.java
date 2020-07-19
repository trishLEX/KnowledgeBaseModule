package ru.fa.scripts;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CommonImport {

    public static void importData(String sheet, DimensionType dimensionType, int startId, String stopStrId) throws Exception {
        NamedParameterJdbcTemplate namedJdbcTemplate = createNamedJdbcTemplate();
        XSSFWorkbook owlContent = new XSSFWorkbook(new FileInputStream("src/main/resources/ru/fa/OWL_content.xlsm"));
        XSSFSheet operationKind = owlContent.getSheet(sheet);
        Iterator<Row> rows = operationKind.rowIterator();
        int i = startId;
        DimensionSubType dimensionSubType = null;
        rows.next();
        rows.next();

        Map<Integer, Integer> idMap = new HashMap<>();
        while (rows.hasNext()) {
            Row row = rows.next();
            if (row.getCell(9) != null && row.getCell(9).getStringCellValue().equals(stopStrId)) {
                break;
            }
            if (row.getCell(9) == null || row.getCell(9).getStringCellValue().isBlank()) {
                continue;
            }
            int id = i;

            int index = IntStream.of(0, 1, 2, 3, 4, 5)
                    .boxed()
                    .filter(idx -> row.getCell(idx) != null)
                    .filter(idx -> !row.getCell(idx).getStringCellValue().isBlank())
                    .findFirst()
                    .orElseThrow();
            idMap.put(index, id);

            Integer broader;
            if (!row.getCell(0).getStringCellValue().equals("")) {
                broader = null;
                dimensionSubType = owlToSubType(row.getCell(9).getStringCellValue());
            } else {
                broader = idMap.get(index - 1);
            }

            String label = Stream.of(
                    row.getCell(0),
                    row.getCell(1),
                    row.getCell(2),
                    row.getCell(3),
                    row.getCell(4),
                    row.getCell(5)
            )
                    .filter(Objects::nonNull)
                    .map(Cell::getStringCellValue)
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .orElseThrow();

            String strId = owlIdToStrId(row.getCell(9).getStringCellValue());
            String question = row.getCell(11).getStringCellValue();
            namedJdbcTemplate.update(
                    "" +
                            "insert into dimension\n" +
                            "(id, str_id, label, broader, type, subtype, question, level)\n" +
                            "values (:id, :strId, :label, :broader, :type, :subtype, :question, :level)\n" +
                            "on conflict (id) do update set\n" +
                            "(str_id, label, broader, type, subtype, question, level) = \n" +
                            "(:strId, :label, :broader, :type, :subtype, :question, :level)",
                    new MapSqlParameterSource()
                            .addValue("id", id)
                            .addValue("strId", strId)
                            .addValue("label", label)
                            .addValue("broader", broader)
                            .addValue("type", dimensionType.name())
                            .addValue("subtype", dimensionSubType.name())
                            .addValue("question", question)
                            .addValue("level", index)
            );
            i++;
        }
    }

    public static NamedParameterJdbcTemplate createNamedJdbcTemplate() {
        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .password("0212")
                .username("postgres")
                .url("jdbc:postgresql://localhost:5432/kn_base")
                .build();
        return new NamedParameterJdbcTemplate(dataSource);
    }

    public static String owlIdToStrId(String owlId) {
        return owlId.replace("_", "");
    }

    private static DimensionSubType owlToSubType(String owlId) {
        switch (owlId) {
            case "_CardCategory":
                return DimensionSubType.CARD_CATEGORY;
            case "_CardType":
                return DimensionSubType.CARD_TYPE;
            case "_CardProduct":
                return DimensionSubType.CARD_PRODUCT;
            case "_CardPaymentSystem":
                return DimensionSubType.CARD_PAYMENT_SYSTEM;
            case "_CardSpecific":
                return DimensionSubType.CARD_SPECIFIC;
            case "_CardCurrency":
                return DimensionSubType.CARD_CURRENCY;
            case "_CardLevel":
                return DimensionSubType.CARD_LEVEL;
            case "_CardBank":
                return DimensionSubType.CARD_BANK;
            case "_CardSpecialTariffPlan":
                return DimensionSubType.CARD_SPECIAL_TARIFF_PLAN;
            case "_CardServicePackage":
                return DimensionSubType.CARD_SERVICE_PACKAGE;
            case "_CardIndividualDesign":
                return DimensionSubType.CARD_INDIVIDUAL_DESIGN;
            case "_CardTransportApp":
                return DimensionSubType.CARD_TRANSPORT_APP;
            case "_TypeOfOffer":
                return DimensionSubType.TYPE_OF_OFFER;
            case "_CardOperation":
                return DimensionSubType.CARD_OPERATION;
            case "_OperationPlaceGeo":
                return DimensionSubType.OPERATION_PLACE_GEO;
            case "_OperationPlaceBank":
                return DimensionSubType.OPERATION_PLACE_BANK;
            case "_OperationChannel":
                return DimensionSubType.OPERATION_CHANNEL;
            case "_OperationCurrency":
                return DimensionSubType.OPERATION_CURRENCY;
            case "_Citizenship":
                return DimensionSubType.CITIZENSHIP;
            case "_Age":
                return DimensionSubType.AGE;
            case "_ClientCategory":
                return DimensionSubType.CLIENT_CATEGORY;
            case "_ClientType":
                return DimensionSubType.CLIENT_TYPE;
            case "_Registration":
                return DimensionSubType.REGISTRATION;
            default:
                return null;
        }
    }

    public static Dimension mapDimension(ResultSet rs, int rn) throws SQLException {
        return Dimension.newBuilder()
                .setId(rs.getLong("id"))
                .setLevel(rs.getInt("level"))
                .setStrId(rs.getString("str_id"))
                .setLabel(rs.getString("label"))
                .setDimensionType(DimensionType.valueOf(rs.getString("type")))
                .setDimensionSubType(DimensionSubType.valueOf(rs.getString("subtype")))
                .setParentId(rs.getLong("broader"))
                .setAllChildrenIds(Arrays.asList((Long[]) rs.getArray("all_narrower").getArray()))
                .setChildrenIds(Arrays.asList((Long[]) rs.getArray("narrower").getArray()))
                .setQuestion(rs.getString("question"))
                .build();
    }
}
