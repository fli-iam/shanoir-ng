package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.dataset.modality.ProcessedDatasetType;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.processing.service.DatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.processing.carmin.model.Execution;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.KeycloakServiceClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * CRON job to request VIP api and create processedDataset
 * 
 * @author KhalilKes
 */
@Service
public class ExecutionStatusMonitor implements ExecutionStatusMonitorService {

    @Value("${vip.uri}")
    private String VIP_URI;

    @Value("${vip.upload-folder}")
    private String importDir;

    @Value("${vip.sleep-time}")
    private long sleepTime;

    @Value("${vip.file-formats}")
    private String[] listOfNiftiExt;

    private boolean stop;

    private String identifier;

    private String accessToken = "";

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

    @Autowired
    private CarminDatasetProcessingService carminDatasetProcessingService;

    @Autowired
    private DatasetProcessingService datasetProcessingService;

    @Autowired
    private ImporterService importerService;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private KeycloakServiceClientUtils keycloakServiceClientUtils;

    @Async
    @Override
    @Transactional
    public void startJob(String identifier) throws EntityNotFoundException {
        int attempts = 1;
        this.identifier = identifier;
        this.stop = false;

        String uri = VIP_URI + identifier + "/summary";
        RestTemplate restTemplate = new RestTemplate();

        // check if the token is initialized
        if (this.accessToken.isEmpty()) {
            // refresh the token and stop the thread if it's not refreshed
            if (!this.refreshServiceClientAccessToken()) {
                return;
            }
        }

        CarminDatasetProcessing carminDatasetProcessing = this.carminDatasetProcessingService
                .findByIdentifier(this.identifier)
                .orElseThrow(() -> new EntityNotFoundException(
                        "entity not found with identifier :" + this.identifier));

        while (!stop) {

            // init headers with the active access token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + this.accessToken);
            HttpEntity entity = new HttpEntity(headers);

            // check how many times the loop tried to get the execution's info without success
            if(attempts >= 3){
                LOG.error("failed to get execution details in {} attempts.", attempts);
                LOG.error("Stopping the thread...");
                this.stop = true;
                break;
            }

            try {
                ResponseEntity<Execution> executionResponseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Execution.class);
                Execution execution = executionResponseEntity.getBody();
                // init attempts
                attempts = 1;
                switch (execution.getStatus()) {
                    case FINISHED:
                        /**
                         * updates the status and finish the job
                         */

                        carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);

                        this.carminDatasetProcessingService.updateCarminDatasetProcessing(carminDatasetProcessing);

                        // untar the .tgz files
                        final File userImportDir = new File(
                                this.importDir + File.separator +
                                        carminDatasetProcessing.getResultsLocation());

                        final PathMatcher matcher = userImportDir.toPath().getFileSystem()
                                .getPathMatcher("glob:**/*.{tgz,tar.gz}");
                        final Stream<java.nio.file.Path> stream = Files.list(userImportDir.toPath());

                        stream.filter(matcher::matches)
                                .forEach(zipFile -> decompressTGZ(zipFile.toFile(),
                                        userImportDir.getAbsoluteFile(),
                                        carminDatasetProcessing));

                        LOG.info("execution status updated, stopping job...");

                        stop = true;
                        break;

                    case UNKOWN:
                    case EXECUTION_FAILED:
                    case KILLED:

                        carminDatasetProcessing.setStatus(execution.getStatus());
                        this.carminDatasetProcessingService.updateCarminDatasetProcessing(carminDatasetProcessing);
                        LOG.info("execution status updated, stopping job...");

                        stop = true;
                        break;

                    case RUNNING:
                        Thread.sleep(sleepTime); // sleep/stop a thread for 20 seconds
                        break;

                    default:
                        this.stop = true;
                        break;
                }
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    LOG.warn("Unauthorized");
                    LOG.info("Getting new token...");
                    if (!this.refreshServiceClientAccessToken()) {
                        this.stop = true;
                        break;
                    }
                    // inc attempts.
                    attempts++;
                } else {
                    LOG.error("error while getting execution info with status : {} ,and message :", e.getStatusCode(), e.getMessage());
                    this.stop = true;
                }
            } catch (RestClientException e) {
                LOG.error("there is no response payload while getting execution info");
                this.stop = true;
            } catch (InterruptedException e) {
                LOG.error("sleep thread exception :", e);
                this.stop = true;
            } catch (IOException e) {
                LOG.error("file exception :", e);
                this.stop = true;
            }
        }
    }

    /**
     * 
     * @param in
     * @param out
     * @param carminDatasetProcessing
     */
    private void decompressTGZ(File in, File out, CarminDatasetProcessing carminDatasetProcessing) {
        try (TarArchiveInputStream fin = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(in)))) {
            TarArchiveEntry entry;

            File cacheFolder = new File(out.getAbsolutePath() + File.separator + "cache");

            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
            }
           
            while ((entry = fin.getNextTarEntry()) != null) {
                                
                String parsedEntry = entry.getName();
                LOG.info("tar entry :" + parsedEntry);

                if (entry.isDirectory()) {
                    continue;
                }

                if (parsedEntry.contains("/")) {
                    parsedEntry = parsedEntry.substring(parsedEntry.lastIndexOf("/") + 1);
                }

                File currentFile = new File(cacheFolder, parsedEntry);
                File parent = currentFile.getParentFile();
                
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                IOUtils.copy(fin, Files.newOutputStream(currentFile.toPath()));

                // check all nifti formats
                for (int i = 0; i < listOfNiftiExt.length; i++) {
                    if (entry.getName().endsWith(listOfNiftiExt[i])) {
                        createProcessedDataset(currentFile, cacheFolder.getAbsolutePath(),
                                carminDatasetProcessing);
                    }
                }

            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     *
     * @param niiftiFile
     * @param destDir
     * @param carminDatasetProcessing
     */
    private void createProcessedDataset(File niiftiFile, String destDir,
            CarminDatasetProcessing carminDatasetProcessing) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists())
            dir.mkdirs();

        try {
            ProcessedDatasetImportJob processedDataset = new ProcessedDatasetImportJob();
            DatasetProcessing datasetProcessing = datasetProcessingService
                    .findById(carminDatasetProcessing.getId())
                    .orElseThrow(() -> new NotFoundException("datasetProcessing not found"));

            Study study = studyRepository.findById(datasetProcessing.getStudyId())
                    .orElseThrow(() -> new NotFoundException("study not found"));

            processedDataset.setDatasetProcessing(datasetProcessing);

            processedDataset.setProcessedDatasetFilePath(niiftiFile.getAbsolutePath());
            processedDataset.setProcessedDatasetType(ProcessedDatasetType.RECONSTRUCTEDDATASET);
            processedDataset.setStudyId(datasetProcessing.getStudyId());
            processedDataset.setStudyName(study.getName());

            List<Dataset> inputDatasets = datasetProcessing.getInputDatasets();

            if(inputDatasets.size() != 0) {
                
                List<Long> subjectIds = inputDatasets.stream().map(dataset -> dataset.getSubjectId())
                    .collect(Collectors.toList());
             
                Predicate<Long> predicate = obj -> Objects.equals(inputDatasets.get(0).getSubjectId(), obj);

                if (subjectIds.stream().allMatch(predicate)) {
                    Subject subject = subjectRepository.findById(inputDatasets.get(0).getSubjectId())
                        .orElseThrow(() -> new NotFoundException("subject not found"));

                    processedDataset.setSubjectId(subject.getId());
                    processedDataset.setSubjectName(subject.getName());
                    processedDataset.setDatasetType(inputDatasets.get(0).getType());
                } else {
                    processedDataset.setDatasetType("Mesh");
                }

            } else {
                processedDataset.setDatasetType("Mesh");
            }
            
            processedDataset.setProcessedDatasetName(getNameWithoutExtension(niiftiFile.getName())); 
            importerService.createProcessedDataset(processedDataset);

            deleteCacheDir(Paths.get(destDir));

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    private void deleteCacheDir(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private String getNameWithoutExtension(String file) {
        int dotIndex = file.indexOf('.');
        return (dotIndex == -1) ? file : file.substring(0, dotIndex);
    }

    private boolean refreshServiceClientAccessToken(){
        AccessTokenResponse accessTokenResponse = keycloakServiceClientUtils.getServiceAccountAccessToken();
        if(accessTokenResponse == null){
            LOG.error("error while getting the service client token");
            return false;
        }
        this.accessToken = accessTokenResponse.getToken();
        LOG.info("new Token : {}", this.accessToken);
        return true;
    }
}
