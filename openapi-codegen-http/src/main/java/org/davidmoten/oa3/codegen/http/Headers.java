package org.davidmoten.oa3.codegen.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Stores a map of header keys and values. Keys are case-insensitive, iterating
 * the key values will return the latest key value conserved in submitted case.
 */
public final class Headers {

    private final Map<String, KeyAndList> map;

    public Headers() {
        this.map = new HashMap<>();
    }

    public Headers(Headers headers) {
        this.map = new HashMap<>(headers.map);
    }

    public static Headers create() {
        return new Headers();
    }

    public Optional<List<String>> get(String key) {
        return Optional.ofNullable(map.get(key.toUpperCase(Locale.ENGLISH))).map(x -> x.list);
    }

    public Headers put(String key, String value) {
        Preconditions.checkArgumentNotNull(key);
        Preconditions.checkArgumentNotNull(value);
        if (key.equalsIgnoreCase("Content-Type") && get(key).isPresent()) {
            // replace
            get(key).get().clear();
        }
        Optional<List<String>> v = get(key);
        if (!v.isPresent()) {
            KeyAndList x = new KeyAndList(key);
            map.put(key.toUpperCase(Locale.ENGLISH), x);
            v = Optional.of(x.list);
        }
        if (!v.get().contains(value)) {
            v.get().add(value);
        }
        return this;
    }

    public void putAll(Headers headers) {
        headers.forEach((key, list) -> list.forEach(v -> put(key, v)));
    }

    public Headers remove(String key) {
        map.remove(key.toUpperCase(Locale.ENGLISH));
        return this;
    }

    public boolean contains(String name, String value) {
        Preconditions.checkArgumentNotNull(name);
        Preconditions.checkArgumentNotNull(value);
        return get(name).map(list -> list.contains(value)).orElse(false);
    }

    public static Headers create(Map<String, List<String>> headers) {
        Headers h = create();
        headers.forEach((key, list) -> {
            if (key != null && list != null)
                list.forEach(v -> h.put(key, v));
        });
        return h;
    }

    public List<String> keys() {
        return map.values().stream().map(x -> x.key).collect(Collectors.toList());
    }

    public void forEach(BiConsumer<? super String, ? super List<String>> consumer) {
        map.values().forEach(v -> consumer.accept(v.key, v.list));
    }

    private static final class KeyAndList {
        final String key;
        final List<String> list;

        KeyAndList(String key, List<String> list) {
            this.key = key;
            this.list = list;
        }

        KeyAndList(String key) {
            this(key, new ArrayList<>());
        }

    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Headers [");
        b.append(map.values().stream().map(x -> x.key + " -> " + x.list).collect(Collectors.joining(", ")));
        b.append("]");
        return b.toString();
    }

}
