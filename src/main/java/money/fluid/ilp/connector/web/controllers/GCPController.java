package money.fluid.ilp.connector.web.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A controller for Google Cloud Platform's compute instance  start/stop health checks.
 */
@Controller
@RequestMapping("/")
public class GCPController {

    /**
     * Get the Root resource for the API.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getRoot() {
        return new ResponseEntity(HttpStatus.OK);
    }


    /**
     * Get the "/_ah/start" resource for warmup requests...
     */
    @RequestMapping(method = RequestMethod.GET, path = "/_ah/start")
    public ResponseEntity getStart() {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Get the "/_ah/stop" resource for shutdown requests...
     */
    @RequestMapping(method = RequestMethod.GET, path = "/_ah/stop")
    public ResponseEntity getStop() {
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
