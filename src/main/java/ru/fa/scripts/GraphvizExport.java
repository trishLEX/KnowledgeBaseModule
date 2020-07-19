package ru.fa.scripts;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

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
                "select * from dimension",
                Collections.emptyMap(),
                CommonImport::mapDimension
        );

        List<Long> observationDims = namedJdbcTemplate.queryForList(
                "select dimension_id from observation_dimension where observation_id = :obsId",
                new MapSqlParameterSource("obsId", observationId),
                Long.class
        );

        try (PrintWriter writer = new PrintWriter(output)) {

            writer.println("digraph g {");
            for (Dimension dimension : dimensions) {
                writer.println("\t" + dimension.getId() + " [label=\"" + dimension.getId() + "(" + dimension.getStrId() + ")\"];");
            }

            for (Dimension dimension : dimensions) {
                for (long child : dimension.getChildrenIds()) {
                    writer.println("\t" + dimension.getId() + " -> " + child + ";");
                }
            }

            if (!observationDims.isEmpty()) {
                writer.println("\tsubgraph observation {");
                writer.println("\t\tcolor=red;");
                for (long obsDim : observationDims) {
                    writer.println("\t\t" + obsDim + ";");
                }
                writer.println("\t}");
            }

            writer.println("}");
        }
    }
}
