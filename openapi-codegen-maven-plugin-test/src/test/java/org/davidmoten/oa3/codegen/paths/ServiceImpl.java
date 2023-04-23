package org.davidmoten.oa3.codegen.paths;

import org.davidmoten.oa3.codegen.paths.path.QueryObjectGetIdParameterId;
import org.davidmoten.oa3.codegen.paths.schema.Response2;
import org.davidmoten.oa3.codegen.paths.service.Service;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ServiceImpl implements Service {

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
        throw new ServiceException(
                ResponseEntity.status(500).body(new org.davidmoten.oa3.codegen.paths.schema.Response1("beehive")));
    }

    @Override
    public void queryObjectGet(QueryObjectGetIdParameterId id) throws ServiceException {
        System.out.println(id.first() + ", " + id.second().get());
    }

}
