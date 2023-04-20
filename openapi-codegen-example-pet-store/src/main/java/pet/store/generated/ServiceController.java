package pet.store.generated;

import org.davidmoten.oa3.codegen.runtime.internal.Preconditions;
import org.davidmoten.oa3.codegen.runtime.internal.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

    private final Service service;
    
    public ServiceController(@Autowired(required = false) Service service) {
        this.service = Util.orElse(service, new Service() {});
    }

    @RequestMapping(method = RequestMethod.GET, value = "/pets", produces = { "application/json" })
    public ResponseEntity<?> pets(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        try {
            Preconditions.checkMinimum(limit, "1", "limit", false);
            Preconditions.checkMaximum(limit, "10", "limit", false);
            return ResponseEntity.ok(service.pets(limit));
        } catch (Throwable e) {
            return service.errorResponse(e);
        }
    }

}
