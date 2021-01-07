package ru.fa.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import ru.fa.FunctionalTest;
import ru.fa.JsonAsserts;

class ObservationControllerTest extends FunctionalTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetById() {
        ResponseEntity<String> response = restTemplate.getForEntity("/observations/1", String.class);
        //language=json
        String expected = "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"id\": 1,\n" +
                "    \"strId\": \"Observation1\",\n" +
                "    \"dimensionMap\": {\n" +
                "      \"SUBTYPE_2\": {\n" +
                "        \"id\": 16,\n" +
                "        \"level\": 3,\n" +
                "        \"strId\": \"Dimension16Subtype2\",\n" +
                "        \"label\": \"LABEL_16\",\n" +
                "        \"dimensionType\": \"TYPE_2\",\n" +
                "        \"dimensionSubType\": \"SUBTYPE_2\",\n" +
                "        \"parentId\": 14,\n" +
                "        \"childrenIds\": [],\n" +
                "        \"allChildrenIds\": [],\n" +
                "        \"question\": \"\"\n" +
                "      },\n" +
                "      \"SUBTYPE_1\": {\n" +
                "        \"id\": 5,\n" +
                "        \"level\": 2,\n" +
                "        \"strId\": \"Dimension5Subtype1\",\n" +
                "        \"label\": \"LABEL_5\",\n" +
                "        \"dimensionType\": \"TYPE_1\",\n" +
                "        \"dimensionSubType\": \"SUBTYPE_1\",\n" +
                "        \"parentId\": 1,\n" +
                "        \"childrenIds\": [],\n" +
                "        \"allChildrenIds\": [],\n" +
                "        \"question\": \"\"\n" +
                "      },\n" +
                "      \"SUBTYPE_3\": {\n" +
                "        \"id\": 25,\n" +
                "        \"level\": 2,\n" +
                "        \"strId\": \"Dimension25Subtype3\",\n" +
                "        \"label\": \"LABEL_25\",\n" +
                "        \"dimensionType\": \"TYPE_3\",\n" +
                "        \"dimensionSubType\": \"SUBTYPE_3\",\n" +
                "        \"parentId\": 22,\n" +
                "        \"childrenIds\": [],\n" +
                "        \"allChildrenIds\": [],\n" +
                "        \"question\": \"\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

//    @Test
//    void testCreateUpdateDeleteObservation() {
//        Dimension dimension3 = Dimension.newBuilder()
//                .setId(25)
//                .setLevel(2)
//                .setStrId("Dimension25Subtype3")
//                .setLabel("LABEL_25")
//                .setDimensionType("TYPE_3")
//                .setDimensionSubType("SUBTYPE_3")
//                .setParentId(22L)
//                .setChildrenIds(Collections.emptySet())
//                .setAllChildrenIds(Collections.emptySet())
//                .setQuestion("")
//                .build();
//
//        Dimension dimension1 = Dimension.newBuilder()
//                .setId(5)
//                .setLevel(2)
//                .setStrId("Dimension5Subtype1")
//                .setLabel("LABEL_5")
//                .setDimensionType("TYPE_1")
//                .setDimensionSubType("SUBTYPE_1")
//                .setParentId(1L)
//                .setChildrenIds(Collections.emptySet())
//                .setAllChildrenIds(Collections.emptySet())
//                .setQuestion("")
//                .build();
//
//        Dimension dimension2 = Dimension.newBuilder()
//                .setId(17)
//                .setLevel(3)
//                .setStrId("Dimension16Subtype2")
//                .setLabel("LABEL_16")
//                .setDimensionType("TYPE_2")
//                .setDimensionSubType("SUBTYPE_2")
//                .setParentId(14L)
//                .setChildrenIds(Collections.emptySet())
//                .setAllChildrenIds(Collections.emptySet())
//                .setQuestion("")
//                .build();
//
//        Observation observation = new Observation(
//                100500,
//                "TEST_OBS",
//                Map.of("SUBTYPE_1", dimension1, "SUBTYPE_2", dimension2, "SUBTYPE_3", dimension3)
//        );
//        restTemplate.postForEntity("/observations", observation, String.class);
//
//        Observation update = new Observation(
//                100500,
//                "TEST_OBS_UPD",
//                Map.of("SUBTYPE_1", dimension1, "SUBTYPE_2", dimension2, "SUBTYPE_3", dimension3)
//        );
//        restTemplate.put("/observations/100500", update);
//        ResponseEntity<String> upd = restTemplate.getForEntity("/observations/100500", String.class);
//        System.out.println(upd.getBody());
//
//        restTemplate.delete("/observations/100500");
//    }
}
