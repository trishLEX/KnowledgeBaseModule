package ru.fa.scripts;

import org.apache.jena.atlas.lib.Pair;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class ObservationChecker {

    public static void main(String[] args) {
        NamedParameterJdbcTemplate namedJdbcTemplate = CommonImport.createNamedJdbcTemplate();

        List<Observation> observations = CommonImport.getAllObservations(namedJdbcTemplate);

        List<Pair<Observation, Observation>> higherObservations = new ArrayList<>();
        for (Observation observation : observations) {
            for (Observation anotherObservation : observations) {
                if (!observation.equals(anotherObservation)) {
                    boolean firstHigherAnother = true;
                    boolean anotherHigherFirst = true;
                    for (DimensionSubType dimensionSubType : DimensionSubType.values()) {
                        if (dimensionSubType == DimensionSubType.REGISTRATION) {
                            continue;
                        }
                        List<Long> firstChildren = observation.getDimension(dimensionSubType).getAllChildrenIds();
                        Dimension anotherDimension = anotherObservation.getDimension(dimensionSubType);
                        if (!firstChildren.contains(anotherDimension.getId())) {
                            firstHigherAnother = false;
                        }

                        List<Long> anotherChildren = anotherObservation.getDimension(dimensionSubType).getAllChildrenIds();
                        Dimension firstDimension = observation.getDimension(dimensionSubType);
                        if (!anotherChildren.contains(firstDimension.getId())) {
                            anotherHigherFirst = false;
                        }
                    }

                    if (firstHigherAnother && anotherHigherFirst) {
                        throw new IllegalStateException("WTF: " + observation + " " + anotherObservation);
                    } else if (firstHigherAnother ^ anotherHigherFirst) {
                        if (firstHigherAnother) {
                            higherObservations.add(new Pair<>(observation, anotherObservation));
                        } else {
                            higherObservations.add(new Pair<>(anotherObservation, observation));
                        }
                    }
                }
            }
        }

        System.out.println(higherObservations.size());
    }
}
