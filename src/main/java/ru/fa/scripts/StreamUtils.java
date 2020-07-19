package ru.fa.scripts;

import com.google.common.collect.ArrayListMultimap;

import java.util.function.Function;
import java.util.stream.Collector;

public class StreamUtils {

    private StreamUtils() {
        //Utility Class
        throw new UnsupportedOperationException();
    }

    public static <K, V> Collector<V, ArrayListMultimap<K, V>, ArrayListMultimap<K, V>> toArrayMultimap(
            Function<V, K> keyGetter
    ) {
        return Collector.of(
                ArrayListMultimap::create,
                (map, element) -> map.put(keyGetter.apply(element), element),
                (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                }
        );
    }

    public static <E, K, V> Collector<E, ArrayListMultimap<K, V>, ArrayListMultimap<K, V>> toArrayMultimap(
            Function<E, K> keyGetter,
            Function<E, V> valueGetter
    ) {
        return Collector.of(
                ArrayListMultimap::create,
                (map, element) -> map.put(keyGetter.apply(element), valueGetter.apply(element)),
                (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                }
        );
    }
}
