package pet.store;

import org.davidmoten.oa3.codegen.runtime.internal.Preconditions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pet.store.model.Error;

@RestController
public class ApplicationController {

    Service service = new ServiceImpl();

    @RequestMapping(method = RequestMethod.GET, value = "/pets", produces = { "application/json" })
    public ResponseEntity<?> pets(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        try {
            Preconditions.checkMinimum(limit, "1", "limit", false);
            Preconditions.checkMaximum(limit, "10", "limit", false);
            return ResponseEntity.ok(service.pets(limit));
        } catch (ServiceException e) {
            return ResponseEntity.status(e.statusCode()).body(new Error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new Error(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(501).body(new Error(e.getMessage()));
        }
    }
    
}
