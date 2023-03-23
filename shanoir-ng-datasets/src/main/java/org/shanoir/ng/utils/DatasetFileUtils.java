package org.shanoir.ng.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;

import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.dataset.dto.InputDTO;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatasetFileUtils {

    private static final String UNDERSCORE = "_";

    public static final String INPUT = "input.json";

    public static File getUserImportDir(String importDir) {
        final Long userId = KeycloakUtil.getTokenUserId();
        final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
        final File userImportDir = new File(userImportDirFilePath);
        if (!userImportDir.exists()) {
            userImportDir.mkdirs(); // create if not yet existing
        } // else is wanted case, user has already its import directory
        return userImportDir;
    }

    /**
     * Reads all dataset files depending on the format attached to one dataset.
     * @param dataset
     * @param pathURLs
     * @throws MalformedURLException
     */
    public static void getDatasetFilePathURLs(final Dataset dataset, final List<URL> pathURLs, final DatasetExpressionFormat format) throws MalformedURLException {
        List<DatasetExpression> datasetExpressions = dataset.getDatasetExpressions();
        for (Iterator<DatasetExpression> itExpressions = datasetExpressions.iterator(); itExpressions.hasNext();) {
            DatasetExpression datasetExpression = itExpressions.next();
            if (datasetExpression.getDatasetExpressionFormat().equals(format)) {
                List<DatasetFile> datasetFiles = datasetExpression.getDatasetFiles();
                for (Iterator<DatasetFile> itFiles = datasetFiles.iterator(); itFiles.hasNext();) {
                    DatasetFile datasetFile = itFiles.next();
                    URL url = new URL(datasetFile.getPath().replaceAll("%20", " "));
                    pathURLs.add(url);
                }
            }
        }
    }

    /**
     * Receives a list of URLs containing file:/// urls and copies the files to a folder named workFolder.
     * Return the list of copied files
     *
     * @param urls
     * @param workFolder
     * @param subjectName the subjectName
     * @param datasetFilePath 
     * @throws IOException
     * @throws MessagingException
     * @return
     */
    public static List<String> copyNiftiFilesForURLs(final List<URL> urls, final ZipOutputStream zipOutputStream, Dataset dataset, String subjectName, boolean keepName, String datasetFilePath) throws IOException {
        int index = 0;
        List<String> files = new ArrayList<>();
        for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
            URL url =  iterator.next();
            File srcFile = new File(UriUtils.decode(url.getPath(), StandardCharsets.UTF_8.name()));

            String fileName = getFileName(keepName, srcFile, subjectName, dataset, index);

            if (fileName.contains(File.separator)) {
                fileName = fileName.replaceAll(File.separator, UNDERSCORE);
            }
            // add folder logic if necessary
            if (datasetFilePath != null) {
            	fileName = datasetFilePath + File.separator + fileName;
            }
            
            files.add(fileName);
            
            FileSystemResource fileSystemResource = new FileSystemResource(srcFile.getAbsolutePath());
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipEntry.setSize(fileSystemResource.contentLength());
            zipEntry.setTime(System.currentTimeMillis());
            zipOutputStream.putNextEntry(zipEntry);
            StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
            zipOutputStream.closeEntry();

            index++;
        }
        return files;
    }

	public static void writeInputFileForExport(final ZipOutputStream zipOutputStream, Map<Long, List<String>> files2AcquisitionId) throws IOException {
        InputDTO input = new InputDTO();
        for (Map.Entry<Long, List<String>> entry : files2AcquisitionId.entrySet()) {
            InputDTO.InputSerieDTO serie = new InputDTO.InputSerieDTO();
            serie.setId(entry.getKey());
            for(String file : entry.getValue()){
                serie.getFiles().add(file);
            }
            input.getSeries().add(serie);
        }
        
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET,false);

        ZipEntry zipEntry = new ZipEntry(INPUT);
        zipEntry.setTime(System.currentTimeMillis());
        zipOutputStream.putNextEntry(zipEntry);
        new ObjectMapper(jsonFactory).writeValue(zipOutputStream, input);
        zipOutputStream.closeEntry();
    }

    public static String getFileName(boolean keepName, File srcFile, String subjectName, Dataset dataset, int index) {
        // Theorical file name:  NomSujet_SeriesDescription_SeriesNumberInProtocol_SeriesNumberInSequence.nii(.gz)
        StringBuilder name = new StringBuilder("");
        
        if (dataset.getDatasetProcessing() != null || dataset.getDatasetAcquisition() == null) {
        	keepName = true;
        }

        if (keepName) {
            name.append(srcFile.getName());
        } else {
            name.append(subjectName).append(UNDERSCORE);
            if (dataset instanceof EegDataset) {
                name.append(dataset.getName()).append(UNDERSCORE);
            } else {
                if (dataset.getUpdatedMetadata().getComment() != null) {
                    name.append(dataset.getUpdatedMetadata().getComment()).append(UNDERSCORE);
                }
                name.append(dataset.getDatasetAcquisition().getSortingIndex()).append(UNDERSCORE);
                if (dataset.getUpdatedMetadata().getName() != null && dataset.getUpdatedMetadata().getName().lastIndexOf(" ") != -1) {
                    name.append(dataset.getUpdatedMetadata().getName().substring(dataset.getUpdatedMetadata().getName().lastIndexOf(" ") + 1)).append(UNDERSCORE);
                }
            }
            name.append(dataset.getDatasetAcquisition().getRank()).append(UNDERSCORE)
                    .append(index)
                    .append(".");
            if (srcFile.getName().endsWith(".nii.gz")) {
                name.append("nii.gz");
            } else {
                name.append(FilenameUtils.getExtension(srcFile.getName()));
            }
        }
        return name.toString();
    }
}
