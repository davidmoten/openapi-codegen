package pet.store.generated;

import pet.store.model.Path_pets_Get_200_Response;
import pet.store.runtime.ErrorHandler;
import pet.store.runtime.ServiceException;

public interface Service extends ErrorHandler {

    Path_pets_Get_200_Response pets(int limit) throws ServiceException;

}
