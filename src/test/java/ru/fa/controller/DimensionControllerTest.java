package ru.fa.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import ru.fa.FunctionalTest;
import ru.fa.JsonAsserts;

class DimensionControllerTest extends FunctionalTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetById() {
        ResponseEntity<String> response = restTemplate.getForEntity("/dimensions/1", String.class);
        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"id\": 1,\n" +
                "    \"level\": 1,\n" +
                "    \"strId\": \"Dimension1Subtype1\",\n" +
                "    \"label\": \"LABEL_1\",\n" +
                "    \"dimensionType\": \"TYPE_1\",\n" +
                "    \"dimensionSubType\": \"SUBTYPE_1\",\n" +
                "    \"parentId\": 0,\n" +
                "    \"childrenIds\": [4,5],\n" +
                "    \"allChildrenIds\": [4,5],\n" +
                "    \"question\": \"4, 5?\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }
}
