package ru.fa.controller;

import com.fasterxml.jackson.databind.node.IntNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.fa.FunctionalTest;
import ru.fa.JsonAsserts;
import ru.fa.model.Value;

class ValueControllerTest extends FunctionalTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testGetById() {
        ResponseEntity<String> response = restTemplate.getForEntity("/values/1", String.class);
        //language=json
        String expected = "" +
                "{\n" +
                "  \"result\": {\n" +
                "    \"id\": 1,\n" +
                "    \"strId\": \"Value1\",\n" +
                "    \"content\": {\n" +
                "      \"id\": 1,\n" +
                "      \"body\": \"superBody\"\n" +
                "    },\n" +
                "    \"type\": \"VALUE_TYPE_1\"\n" +
                "  },\n" +
                "  \"status\": \"OK\"\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

    @Test
    void testGetAll() {
        ResponseEntity<String> response = restTemplate.getForEntity("/values", String.class);
        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"strId\": \"Value0\",\n" +
                "      \"content\": {\n" +
                "        \"id\": 0,\n" +
                "        \"body\": \"superBody\"\n" +
                "      },\n" +
                "      \"type\": \"VALUE_TYPE_1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"strId\": \"Value1\",\n" +
                "      \"content\": {\n" +
                "        \"id\": 1,\n" +
                "        \"body\": \"superBody\"\n" +
                "      },\n" +
                "      \"type\": \"VALUE_TYPE_1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"strId\": \"Value2\",\n" +
                "      \"content\": {\n" +
                "        \"id\": 2,\n" +
                "        \"body\": \"superBody\"\n" +
                "      },\n" +
                "      \"type\": \"VALUE_TYPE_1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 3,\n" +
                "      \"strId\": \"Value3\",\n" +
                "      \"content\": {\n" +
                "        \"id\": 3,\n" +
                "        \"body\": \"superBody\"\n" +
                "      },\n" +
                "      \"type\": \"VALUE_TYPE_1\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 4,\n" +
                "      \"strId\": \"Value4\",\n" +
                "      \"content\": {\n" +
                "        \"id\": 4,\n" +
                "        \"body\": \"superBody\"\n" +
                "      },\n" +
                "      \"type\": \"VALUE_TYPE_1\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

    @Test
    void testCreateUpdateDelete() {
        Value value = new Value(100500, "100500_test", new IntNode(100500), "TEST_VALUE");
        restTemplate.postForEntity("/values", value, String.class);

        Value update = new Value(100500, "1_test_upd", new IntNode(100500), "TEST_VALUE_UPD");
        restTemplate.put("/values/100500", update);

        ResponseEntity<String> response = restTemplate.getForEntity("/values/100500", String.class);
        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"id\": 100500,\n" +
                "    \"strId\": \"1_test_upd\",\n" +
                "    \"content\": 100500,\n" +
                "    \"type\": \"TEST_VALUE_UPD\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());

        restTemplate.delete("/values/100500");
    }

    @Test
    void testNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/values/100500", String.class);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        //language=json
        String expected = "{\"status\":\"ERROR\",\"result\":\"No values with id: 100500\"}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }
}
