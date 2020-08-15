package ru.fa;

import org.hamcrest.MatcherAssert;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonAsserts {
    /**
     * Сравнить два JSON'а в виде строк без учета отступов
     * @param expected ожидаемый результат
     * @param actual тестируемый результат
     */
    public static void assertJsonEquals(String expected, String actual) {
        MatcherAssert.assertThat(actual, new JsonEqualsMatcher(expected));
    }

    /**
     * Сравнить два JSON'а в виде строк без учета отступов
     * @param expected ожидаемый результат
     * @param actual тестируемый результат
     * @param ignoredAttributes набор игнорируемых атрибутов
     */
    public static void assertJsonEquals(String expected, String actual, Set<String> ignoredAttributes) {
        MatcherAssert.assertThat(actual, new JsonEqualsMatcher(expected, ignoredAttributes));
    }

    /**
     * Сравнить два JSON'а в виде строк без учета отступов
     * @param expected ожидаемый результат
     * @param actual тестируемый результат
     * @param customComparator кастомный компаратор для сравнения JSON'ов
     */
    public static void assertJsonEquals(String expected, String actual, CustomComparator customComparator) {
        MatcherAssert.assertThat(actual, new JsonEqualsMatcher(expected, customComparator));
    }

    public static void assertHeader(ResponseEntity entity, String header, Object value) {
        List<String> values = entity.getHeaders().get(header);
        assertEquals(1, values.size());
        assertEquals(String.valueOf(value), values.get(0));
    }
}
