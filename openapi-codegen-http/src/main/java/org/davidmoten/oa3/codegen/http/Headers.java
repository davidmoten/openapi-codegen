package org.davidmoten.oa3.codegen.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Headers extends HashMap<String, List<String>> {

    private static final long serialVersionUID = 4670201310662941557L;

    public Headers() {
        super();
    }

    public Headers(Headers requestHeaders) {
        super(requestHeaders);
    }

    public Headers put(String key, String value) {
        if (key.equalsIgnoreCase("Content-Type") && get(key) != null) {
            // replace
            get(key).clear();
        }
        List<String> list = get(key);
        if (list == null) {
            list = new ArrayList<>();
            put(key, list);
        }
        if (!list.contains(value)) {
            list.add(value);
        }
        return this;
    }

    public static Headers create() {
        return new Headers();
    }

}
