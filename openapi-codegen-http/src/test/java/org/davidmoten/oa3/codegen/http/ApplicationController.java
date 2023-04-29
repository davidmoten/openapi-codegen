package org.davidmoten.oa3.codegen.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

    @RequestMapping(method = RequestMethod.GET, value = "/thing", produces = { "application/json" })
    public ResponseEntity<?> thingGet(@RequestParam(name = "id", required = true) String id) {
        if (id.equals("a")) {
            return ResponseEntity.ok(new Thing("janice", 34));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Problem("bad id", HttpStatus.BAD_REQUEST.value()));
        }
    }
}
