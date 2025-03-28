/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

/**
 * Shanoir-NG microservice import application.
 */
@SpringBootApplication
@OpenAPIDefinition
@EnableAsync
public class ShanoirImportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShanoirImportApplication.class, args);
    }

    /**
     * MK: This bean fixes the issue, that when the async ImporterManagerService is called and inside,
     * like in DatasetsCreatorAndNIfTIConverter a sec annotation like preauthorize occurs, to have
     * a context to be checked and avoid the exception: org.springframework.security.authentication
     * .AuthenticationCredentialsNotFoundException: An Authentication object was not found in the SecurityContext
     */
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

}