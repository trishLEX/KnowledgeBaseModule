package ru.fa.scripts;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.fa.model.ValueType;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static ru.fa.scripts.CommonImport.createNamedJdbcTemplate;
import static ru.fa.scripts.CommonImport.owlIdToStrId;

public class ValueImport {

    private static final List<String> VALUES_SHEETS = List.of(
            "LimitValue",
            "FeeValue",
            "TariffValue",
            "ChannelValue",
            "DocumentsValue",
            "InformationValue",
            "PeriodValue"
    );

    private static final Map<String, ValueType> VALUES_TYPES = Map.of(
            "LimitValue", ValueType.LIMIT_VALUE,
            "FeeValue", ValueType.FEE_VALUE,
            "TariffValue", ValueType.TARIFF_VALUE,
            "ChannelValue", ValueType.CHANNEL_VALUE,
            "DocumentsValue", ValueType.DOCUMENTS_VALUE,
            "InformationValue", ValueType.INFORMATION_VALUE,
            "PeriodValue", ValueType.PERIOD_VALUE
    );

    private static final String MERGE_VALUES = "" +
            "insert into value values (:id, :strId, :content, :type)\n" +
            "on conflict (id) do update set (str_id, content, type) = (:strId, :content, :type)";

    public static void main(String[] args) throws Exception {
        NamedParameterJdbcTemplate namedJdbcTemplate = createNamedJdbcTemplate();

        XSSFWorkbook owlContent = new XSSFWorkbook(new FileInputStream("src/main/resources/ru/fa/OWL_content.xlsm"));

        int id = 1;
        for (String valuesSheet : VALUES_SHEETS) {
            XSSFSheet operationKind = owlContent.getSheet(valuesSheet);
            Iterator<Row> rows = operationKind.rowIterator();

            Row header = rows.next();

            Map<Integer, String> colsNames = new HashMap<>();
            short lastCell = header.getLastCellNum();
            for (int i = 0; i < lastCell; i++) {
                colsNames.put(i, header.getCell(i).getStringCellValue());
            }

            while (rows.hasNext()) {
                Row row = rows.next();
                if (row.getCell(0) == null || row.getCell(0).getStringCellValue().isBlank()) {
                    continue;
                }
                String strId = owlIdToStrId(row.getCell(0).getStringCellValue());

                JsonObject jsonNode = new JsonObject();
                for (int col = 1; col < lastCell; col++) {
                    if (row.getCell(col) == null) {
                        jsonNode.put(colsNames.get(col), StringUtils.EMPTY);
                        continue;
                    }
                    CellType type = row.getCell(col).getCellType();
                    switch (type) {
                        case STRING:
                            jsonNode.put(colsNames.get(col), row.getCell(col).getStringCellValue());
                            break;
                        case NUMERIC:
                            jsonNode.put(colsNames.get(col), Double.toString(row.getCell(col).getNumericCellValue()));
                            break;
                        default:
                            jsonNode.put(colsNames.get(col), row.getCell(col).getStringCellValue());
                    }
                }

                PGobject content = new PGobject();
                content.setType("jsonb");
                content.setValue(jsonNode.toString());

                namedJdbcTemplate.update(
                        MERGE_VALUES,
                        new MapSqlParameterSource()
                            .addValue("id", id)
                            .addValue("strId", strId)
                            .addValue("content", content)
                            .addValue("type", VALUES_TYPES.get(valuesSheet).name())
                );
                id++;
            }
        }
    }
}
