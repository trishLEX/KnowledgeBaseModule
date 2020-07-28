package ru.fa.dao;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.fa.model.Dimension;
import ru.fa.model.DimensionSubType;
import ru.fa.model.DimensionType;
import ru.fa.model.Observation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
public class ObservationDao {

    private static final String GET_OBSERVATION_IDS_BY_DIMENSIONS = "" +
            "select observation_id\n" +
            "from observation_dimension_v2 od\n" +
            "where obs_dimension_id in (\n" +
            "    select id\n" +
            "    from dimension\n" +
            "    where id in (:ids)\n" +
            "    union\n" +
            "    select unnest(all_narrower) child_id\n" +
            "    from dimension\n" +
            "    where id in (:ids))\n" +
            "group by observation_id, dimension_id\n";

    private static final String GET_OBSERVATIONS_BY_IDS = "" +
            "select dimension_id, observation_id, obs_dimension_id, dimension_subtype\n" +
            "from observation_dimension_v2\n" +
            "where observation_id in (:observationIds)";

    private static final String GET_DIMENSION_SUBTYPES_TO_CLARIFY = "" +
            "select dimension_subtype, dimensions\n" +
            "from (\n" +
            "         select dimension_subtype, array_agg(dimension_id) dimensions\n" +
            "         from (\n" +
            "                  select distinct dimension_subtype, dimension_id\n" +
            "                  from observation_dimension_v2\n" +
            "                  where observation_id in (:observationIds)\n" +
            "              ) od_distinct\n" +
            "         group by dimension_subtype\n" +
            "         having cardinality(array_agg(dimension_id)) > 1\n" +
            ") od\n" +
            "join dimension_subtype ds on ds.subtype = od.dimension_subtype\n" +
            "order by num asc";

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    @Autowired
    public ObservationDao(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    public Set<Long> getObservationsIdsByDimensions(Collection<Long> dimensions) {
        Set<Long> result = new HashSet<>();
        namedJdbcTemplate.query(
                GET_OBSERVATION_IDS_BY_DIMENSIONS,
                new MapSqlParameterSource("ids", dimensions),
                rs -> {result.add(rs.getLong("observation_id"));}
        );
        return result;
    }

    public Multimap<DimensionSubType, Long> getDimensionSubTypesToClarify(Collection<Long> observationIds) {
//        List<Long> queryObservationIds = observationIds.size() > Short.MAX_VALUE
//                ? observationIds.subList(0, Short.MAX_VALUE - 1)
//                : observationIds;
        Multimap<DimensionSubType, Long> result = LinkedHashMultimap.create();
        namedJdbcTemplate.query(
                GET_DIMENSION_SUBTYPES_TO_CLARIFY,
                new MapSqlParameterSource("observationIds", observationIds),
                rs -> {
                    result.putAll(
                            DimensionSubType.valueOf(rs.getString("dimension_subtype")),
                            Arrays.asList((Long[]) rs.getArray("dimensions").getArray())
                    );
                }
        );
        return result;
    }

    public Map<Long, Observation> getObservationsByIds(Collection<Long> observationIds) {
        Map<Long, String> strIds = new HashMap<>();
        Map<Long, Map<DimensionSubType, Dimension>> obsDimensionsMap = new HashMap<>();

        namedJdbcTemplate.query(
                GET_OBSERVATIONS_BY_IDS,
                new MapSqlParameterSource("observationIds", observationIds),
                rs -> {
                    strIds.put(rs.getLong("o.id"), rs.getString("o.str_id"));

                    Dimension dimension = mapDimension(rs);
                    obsDimensionsMap.computeIfAbsent(
                            rs.getLong("o.id"),
                            id -> new HashMap<>()
                    ).put(dimension.getDimensionSubType(), dimension);
                }
        );

        Map<Long, Observation> observationMap = new HashMap<>();
        for (Long observation : observationIds) {
            observationMap.put(
                    observation,
                    new Observation(
                            observation,
                            strIds.get(observation),
                            obsDimensionsMap.get(observation)
                    )
            );
        }
        return observationMap;
    }

    private static Dimension mapDimension(ResultSet rs) throws SQLException {
        return Dimension.newBuilder()
                .setId(rs.getLong("d.id"))
                .setLevel(rs.getInt("d.level"))
                .setStrId(rs.getString("d.str_id"))
                .setLabel(rs.getString("d.label"))
                .setDimensionType(DimensionType.valueOf(rs.getString("d.type")))
                .setDimensionSubType(DimensionSubType.valueOf(rs.getString("d.subtype")))
                .setParentId(rs.getLong("d.broader"))
                .setAllChildrenIds(Arrays.asList((Long[]) rs.getArray("d.all_narrower").getArray()))
                .setChildrenIds(Arrays.asList((Long[]) rs.getArray("d.narrower").getArray()))
                .setQuestion(rs.getString("d.question"))
                .build();
    }

    private static DimensionSubType mapDimensionSubtype(ResultSet rs, int rn) throws SQLException {
        return DimensionSubType.valueOf(rs.getString("dimension_subtype"));
    }
}
