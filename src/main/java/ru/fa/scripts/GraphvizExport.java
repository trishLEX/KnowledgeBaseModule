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
import java.util.Set;
import java.util.stream.Collectors;

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
                        "select * from test.dimension where id in (select distinct dimension_id\n" +
                        "from observation_dimension_v2 od\n" +
                        "where observation_id in (1696, 949, 2876, 2878))",
                Collections.emptyMap(),
                CommonImport::mapDimension
        );

        Set<Long> dimIds = dimensions.stream().map(Dimension::getId).collect(Collectors.toSet());
        dimensions.forEach(
                d -> d.setChildrenIds(
                        d.getChildrenIds()
                                .stream()
                                .filter(dimIds::contains)
                                .collect(Collectors.toList())
                )
        );

        List<Long> observationDims = namedJdbcTemplate.queryForList(
                "select distinct dimension_id from test.observation_dimension_v2 where observation_id in (1696, 949, 2876, 2878)",
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
