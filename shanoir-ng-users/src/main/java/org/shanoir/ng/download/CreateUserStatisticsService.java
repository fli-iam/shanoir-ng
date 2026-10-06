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

package org.shanoir.ng.download;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.servlet.http.HttpServletResponse;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CreateUserStatisticsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ShanoirEventService eventService;

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserStatisticsService.class);

    @Transactional
    public void downloadUserStatistics(HttpServletResponse response) throws IOException {

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=\"UserStatistics.zip\"");

        ShanoirEvent event = new ShanoirEvent(
                ShanoirEventType.DOWNLOAD_USER_STATISTICS_EVENT,
                null,
                KeycloakUtil.getTokenUserId(),
                "Fetching users statistics.",
                ShanoirEvent.IN_PROGRESS,
                0f,
                null);
        eventService.publishEvent(event);

        createStats(event, response);
    }

    private void createStats(ShanoirEvent event, HttpServletResponse response) throws IOException {
        try {
            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getUserStatistics");
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();

            ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
            zos.putNextEntry(new ZipEntry("UserStatistics.csv"));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(zos, StandardCharsets.UTF_8));
            for (Object[] row : results) {
                writer.write(Arrays.stream(row)
                        .map(CreateUserStatisticsService::escapeCsv)
                        .collect(Collectors.joining(",")));
                writer.write("\n");
            }
            writer.flush();
            zos.closeEntry();
            zos.finish();

            event.setProgress(1f);
            event.setStatus(ShanoirEvent.SUCCESS);
            event.setMessage("Users statistics fetched.");
        } catch (Exception e) {
            LOG.error("Error during fetching of users statistics", e);
            event.setProgress(-1f);
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Error during fetching of users statistics.");
            throw e;
        } finally {
            eventService.publishEvent(event);
        }
    }

    private static String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
