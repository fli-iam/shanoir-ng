package org.shanoir.ng.configuration.security;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration 
@ComponentScan("org.shanoir.ng") 
@EnableWebMvc   
@Import({ SecurityConfiguration.class })
public class AppConfig {  
	
} 