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

package org.shanoir.uploader.test;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceTests extends AbstractTest {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTests.class);

    @Test
    public void findStudiesTest() throws Exception {
        logger.info("Starting findStudiesTest");
        long totalTime = 0;
        int numberOfCalls = 100;
        for (int i = 0; i < numberOfCalls; i++) {
            long startTime = System.currentTimeMillis();
            shUpClient.findStudies();
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            totalTime += elapsedTime;
        }
        long averageTime = totalTime / numberOfCalls;
        logger.info("Average request time: " + averageTime + "ms (over " + numberOfCalls + " calls)");
    }

    @Test
    public void findStudiesNamesAndCentersTest() throws Exception {
        logger.info("Starting findStudiesNamesAndCentersTest");
        long totalTime = 0;
        int numberOfCalls = 100;
        for (int i = 0; i < numberOfCalls; i++) {
            long startTime = System.currentTimeMillis();
            shUpClient.findStudiesNamesAndCenters();
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            totalTime += elapsedTime;
        }
        long averageTime = totalTime / numberOfCalls;
        logger.info("Average request time: " + averageTime + "ms (over " + numberOfCalls + " calls)");
    }

    @Test
    public void findStudiesPublicDataTest() throws Exception {
        logger.info("Starting findStudiesPublicDataTest");
        long totalTime = 0;
        int numberOfCalls = 100;
        for (int i = 0; i < numberOfCalls; i++) {
            long startTime = System.currentTimeMillis();
            shUpClient.findStudiesPublicData();
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            totalTime += elapsedTime;
        }
        long averageTime = totalTime / numberOfCalls;
        logger.info("Average request time: " + averageTime + "ms (over " + numberOfCalls + " calls)");
    }

    @Test
    public void findExaminationsTest() throws Exception {
        logger.info("Starting findExaminationsTest");
        long totalTime = 0;
        int numberOfCalls = 100;
        for (int i = 0; i < numberOfCalls; i++) {
            long startTime = System.currentTimeMillis();
            shUpClient.findExaminations();
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            totalTime += elapsedTime;
        }
        long averageTime = totalTime / numberOfCalls;
        logger.info("Average request time: " + averageTime + "ms (over " + numberOfCalls + " calls)");
    }

}
