package org.shanoir.uploader.upload;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages UploadJobs.
 * @author mkain
 *
 */
public class UploadJobManager {

    private static final Logger logger = LoggerFactory.getLogger(UploadJobManager.class);

    public static final String UPLOAD_JOB_XML = "upload-job.xml";

    private File uploadJobFile;

    /**
     * Initialize UploadJobManager empty and reset uploadJobFile
     * with method setUploadJobFile.
     */
    public UploadJobManager() {
    }

    /**
     * Initialize UploadJobManager with current upload folder path.
     * @param uploadFolder
     */
    public UploadJobManager(final String uploadFolderPath) {
        this.uploadJobFile = new File(
            uploadFolderPath
            + File.separatorChar
            + UPLOAD_JOB_XML);
        logger.debug("UploadJobManager initialized with file: " + this.uploadJobFile.getAbsolutePath());
    }

    /**
     * Initialize UploadJobManager with UploadJob file.
     * @param uploadFolder
     */
    public UploadJobManager(final File uploadJobFile) {
        this.uploadJobFile = uploadJobFile;
        logger.debug("UploadJobManager initialized with file: " + this.uploadJobFile.getAbsolutePath());
    }

    /* (non-Javadoc)
     * @see org.shanoir.uploader.upload.IUploadJobManager#writeUploadJob(org.shanoir.uploader.upload.UploadJob)
     */
    public void writeUploadJob(final UploadJob uploadJob) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(UploadJob.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(uploadJob, uploadJobFile);
        } catch (JAXBException e) {
            logger.error(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.shanoir.uploader.upload.IUploadJobManager#readUploadJob()
     */
    public UploadJob readUploadJob() {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(UploadJob.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final UploadJob uploadJob = (UploadJob) jaxbUnmarshaller.unmarshal(uploadJobFile);
            return uploadJob;
        } catch (JAXBException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public File getUploadJobFile() {
        return uploadJobFile;
    }

    public void setUploadJobFile(File uploadJobFile) {
        this.uploadJobFile = uploadJobFile;
    }

}
