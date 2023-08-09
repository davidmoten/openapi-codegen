package org.davidmoten.oa3.codegen.test.library.service;

import jakarta.annotation.Generated;

import java.lang.String;
import java.util.Optional;

import org.davidmoten.oa3.codegen.spring.runtime.ErrorHandler;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.davidmoten.oa3.codegen.test.library.schema.Item;
import org.davidmoten.oa3.codegen.test.library.schema.User;
import org.davidmoten.oa3.codegen.test.library.schema.UsersPage;

/**
 * <p>Library Demo
 * <p>Library demonstration of some features of OpenAPI 3 and <em>openapi-codegen</em>
 */
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2-SNAPSHOT")
public interface Service extends ErrorHandler {

    /**
     * <p>List users page by page, filtered by search if present
     * 
     * <p>[status=200, application/json] --&gt; {@link UsersPage}
     * 
     * @param search
     *            search
     * @param continuationToken
     *            continuationToken
     * @return primary response status code 200
     */
    default UsersPage getUsers(
            Optional<String> search, 
            Optional<String> continuationToken) throws ServiceException {
        throw notImplemented();
    }

    /**
     * <p>Creates a new user
     * 
     * @param requestBody
     *            requestBody
     */
    default void createUser(
            User requestBody) throws ServiceException {
        throw notImplemented();
    }

    /**
     * <p>Gets user details
     * 
     * <p>[status=200, application/json] --&gt; {@link User}
     * 
     * @param id
     *            id
     * @return primary response status code 200
     */
    default User getUser(
            String id) throws ServiceException {
        throw notImplemented();
    }

    /**
     * <p>Updates a user
     * 
     * @param requestBody
     *            requestBody
     * @param id
     *            id
     */
    default void updateUser(
            String id, 
            User requestBody) throws ServiceException {
        throw notImplemented();
    }

    /**
     * <p>Deletes a user (logically)
     * 
     * @param id
     *            id
     */
    default void deleteUser(
            String id) throws ServiceException {
        throw notImplemented();
    }

    /**
     * <p>Gets item details
     * 
     * <p>[status=200, application/json] --&gt; {@link Item}
     * 
     * @param itemId
     *            itemId
     * @return primary response status code 200
     */
    default Item getItem(
            String itemId) throws ServiceException {
        throw notImplemented();
    }
}
