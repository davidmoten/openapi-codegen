package pet.store;

import java.util.Collections;

import pet.store.model.Path_pets_Get_200_Response;

public class ServiceImpl implements Service {

    @Override
    public Path_pets_Get_200_Response pets(int limit) {
        return new Path_pets_Get_200_Response(Collections.emptyList());
    }

}
