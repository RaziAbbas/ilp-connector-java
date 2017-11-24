package money.fluid.ilp.connector.web.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * A spring controllers for serving the /health resource.
 */
@RestController
public class HealthRestController {

	@RequestMapping(path = "/health", method = RequestMethod.GET)
	public String get() {
		return "I am working !!!";
		//return new HealthRepresentation(true);
	}
}
