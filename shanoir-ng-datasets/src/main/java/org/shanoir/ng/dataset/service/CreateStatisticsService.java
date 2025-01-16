package org.shanoir.ng.dataset.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CreateStatisticsService {
    @Autowired
    ShanoirEventService eventService;
    @Autowired
    private DatasetService datasetService;
    @PersistenceContext
    private EntityManager entityManager;
    private static final String ZIP = ".zip";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final Logger LOG = LoggerFactory.getLogger(CreateStatisticsService.class);

    private File recreateFile(final String fileName) throws IOException {
        File file = new File(fileName);
        if(file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

    @Async
    @Transactional
    public void createStats(String studyNameInRegExp, String studyNameOutRegExp, String subjectNameInRegExp, String subjectNameOutRegExp, ShanoirEvent event, String params) throws IOException {
        LOG.error("createStats");
        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
        File zipFile = recreateFile(userDir + File.separator + "shanoirExportStatistics_" + event.getId() + ZIP);

        // Get the data and write it into a zip file
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            ZipEntry zipEntry = new ZipEntry("shanoirExportStatistics_" + event.getId() + ".tsv");
            zos.putNextEntry(zipEntry);

            OutputStreamWriter writer = new OutputStreamWriter(zos);

            StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getStatistics");
            query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);
            query.setParameter(1, studyNameInRegExp);
            query.setParameter(2, studyNameOutRegExp);
            query.setParameter(3, subjectNameInRegExp);
            query.setParameter(4, subjectNameOutRegExp);

            query.getResultStream().forEach(result -> {
                float progress = 0;
                Object[] row = (Object[]) result;
                try {
                    progress += 1f / ((Object[]) result).length;
                    event.setProgress(progress);
                    eventService.publishEvent(event);
                    String line = Arrays.stream(row)
                            .map(obj -> Objects.toString(obj, ""))
                            .collect(Collectors.joining("\t"));
                    LOG.error("line : " + line);
                    writer.write(line + "\n");
                    writer.flush();
                } catch (Exception e) {
                    event.setStatus(ShanoirEvent.ERROR);
                    event.setMessage("Error during writing of statistics.");
                    event.setProgress(-1f);
                    eventService.publishEvent(event);
                    LOG.error("Error during writing of statistics with id : " + event.getId());
                    LOG.error(e.getMessage(), e);
                }
            });

            zos.closeEntry();
            LOG.error("end writing stats");
        } catch (Exception e) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Error during writing of statistics.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            LOG.error("Error during writing of statistics with id : " + event.getId());
            LOG.error(e.getMessage(), e);
        } finally {
            event.setObjectId(String.valueOf(event.getId()));
            event.setProgress(1f);
            event.setMessage("Statistics fetched with params : " + params + "\nDownload available for 6 hours");
            event.setStatus(ShanoirEvent.SUCCESS);
            eventService.publishEvent(event);
        }
    }
}
