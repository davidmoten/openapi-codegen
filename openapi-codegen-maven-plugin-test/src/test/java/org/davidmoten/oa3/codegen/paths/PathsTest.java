package org.davidmoten.oa3.codegen.paths;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.davidmoten.oa3.codegen.paths.response.Response4;
import org.davidmoten.oa3.codegen.paths.schema.RequestBody1;
import org.davidmoten.oa3.codegen.paths.schema.RequestBody2;
import org.davidmoten.oa3.codegen.paths.schema.Response1;
import org.davidmoten.oa3.codegen.paths.schema.Response2;
import org.davidmoten.oa3.codegen.paths.service.Service;
import org.davidmoten.oa3.codegen.paths.service.ServiceController;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@SuppressWarnings("unused")
public class PathsTest {

    private static final Service service = Mockito.mock(Service.class);

    @Test
    public void testRequestBodyRequired() throws ServiceException {
        RequestBody1 r = new RequestBody1("fred");
        Response1 response = service.requestBodyRequiredPost(r);
        hasParameterAnnotation(ServiceController.class, RequestBody.class, "requestBodyRequiredPost", 0,
                RequestBody1.class);
    }

    @Test
    public void testRequestBodyNotRequired() throws ServiceException {
        RequestBody2 r = new RequestBody2("fred");
        Response2 response = service.requestBodyNotRequiredPost(Optional.of(r));
    }

    @Test
    public void testResponseMultiType() throws ServiceException {
        RequestBody2 r = new RequestBody2("fred");
        Response1 response = service.responseMultiTypeGet(MediaType.APPLICATION_JSON_VALUE, "miyaki");
    }

    @Test
    public void testGetReturns200() throws ServiceException {
        Response2 response = service.itemGet();
    }

    @Test
    public void testGetReturns201() throws ServiceException {
        Response2 response = service.item201Get();
    }

    @Test
    public void testResponseRef() throws ServiceException {
        Response2 response = service.responseRefGet();
    }

    @Test
    public void testResponseRef2() throws ServiceException {
        Response4 response = service.responseRef2Get();
    }

    @Test
    public void testGetEmpty() throws ServiceException {
        service.emptyGet();
        isVoid("emptyGet");
    }

    @Test
    public void testParams() throws ServiceException {
        Response2 response = service.paramsGet("123abc", OffsetDateTime.now(), Optional.of(123L), 45);
        hasSignature(Service.class, "paramsGet", String.class, OffsetDateTime.class, Optional.class, int.class);
    }

    @Test
    public void testParamsResponseStatusCode() throws ServiceException {
        Service svc = new Service() {
            @Override
            public Response2 paramsGet(String id, OffsetDateTime first, Optional<Long> second, int third)
                    throws ServiceException {
                return new Response2("token123");
            }
        };
        ServiceController c = new ServiceController(svc);
        ResponseEntity<?> r = c.paramsGet("123abc", OffsetDateTime.now(), Optional.of(123L), 45);
        assertEquals(203, r.getStatusCodeValue());
    }

    @Test
    public void testPathVariableAndRequestHeaderAndCookie() throws ServiceException {
        service.paramsIdGet("abc", "apikey", "timtam");
        hasParameterAnnotation(ServiceController.class, PathVariable.class, "paramsIdGet", 0, String.class,
                String.class, String.class);
        hasParameterAnnotation(ServiceController.class, RequestHeader.class, "paramsIdGet", 1, String.class,
                String.class, String.class);
        hasParameterAnnotation(ServiceController.class, CookieValue.class, "paramsIdGet", 2, String.class, String.class,
                String.class);
    }

    @Test
    public void testGetOctetStream() throws ServiceException {
        Resource response = service.bytesGet();
    }

    @Test
    public void testGetText() throws ServiceException {
        String response = service.textGet();
    }

    private static void hasParameterAnnotation(Class<?> c, Class<? extends Annotation> annotation, String methodName,
            int argNo, Class<?>... args) {
        try {
            Method m = c.getMethod(methodName, args);
            assertTrue(Arrays.stream(m.getParameters()).anyMatch(p -> p.isAnnotationPresent(annotation)));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static void isVoid(String methodName) {
        Method m;
        try {
            m = Service.class.getMethod(methodName);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        assertTrue(m.getReturnType() == Void.TYPE);
    }

    private static void hasSignature(Class<?> c, String methodName, Class<?>... classes) {
        try {
            c.getMethod(methodName, classes);
        } catch (NoSuchMethodException | SecurityException e) {
            fail(e.getMessage());
        }
    }

}
