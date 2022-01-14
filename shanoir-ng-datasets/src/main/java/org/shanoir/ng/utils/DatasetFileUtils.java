package org.shanoir.ng.utils;

import org.apache.commons.io.FilenameUtils;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriUtils;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DatasetFileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetFileUtils.class);


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
     * @param urls
     * @param workFolder
     * @param subjectName the subjectName
     * @throws IOException
     * @throws MessagingException
     */
    public static void copyNiftiFilesForURLs(final List<URL> urls, final File workFolder, Dataset dataset, Object subjectName) throws IOException {
        int index = 0;
        for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
            URL url =  iterator.next();
            File srcFile = new File(UriUtils.decode(url.getPath(), "UTF-8"));

            // Theorical file name:  NomSujet_SeriesDescription_SeriesNumberInProtocol_SeriesNumberInSequence.nii(.gz)
            StringBuilder name = new StringBuilder("");

            name.append(subjectName).append("_");
            if (dataset instanceof EegDataset) {
                name.append(dataset.getName()).append("_");
            } else {
                if (dataset.getUpdatedMetadata().getComment() != null) {
                    name.append(dataset.getUpdatedMetadata().getComment()).append("_");
                }
                name.append(dataset.getDatasetAcquisition().getSortingIndex()).append("_");
                if (dataset.getUpdatedMetadata().getName() != null && dataset.getUpdatedMetadata().getName().lastIndexOf(" ") != -1) {
                    name.append(dataset.getUpdatedMetadata().getName().substring(dataset.getUpdatedMetadata().getName().lastIndexOf(" ") + 1)).append("_");
                }
            }
            name.append(dataset.getDatasetAcquisition().getRank()).append("_")
                    .append(index)
                    .append(".");
            if (srcFile.getName().endsWith(".nii.gz")) {
                name.append("nii.gz");
            } else {
                name.append(FilenameUtils.getExtension(srcFile.getName()));
            }
            File destFile = new File(workFolder.getAbsolutePath() + File.separator + name);
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            index++;
        }
    }

    /**
     * Zip
     *
     * @param sourceDirPath
     * @param zipFilePath
     * @throws IOException
     */
    public static void zip(final String sourceDirPath, final String zipFilePath) throws IOException {
        Path p = Paths.get(zipFilePath);
        // 1. Create an outputstream (zip) on the destination
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(p))) {

            // 2. "Walk" => iterate over the source file
            Path pp = Paths.get(sourceDirPath);
            try(Stream<Path> walker = Files.walk(pp)) {

                // 3. We only consider directories, and we copyt them directly by "relativising" them then copying them to the output
                walker.filter(path -> !path.toFile().isDirectory())
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                            try {
                                zos.putNextEntry(zipEntry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                LOG.error(e.getMessage(), e);
                            }
                        });
            }
            zos.finish();
        }
    }

    public static String generateDatasetName(Dataset dataset){
        String datasetName = "";
        datasetName += dataset.getId() + "-" + dataset.getName();
        if (dataset.getUpdatedMetadata() != null && dataset.getUpdatedMetadata().getComment() != null) {
            datasetName += "-" + dataset.getUpdatedMetadata().getComment();
        }
        return datasetName;
    }

}
