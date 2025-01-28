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
import java.util.ArrayList;
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
        float progress = 0;
        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
        File zipFile = recreateFile(userDir + File.separator + "shanoirExportStatistics_" + event.getId() + ZIP);
        int startRow = 0;
        int blocSize = 50000;

        event.setMessage("Querying size...");
        eventService.publishEvent(event);

        int procedureSize = querySize(studyNameInRegExp, studyNameOutRegExp, subjectNameInRegExp, subjectNameOutRegExp);
        if (procedureSize > -1) {

            if (procedureSize < blocSize) {
                blocSize = procedureSize;
            }

            // Get the data
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                ZipEntry zipEntry = new ZipEntry("shanoirExportStatistics_" + event.getId() + ".tsv");
                zos.putNextEntry(zipEntry);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(zos);

                try (BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {

                    while (true) {
                        event.setMessage("Querying results: " + (startRow + blocSize) + "/" + procedureSize);
                        eventService.publishEvent(event);
                        //"getStatistics" is the name of the MySQL procedure
                        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("getStatistics");

                        //Declare the parameters in the same order
                        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
                        query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
                        query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
                        query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);
                        query.registerStoredProcedureParameter(5, Integer.class, ParameterMode.IN);
                        query.registerStoredProcedureParameter(6, Integer.class, ParameterMode.IN);

                        //Pass the parameter values
                        query.setParameter(1, studyNameInRegExp);
                        query.setParameter(2, studyNameOutRegExp);
                        query.setParameter(3, subjectNameInRegExp);
                        query.setParameter(4, subjectNameOutRegExp);
                        query.setParameter(5, startRow);
                        query.setParameter(6, blocSize);

                        //Execute query
                        @SuppressWarnings("unchecked")
                        List<Object[]> results = query.getResultList();

                        if (results.isEmpty()) {
                            break;
                        }

                        for (Object[] or : results) {
                            List<String> strings = Arrays.stream(or).map(object -> Objects.toString(object, null)).collect(Collectors.toList());
                            writer.write(String.join("\t", strings));
                            writer.write('\n');
                        }

                        progress += (1f + blocSize) / procedureSize;
                        event.setProgress(progress);
                        eventService.publishEvent(event);

                        startRow += blocSize;
                        if (startRow > procedureSize) {
                            break;
                        }
                    }

                    writer.flush();

                }

            } catch (Exception e) {
                event.setStatus(ShanoirEvent.ERROR);
                event.setMessage("Error during fetching of statistics.");
                event.setProgress(-1f);
                eventService.publishEvent(event);
                LOG.error("Error during fetching of statistics with id : " + event.getId());
                LOG.error(e.getMessage(), e);
            } finally {
                event.setObjectId(String.valueOf(event.getId()));
                event.setProgress(1f);
                event.setMessage("Statistics fetched with params : " + params + "\nDownload available for 6 hours");
                event.setStatus(ShanoirEvent.SUCCESS);
                eventService.publishEvent(event);
            }
        } else {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Error during calculation of statistics size.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
        }
    }

    private int querySize(String studyNameInRegExp, String studyNameOutRegExp, String subjectNameInRegExp, String subjectNameOutRegExp) {
        StoredProcedureQuery querySize = entityManager.createStoredProcedureQuery("getStatisticsSize");

        querySize.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        querySize.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
        querySize.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
        querySize.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);
        querySize.setParameter(1, studyNameInRegExp);
        querySize.setParameter(2, studyNameOutRegExp);
        querySize.setParameter(3, subjectNameInRegExp);
        querySize.setParameter(4, subjectNameOutRegExp);

        Object sizeRes = querySize.getSingleResult();
        int size = (sizeRes != null) ? ((Number) sizeRes).intValue() : -1;

        return size;
    }

    /**
     * Zip a single file
     *
     * @param sourceFile
     * @param zipFile
     * @throws IOException
     */
    private void zipSingleFile(final File sourceFile, final File zipFile) throws IOException {

        byte[] buffer = new byte[1024];


        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(sourceFile);
        ) {
            // begin writing a new ZIP entry, positions the stream to the start of the entry data
            zos.putNextEntry(new ZipEntry(sourceFile.getName()));

            int length;

            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }
}
