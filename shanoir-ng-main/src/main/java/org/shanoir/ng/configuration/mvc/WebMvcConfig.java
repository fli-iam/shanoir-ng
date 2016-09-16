package org.shanoir.ng.configuration.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Web MVC configuration.
 * 
 * @author msimon
 *
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

	/**
	 * Maps all AngularJS routes to index so that they work with direct linking.
	 */
	@Controller
	static class Routes {

		@RequestMapping({ 
			"/home", 
			"/login" 
		})
		public String index() {
			return "forward:/index.html";
		}
	}

}
