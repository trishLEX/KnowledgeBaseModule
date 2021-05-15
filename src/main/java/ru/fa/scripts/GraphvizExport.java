package ru.fa.scripts;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.fa.model.Dimension;

import java.io.File;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GraphvizExport {

    public static void main(String[] args) throws Exception {
        long observationId = 0;

        File output = new File(
                "src/main/resources/graphviz",
                LocalDateTime.now()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .format(DateTimeFormatter.ISO_DATE_TIME)
                        .replace(":", "-")
        );
        output.createNewFile();

        NamedParameterJdbcTemplate namedJdbcTemplate = CommonImport.createNamedJdbcTemplate();
        List<Dimension> dimensions = namedJdbcTemplate.query(
                "" +
                        "select * from test.dimension\n",
                Collections.emptyMap(),
                GraphvizExport::mapDimension
        );

        List<List<Long>> observationDims = namedJdbcTemplate.query(
                "" +
                        "select array_agg(distinct dimension_id) dims\n" +
                        "from test.observation_dimension_v2\n" +
                        "group by observation_id", //where observation_id in (1)",
                new MapSqlParameterSource("obsId", observationId),
                (rs, rn) -> List.of((Long[]) rs.getArray("dims").getArray())
        );

        try (PrintWriter writer = new PrintWriter(output)) {

            writer.println("digraph g {");
            for (Dimension dimension : dimensions) {
                writer.println("\t" + dimension.getId() + " [label=\"" + dimension.getId() + "\"];");
            }

            for (Dimension dimension : dimensions) {
                for (long child : dimension.getChildrenIds()) {
                    writer.println("\t" + dimension.getId() + " -> " + child + ";");
                }
            }

            if (!observationDims.isEmpty()) {
                for (List<Long> obsDimIds : observationDims) {
                    String color = generateColor();
                    for (var obsDimId : obsDimIds) {
                        writer.println("\t" + obsDimId + " [color=\"" + color + "\"]\n");
//                writer.println("\tsubgraph observation {");
//                writer.println("\t\tcolor=red;");
//                for (long obsDim : observationDims) {
//                    writer.println("\t\t" + obsDim + ";");
//                }
//                writer.println("\t}");
                    }
                }
            }

            writer.println("}");
        }
    }

    private static Dimension mapDimension(ResultSet rs, int rn) throws SQLException {
        return Dimension.newBuilder()
                .setId(rs.getLong("id"))
                .setLevel(rs.getInt("level"))
                .setStrId(rs.getString("str_id"))
                .setLabel(rs.getString("label"))
                .setDimensionType(rs.getString("type"))
                .setDimensionSubType(rs.getString("subtype"))
                .setParentId(rs.getLong("broader"))
                .setAllChildrenIds(Set.of((Long[]) rs.getArray("all_narrower").getArray()))
                .setChildrenIds(Set.of((Long[]) rs.getArray("narrower").getArray()))
                .setQuestion(rs.getString("question"))
                .build();
    }

    private static String generateColor() {
        Random r = new Random();
        final char [] hex = { '0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char [] s = new char[7];
        int     n = r.nextInt(0x1000000);

        s[0] = '#';
        for (int i=1;i<7;i++) {
            s[i] = hex[n & 0xf];
            n >>= 4;
        }
        return new String(s);
    }
}
