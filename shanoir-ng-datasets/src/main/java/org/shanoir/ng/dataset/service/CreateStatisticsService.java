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
        float progress = 0;
        String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        File userDir = DatasetFileUtils.getUserImportDir(tmpDir);
        File statisticsFile = recreateFile(userDir + File.separator + "shanoirExportStatistics_" + event.getId() + ".tsv");
        File zipFile = recreateFile(userDir + File.separator + "shanoirExportStatistics_" + event.getId() + ZIP);

        // Get the data
        try (FileOutputStream fos = new FileOutputStream(statisticsFile);
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))){

            int startRow = 0;
            int blocSize = 50000;

            while (true) {
                LOG.error("======");
                LOG.error("startRow : " + startRow);
                LOG.error("blocSize : " + blocSize);
                event.setMessage("Querying results block from " + startRow + " to " + startRow + blocSize);
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
                LOG.error("result size : " + results.size());

                if (results.isEmpty()) {
                    LOG.error("break");
                    break;
                }

                for (Object[] or : results) {
                    LOG.error("for loop");
                    progress += 1f / results.size();
                    event.setProgress(progress);
                    eventService.publishEvent(event);
                    List<String> strings = Arrays.stream(or).map(object -> Objects.toString(object, null)).collect(Collectors.toList());
                    bw.write(String.join("\t", strings));
                    bw.newLine();
                    LOG.error("write : " + strings);
                }
                startRow += blocSize;
            }

//            List<Object[]> results = datasetService.queryStatistics(studyNameInRegExp, studyNameOutRegExp, subjectNameInRegExp, subjectNameOutRegExp);
//
//            for (Object[] or : results) {
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
            LOG.error(e.getMessage(), e);
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
