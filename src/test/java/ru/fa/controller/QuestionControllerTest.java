package ru.fa.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import ru.fa.FunctionalTest;
import ru.fa.JsonAsserts;
import ru.fa.dto.QuestionRequest;

import java.util.Map;

class QuestionControllerTest extends FunctionalTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testExact() {
        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension4Subtype1",
                        "SUBTYPE_2", "Dimension16Subtype2",
                        "SUBTYPE_3", "Dimension25Subtype3"
                )
        );
        ResponseEntity<String> response = restTemplate.postForEntity("/question", questionRequest, String.class);

        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"str_id\": \"Value0\",\n" +
                "    \"content\": {\n" +
                "      \"id\": 0,\n" +
                "      \"body\": \"superBody\"\n" +
                "    },\n" +
                "    \"value_subtype\": \"VALUE_SUBTYPE_1\",\n" +
                "    \"response_type\": \"ANSWER\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

    @Test
    void testInputUpDifBranches() {
        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension1Subtype1",
                        "SUBTYPE_2", "Dimension16Subtype2",
                        "SUBTYPE_3", "Dimension25Subtype3"
                )
        );
        ResponseEntity<String> response = restTemplate.postForEntity("/question", questionRequest, String.class);

        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"question\": \"4, 5?\",\n" +
                "    \"dimension_subtype\": \"SUBTYPE_1\",\n" +
                "    \"response_type\": \"QUESTION\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());

        questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension4Subtype1",
                        "SUBTYPE_2", "Dimension16Subtype2",
                        "SUBTYPE_3", "Dimension25Subtype3"
                )
        );

        response = restTemplate.postForEntity("/question", questionRequest, String.class);
        //language=json
        expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"str_id\": \"Value0\",\n" +
                "    \"content\": {\n" +
                "      \"id\": 0,\n" +
                "      \"body\": \"superBody\"\n" +
                "    },\n" +
                "    \"value_subtype\": \"VALUE_SUBTYPE_1\",\n" +
                "    \"response_type\": \"ANSWER\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

    @Test
    void testInputUpOneBranch() {
        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension5Subtype1",
                        "SUBTYPE_2", "Dimension12Subtype2",
                        "SUBTYPE_3", "Dimension21Subtype3"
                )
        );
        ResponseEntity<String> response = restTemplate.postForEntity("/question", questionRequest, String.class);

        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"question\": \"14, 15?\",\n" +
                "    \"dimension_subtype\": \"SUBTYPE_2\",\n" +
                "    \"response_type\": \"QUESTION\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());

        questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension5Subtype1",
                        "SUBTYPE_2", "Dimension15Subtype2",
                        "SUBTYPE_3", "Dimension21Subtype3"
                )
        );

        response = restTemplate.postForEntity("/question", questionRequest, String.class);
        //language=json
        expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"str_id\": \"Value3\",\n" +
                "    \"content\": {\n" +
                "      \"id\": 3,\n" +
                "      \"body\": \"superBody\"\n" +
                "    },\n" +
                "    \"value_subtype\": \"VALUE_SUBTYPE_1\",\n" +
                "    \"response_type\": \"ANSWER\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

    @Test
    void testInputInOneBranch() {
        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension5Subtype1",
                        "SUBTYPE_2", "Dimension14Subtype2",
                        "SUBTYPE_3", "Dimension21Subtype3"
                )
        );
        ResponseEntity<String> response = restTemplate.postForEntity("/question", questionRequest, String.class);

        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"question\": \"16, 17?\",\n" +
                "    \"dimension_subtype\": \"SUBTYPE_2\",\n" +
                "    \"response_type\": \"QUESTION\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

    @Test
    void testInputBetween() {
        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension5Subtype1",
                        "SUBTYPE_2", "Dimension14Subtype2",
                        "SUBTYPE_3", "Dimension25Subtype3"
                )
        );
        ResponseEntity<String> response = restTemplate.postForEntity("/question", questionRequest, String.class);

        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"question\": \"16, 17?\",\n" +
                "    \"dimension_subtype\": \"SUBTYPE_2\",\n" +
                "    \"response_type\": \"QUESTION\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());

        questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension5Subtype1",
                        "SUBTYPE_2", "Dimension16Subtype2",
                        "SUBTYPE_3", "Dimension25Subtype3"
                )
        );

        response = restTemplate.postForEntity("/question", questionRequest, String.class);
        //language=json
        expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"str_id\": \"Value1\",\n" +
                "    \"content\": {\n" +
                "      \"id\": 1,\n" +
                "      \"body\": \"superBody\"\n" +
                "    },\n" +
                "    \"value_subtype\": \"VALUE_SUBTYPE_1\",\n" +
                "    \"response_type\": \"ANSWER\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());

        questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension5Subtype1",
                        "SUBTYPE_2", "Dimension17Subtype2",
                        "SUBTYPE_3", "Dimension25Subtype3"
                )
        );

        response = restTemplate.postForEntity("/question", questionRequest, String.class);
        //language=json
        expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"str_id\": \"Value2\",\n" +
                "    \"content\": {\n" +
                "      \"id\": 2,\n" +
                "      \"body\": \"superBody\"\n" +
                "    },\n" +
                "    \"value_subtype\": \"VALUE_SUBTYPE_1\",\n" +
                "    \"response_type\": \"ANSWER\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }

    @Test
    void testNoClarify() {
        QuestionRequest questionRequest = new QuestionRequest(
                "VALUE_SUBTYPE_1",
                Map.of(
                        "SUBTYPE_1", "Dimension1Subtype1",
                        "SUBTYPE_2", "Dimension17Subtype2",
                        "SUBTYPE_3", "Dimension26Subtype3"
                )
        );
        ResponseEntity<String> response = restTemplate.postForEntity("/question", questionRequest, String.class);

        //language=json
        String expected = "" +
                "{\n" +
                "  \"status\": \"OK\",\n" +
                "  \"result\": {\n" +
                "    \"str_id\": \"Value2\",\n" +
                "    \"content\": {\n" +
                "      \"id\": 2,\n" +
                "      \"body\": \"superBody\"\n" +
                "    },\n" +
                "    \"value_subtype\": \"VALUE_SUBTYPE_1\",\n" +
                "    \"response_type\": \"ANSWER\"\n" +
                "  }\n" +
                "}";
        JsonAsserts.assertJsonEquals(expected, response.getBody());
    }
}
