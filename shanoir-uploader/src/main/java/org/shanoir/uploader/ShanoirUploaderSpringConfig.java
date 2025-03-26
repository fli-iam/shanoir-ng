package org.shanoir.uploader;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = "org.shanoir.uploader")
@EnableScheduling
public class ShanoirUploaderSpringConfig {

}
