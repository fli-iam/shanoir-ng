package org.shanoir.ng.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Home redirection to swagger api documentation
 * 
 * @author jlouis
 */
@Controller
public class SwaggerController {

	@RequestMapping(value = "/")
	public String index() {
		return "redirect:swagger-ui.html";
	}

}
