package org.davidmoten.oa3.codegen.test.library;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.davidmoten.oa3.codegen.test.library.schema.Item;
import org.davidmoten.oa3.codegen.test.library.schema.User;
import org.davidmoten.oa3.codegen.test.library.schema.UserId;
import org.davidmoten.oa3.codegen.test.library.schema.UserIdWrapped;
import org.davidmoten.oa3.codegen.test.library.schema.UserWithId;
import org.davidmoten.oa3.codegen.test.library.schema.UsersPage;
import org.davidmoten.oa3.codegen.test.library.schema.UsersPage.Users;
import org.davidmoten.oa3.codegen.test.library.service.Service;
import org.springframework.stereotype.Component;

@Component
public class LibraryService implements Service {

    @Override
    public UsersPage getUsers(Optional<String> search, Optional<String> continuationToken) throws ServiceException {
        Users users = new Users(IntStream.range(1, 21).mapToObj(i -> createUser(i)).collect(Collectors.toList()));
        return new UsersPage(users, Optional.empty());
    }

    private UserWithId createUser(int i) {
        return new UserWithId(User.builder() //
                .firstName("User" + i) //
                .lastName("Gomez") //
                .email("user" + i + ".gomez@gmail.com") //
                .build(), //
                UserIdWrapped.userId(UserId.value(i + "")));
    }

    @Override
    public void createUser(User requestBody) throws ServiceException {
        Service.super.createUser(requestBody);
    }

    @Override
    public User getUser(String id) throws ServiceException {
        return Service.super.getUser(id);
    }

    @Override
    public void updateUser(String id, User requestBody) throws ServiceException {
        Service.super.updateUser(id, requestBody);
    }

    @Override
    public void deleteUser(String id) throws ServiceException {
        Service.super.deleteUser(id);
    }

    @Override
    public Item getItem(String itemId) throws ServiceException {
        return Service.super.getItem(itemId);
    }

}
