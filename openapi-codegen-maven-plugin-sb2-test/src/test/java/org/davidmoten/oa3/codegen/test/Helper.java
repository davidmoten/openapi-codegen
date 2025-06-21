package org.davidmoten.oa3.codegen.test;

import java.util.List;

import org.davidmoten.oa3.codegen.http.service.ApacheHttpClientHttpService;
import org.davidmoten.oa3.codegen.http.service.DefaultHttpService;
import org.davidmoten.oa3.codegen.http.service.HttpService;

import com.github.davidmoten.guavamini.Lists;

public final class Helper {

    private Helper() {
        // prevent instantiation
    }
    
    public static List<HttpService> httpServices() {
        return Lists.of(DefaultHttpService.INSTANCE, ApacheHttpClientHttpService.INSTANCE);
    }
}
