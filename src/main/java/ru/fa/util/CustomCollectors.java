package ru.fa.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.function.Function;
import java.util.stream.Collector;

public class CustomCollectors {

    private CustomCollectors() {
        throw new UnsupportedOperationException();
    }

    public static <K, V> Collector<V, Multimap<K, V>, Multimap<K, V>> toLinkedHashMultimap(Function<V, K> keyGetter) {
        return Collector.of(
                LinkedHashMultimap::create,
                (map, element) -> map.put(keyGetter.apply(element), element),
                (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                }
        );
    }

    public static <T, K, V> Collector<T, Multimap<K, V>, Multimap<K, V>> toLinkedHashMultimap(
            Function<T, K> keyGetter,
            Function<T, V> valueGetter
    ) {
        return Collector.of(
                LinkedHashMultimap::create,
                (map, element) -> map.put(keyGetter.apply(element), valueGetter.apply(element)),
                (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                }
        );
    }
}
