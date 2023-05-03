package org.davidmoten.oa3.codegen.library;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.davidmoten.oa3.codegen.library.schema.Item;
import org.davidmoten.oa3.codegen.library.schema.User;
import org.davidmoten.oa3.codegen.library.schema.UserId;
import org.davidmoten.oa3.codegen.library.schema.UserIdWrapped;
import org.davidmoten.oa3.codegen.library.schema.UserWithId;
import org.davidmoten.oa3.codegen.library.schema.UsersPage;
import org.davidmoten.oa3.codegen.library.schema.UsersPage.Users;
import org.davidmoten.oa3.codegen.library.service.Service;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.springframework.stereotype.Component;

@Component
public class LibraryService implements Service {

    @Override
    public UsersPage userGet(Optional<String> search, Optional<String> continuationToken) throws ServiceException {
        Users users = new Users(IntStream.range(1, 21).mapToObj(i -> createUser(i)).collect(Collectors.toList()));
        return new UsersPage(users, Optional.empty());
    }

    private UserWithId createUser(int i) {
        return new UserWithId(new User("User" + i, "Gomez", "user" + i + ".gomez@gmail.com", Optional.empty()),
                new UserIdWrapped(new UserId(i + "")));
    }

    @Override
    public void userPost(User requestBody) throws ServiceException {
        Service.super.userPost(requestBody);
    }

    @Override
    public User userIdGet(String id) throws ServiceException {
        return Service.super.userIdGet(id);
    }

    @Override
    public void userIdPut(String id, User requestBody) throws ServiceException {
        Service.super.userIdPut(id, requestBody);
    }

    @Override
    public void userIdDelete(String id) throws ServiceException {
        Service.super.userIdDelete(id);
    }

    @Override
    public Item itemItemIdGet(String itemId) throws ServiceException {
        return Service.super.itemItemIdGet(itemId);
    }

}
