package org.shanoir.studycard.repositories;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author msimon
 *
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"org.shanoir.studycard.model"})
@EnableJpaRepositories(basePackages = {"org.shanoir.studycard.repositories"})
@EnableTransactionManagement
public class RepositoryConfiguration {

}
