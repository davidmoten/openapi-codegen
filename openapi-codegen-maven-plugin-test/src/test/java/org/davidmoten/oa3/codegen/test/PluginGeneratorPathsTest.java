package org.davidmoten.oa3.codegen.test;

import java.util.Optional;

import org.davidmoten.oa3.codegen.paths.generated.schema.RequestBody1;
import org.davidmoten.oa3.codegen.paths.generated.schema.RequestBody2;
import org.davidmoten.oa3.codegen.paths.generated.schema.Response1;
import org.davidmoten.oa3.codegen.paths.generated.schema.Response2;
import org.davidmoten.oa3.codegen.paths.generated.service.Service;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.junit.Test;
import org.mockito.Mockito;

@SuppressWarnings("unused")
public class PluginGeneratorPathsTest {

    private static final Service service = Mockito.mock(Service.class);

    @Test
    public void testRequestBodyRequired() throws ServiceException {
        RequestBody1 r = new RequestBody1("fred");
        Response1 response = service.requestBodyRequiredPost(r);
    }

    @Test
    public void testRequestBodyNotRequired() throws ServiceException {
        RequestBody2 r = new RequestBody2("fred");
        Response2 response = service.requestBodyNotRequiredPost(Optional.of(r));
    }

    @Test
    public void testGetReturns200() throws ServiceException {
        Response2 response = service.itemGet();
    }
    
    @Test
    public void testGetReturns201() throws ServiceException {
        Response2 response = service.item201Get();
    }

}
