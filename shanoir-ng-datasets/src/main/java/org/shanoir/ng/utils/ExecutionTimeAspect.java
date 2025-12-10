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

package org.shanoir.ng.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime >= logExecutionTime.thresholdMs()) {
                String methodName = joinPoint.getSignature().toShortString();
                String customMessage = logExecutionTime.value();
                if (!customMessage.isEmpty()) {
                    LOG.info("{} - Execution time: {} ms", customMessage, executionTime);
                } else {
                    LOG.info("{} executed in {} ms", methodName, executionTime);
                }
            }
        }
        return result;
    }

}
