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
    public void createStats(String studyNameInRegExp, String studyNameOutRegExp, String subjectNameInRegExp, String subjectNameOutRegExp, ShanoirEvent event, String params) throws RestServiceException, IOException, InterruptedException {

        LOG.error("createStats : " + studyNameInRegExp);
        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        LOG.error("1");
        File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
        LOG.error("2");
        File statisticsFile = recreateFile(userDir + File.separator + "shanoirExportStatistics_" + event.getId() + ".tsv");
        LOG.error("3");
        File zipFile = recreateFile(userDir + File.separator + "shanoirExportStatistics_" + event.getId() + ZIP);
        LOG.error("4");


        // Get the data
        try (FileOutputStream fos = new FileOutputStream(statisticsFile);
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {

            int maxAttempts = 5;
            int attempt = 0;
            while (!statisticsFile.exists() && attempt < maxAttempts) {
                Thread.sleep(500);
                attempt++;
            }
            LOG.error("file exists");

            //List<Object[]> results = datasetService.queryStatistics(studyNameInRegExp, studyNameOutRegExp, subjectNameInRegExp, subjectNameOutRegExp);

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
                LOG.error("query process");
                float progress = 0;
                Object[] row = (Object[]) result;
                try {
                    progress += 1f / ((Object[]) result).length;
                    event.setProgress(progress);
                    eventService.publishEvent(event);
                    String line = Arrays.stream(row)
                            .map(obj -> Objects.toString(obj, ""))
                            .collect(Collectors.joining("\t"));
                    bw.write(line);
                    bw.newLine();


//            for (Object[] or : query.getResultList()) {
//
//                progress += 1f / results.size();
//                event.setProgress(progress);
//                eventService.publishEvent(event);
//                List<String> strings = Arrays.stream(or).map(object -> Objects.toString(object, null)).collect(Collectors.toList());
//                bw.write(String.join("\t", strings));
//                bw.newLine();
//            }

                } catch (Exception e) {
                    event.setStatus(ShanoirEvent.ERROR);
                    event.setMessage("Error during fetching of statistics.");
                    event.setProgress(-1f);
                    eventService.publishEvent(event);
                    LOG.error("Error during fetching of statistics with id : " + event.getId());
                }

            });
        } catch (Exception e) {
            event.setStatus(ShanoirEvent.ERROR);
            event.setMessage("Error during fetching of statistics.");
            event.setProgress(-1f);
            eventService.publishEvent(event);
            LOG.error("Error during fetching of statistics with id : " + event.getId());

        } finally {
            zipSingleFile(statisticsFile, zipFile);
            statisticsFile.delete();
            event.setObjectId(String.valueOf(event.getId()));
            event.setProgress(1f);
            event.setMessage("Statistics fetched with params : " + params + "\nDownload available for 6 hours");
            event.setStatus(ShanoirEvent.SUCCESS);
            eventService.publishEvent(event);
        }

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
