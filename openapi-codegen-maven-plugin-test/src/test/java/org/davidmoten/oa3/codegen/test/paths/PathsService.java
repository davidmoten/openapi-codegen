package org.davidmoten.oa3.codegen.test.paths;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.davidmoten.oa3.codegen.test.paths.path.QueryObjectGetIdParameterId;
import org.davidmoten.oa3.codegen.test.paths.schema.Error_;
import org.davidmoten.oa3.codegen.test.paths.schema.Name;
import org.davidmoten.oa3.codegen.test.paths.schema.Point;
import org.davidmoten.oa3.codegen.test.paths.schema.Response1;
import org.davidmoten.oa3.codegen.test.paths.schema.Response2;
import org.davidmoten.oa3.codegen.test.paths.service.Service;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PathsService implements Service {

    @Override
    public Response2 itemGet() throws ServiceException {
        return new Response2("abcToken");
    }

    @Override
    public Response2 item201Get() throws ServiceException {
        throw new ServiceException(500, "todo sale mal");
    }

    @Override
    public Response2 responseRefGet() throws ServiceException {
        return response(ResponseEntity.status(500).body(Response1.thing("beehive")));
    }

    @Override
    public void queryObjectGet(QueryObjectGetIdParameterId id) throws ServiceException {
        System.out.println(id.first() + ", " + id.second().get());
    }

    @Override
    public void pointsGet(Point a, Point b) throws ServiceException {
        System.out.println(a.lat() + ", " + a.lon());
        System.out.println(b.lat() + ", " + b.lon());
    }

    @Override
    public Response1 responseMultiTypeGet(String accept, String username) throws ServiceException {
        List<String> accepts = Arrays.asList(accept.split(", "));
        if (accepts.stream().anyMatch(x -> MediaType.valueOf(x).isCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))) {
            byte[] bytes = "hello there".getBytes(StandardCharsets.UTF_8);
            InputStream in = new ByteArrayInputStream(bytes);
            InputStreamResource res = new InputStreamResource(in);
            HttpHeaders headers = new HttpHeaders();
            headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_OCTET_STREAM_VALUE));
            headers.put(HttpHeaders.CONTENT_LENGTH, Arrays.asList(bytes.length + ""));
            return response(new ResponseEntity<>(res, headers, HttpStatus.OK));
        } else if (accepts.stream().anyMatch(x -> MediaType.valueOf(x).isCompatibleWith(MediaType.APPLICATION_JSON))) {
            return new Response1(username);
        } else {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "unsupported Accept header: " + accept);
        }
    }

    @Override
    public Resource bytesGet() throws ServiceException {
        return new InputStreamResource(new ByteArrayInputStream("hello there".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public String textGet() throws ServiceException {
        return "example text";
    }

    @Override
    public Response1 defaultErrorGet() throws ServiceException {
        return response(
                ResponseEntity.status(404).body(Error_.builder().statusCode(404L).errorMessage("not found eh").build()));
    }

    @Override
    public Name jsonStringGet() throws ServiceException {
        return Name.value("hello");
    }

}
