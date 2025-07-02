package org.davidmoten.oa3.codegen.test.library.service;

import jakarta.annotation.Generated;

import java.lang.String;
import java.lang.Throwable;
import java.util.Optional;

import org.davidmoten.oa3.codegen.spring.runtime.ControllerExceptionHandler;
import org.davidmoten.oa3.codegen.spring.runtime.RequestPreconditions;
import org.davidmoten.oa3.codegen.test.library.Globals;
import org.davidmoten.oa3.codegen.test.library.schema.Item;
import org.davidmoten.oa3.codegen.test.library.schema.User;
import org.davidmoten.oa3.codegen.test.library.schema.UsersPage;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public class ServiceController implements ControllerExceptionHandler {

    private final Service service;

    public ServiceController(@Autowired(required = false) Service service) {
        this.service = Util.orElse(service, Service.DEFAULT);
    }

    /**
     * <p>List users page by page, filtered by search if present
     * 
     * <p>[status=200, application/json] --&gt; {@code UsersPage}
     * 
     * @param search
     *            <p>search
     * @param continuationToken
     *            <p>continuationToken
     * @return primary response status code 200
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/user",
        produces = {"application/json"})
    public ResponseEntity<?> getUsers(
            @RequestParam(name = "search", required = false) Optional<String> search, 
            @RequestParam(name = "continuationToken", required = false) Optional<String> continuationToken) {
        try {
            if (Globals.config().validateInControllerMethod().test("getUsers")) {
                RequestPreconditions.checkMinLength(search, 2, "search");
                RequestPreconditions.checkMinLength(continuationToken, 1, "continuationToken");
                RequestPreconditions.checkMaxLength(continuationToken, 1500, "continuationToken");
            }
            return ResponseEntity.status(200).body(service.getUsers(search, continuationToken));
        } catch (Throwable e) {
            return service.errorResponse(e);
        }
    }

    /**
     * <p>Creates a new user
     * 
     * @param requestBody
     *            <p>requestBody
     */
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/user",
        consumes = {"application/json"})
    public ResponseEntity<?> createUser(
            @RequestBody User requestBody) {
        try {
            service.createUser(requestBody);
            return ResponseEntity.status(200).build();
        } catch (Throwable e) {
            return service.errorResponse(e);
        }
    }

    /**
     * <p>Gets user details
     * 
     * <p>[status=200, application/json] --&gt; {@code User}
     * 
     * @param id
     *            <p>id
     * @return primary response status code 200
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/user/{id}",
        produces = {"application/json"})
    public ResponseEntity<?> getUser(
            @PathVariable(name = "id", required = true) String id) {
        try {
            if (Globals.config().validateInControllerMethod().test("getUser")) {
                RequestPreconditions.checkMinLength(id, 1, "id");
            }
            return ResponseEntity.status(200).body(service.getUser(id));
        } catch (Throwable e) {
            return service.errorResponse(e);
        }
    }

    /**
     * <p>Updates a user
     * 
     * @param requestBody
     *            <p>requestBody
     * @param id
     *            <p>id
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/user/{id}",
        consumes = {"application/json"})
    public ResponseEntity<?> updateUser(
            @PathVariable(name = "id", required = true) String id, 
            @RequestBody User requestBody) {
        try {
            if (Globals.config().validateInControllerMethod().test("updateUser")) {
                RequestPreconditions.checkMinLength(id, 1, "id");
            }
            service.updateUser(id, requestBody);
            return ResponseEntity.status(200).build();
        } catch (Throwable e) {
            return service.errorResponse(e);
        }
    }

    /**
     * <p>Deletes a user (logically)
     * 
     * @param id
     *            <p>id
     */
    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/user/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable(name = "id", required = true) String id) {
        try {
            if (Globals.config().validateInControllerMethod().test("deleteUser")) {
                RequestPreconditions.checkMinLength(id, 1, "id");
            }
            service.deleteUser(id);
            return ResponseEntity.status(200).build();
        } catch (Throwable e) {
            return service.errorResponse(e);
        }
    }

    /**
     * <p>Gets item details
     * 
     * <p>[status=200, application/json] --&gt; {@code Item}
     * 
     * @param itemId
     *            <p>itemId
     * @return primary response status code 200
     */
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/item/{itemId}",
        produces = {"application/json"})
    public ResponseEntity<?> getItem(
            @PathVariable(name = "itemId", required = true) String itemId) {
        try {
            if (Globals.config().validateInControllerMethod().test("getItem")) {
                RequestPreconditions.checkMinLength(itemId, 1, "itemId");
                RequestPreconditions.checkMaxLength(itemId, 255, "itemId");
            }
            return ResponseEntity.status(200).body(service.getItem(itemId));
        } catch (Throwable e) {
            return service.errorResponse(e);
        }
    }
}
