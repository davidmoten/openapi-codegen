package pet.store.generated;

import org.davidmoten.oa3.codegen.spring.runtime.ErrorHandler;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;

import pet.store.path.PetsGet200Response;

public interface Service extends ErrorHandler {

    default PetsGet200Response pets(int limit) throws ServiceException {
        throw new ServiceException(501, "Not implemented");
    };

}
