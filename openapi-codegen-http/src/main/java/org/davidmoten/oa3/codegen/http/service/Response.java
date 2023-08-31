package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Response {
    
    int statusCode();
    
    InputStream inputStream() throws IOException;
    
    Map<String, List<String>> headers();

}
