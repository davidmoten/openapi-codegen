package org.davidmoten.oa3.codegen.test;

import java.util.Optional;

import org.davidmoten.oa3.codegen.paths.generated.path.RequestBodyNotRequiredPostRequest;
import org.davidmoten.oa3.codegen.paths.generated.path.RequestBodyRequiredPostRequest;
import org.davidmoten.oa3.codegen.paths.generated.schema.RequestBody1;
import org.davidmoten.oa3.codegen.paths.generated.schema.RequestBody2;
import org.davidmoten.oa3.codegen.paths.generated.schema.Response1;
import org.davidmoten.oa3.codegen.paths.generated.schema.Response2;
import org.davidmoten.oa3.codegen.paths.generated.service.Service;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.junit.Test;
import org.mockito.Mockito;

public class PluginGeneratorPathsTest {
    
    private static final Service service = Mockito.mock(Service.class);
    
    @Test
    public void testRequestBodyRequired() throws ServiceException {
        RequestBodyRequiredPostRequest r = new RequestBodyRequiredPostRequest(new RequestBody1("fred"));
        @SuppressWarnings("unused")
        Response1 response = service.requestBodyRequiredPost(r);
    }
    
    @Test
    public void testRequestBodyNotRequired() throws ServiceException {
        RequestBodyNotRequiredPostRequest r = new RequestBodyNotRequiredPostRequest(new RequestBody2("fred"));
        @SuppressWarnings("unused")
        Response2 response = service.requestBodyNotRequiredPost(Optional.of(r));
    }
    
}
