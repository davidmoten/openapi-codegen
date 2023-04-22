package org.davidmoten.oa3.codegen.paths;

import org.davidmoten.oa3.codegen.paths.schema.Response2;
import org.davidmoten.oa3.codegen.paths.service.Service;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class ServiceImpl implements Service {

    @Override
    public Response2 itemGet() throws ServiceException {
        return new Response2("abcToken");
    }

}
