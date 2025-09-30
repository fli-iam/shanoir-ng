package org.shanoir.uploader.nominativeData;

import java.io.File;

import org.shanoir.uploader.ShUpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

/**
 * This class manages NominativeDataUploadJob.
 * @author ifakhfakh
 *
 */
public class NominativeDataUploadJobManager {

    private static final Logger logger = LoggerFactory.getLogger(NominativeDataUploadJobManager.class);

    public static final String NOMINATIVE_DATA_JOB_XML = ShUpConfig.NOMINATIVE_DATA_JOB_XML;

    private File nominativeDataJobFile;

    /**
     * Initialize UploadJobManager empty and reset uploadJobFile
     * with method setUploadJobFile.
     */
    public NominativeDataUploadJobManager() {
    }

    /**
     * Initialize MoninativeDataUploadJobManager with current moninative date folder path.
     * @param uploadFolder
     */
    public NominativeDataUploadJobManager(final String uploadFolderPath) {
        this.nominativeDataJobFile = new File(
            uploadFolderPath
            + File.separatorChar
            + NOMINATIVE_DATA_JOB_XML);
        logger.debug("UploadJobManager initialized with file: "
                + this.nominativeDataJobFile.getAbsolutePath());
    }

    /**
     * Initialize UploadJobManager with UploadJob file.
     * @param uploadFolder
     */
    public NominativeDataUploadJobManager(final File uploadJobFile) {
        this.nominativeDataJobFile = uploadJobFile;
        logger.debug("UploadJobManager initialized with file: "
                + this.nominativeDataJobFile.getAbsolutePath());
    }

    /* (non-Javadoc)
     * @see org.shanoir.uploader.upload.IUploadJobManager#writeUploadJob(org.shanoir.uploader.upload.UploadJob)
     */

    public void writeUploadDataJob(final NominativeDataUploadJob nominativeDataUploadJob) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(NominativeDataUploadJob.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(nominativeDataUploadJob, nominativeDataJobFile);
        } catch (JAXBException e) {
            logger.error(e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.shanoir.uploader.upload.IUploadJobManager#readUploadJob()
     */

    public NominativeDataUploadJob readUploadDataJob() {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(NominativeDataUploadJob.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final NominativeDataUploadJob uploadJob = (NominativeDataUploadJob) jaxbUnmarshaller.unmarshal(nominativeDataJobFile);
            return uploadJob;
        } catch (JAXBException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    public File getUploadJobFile() {
        return nominativeDataJobFile;
    }

    public void setUploadJobFile(File uploadJobFile) {
        this.nominativeDataJobFile = uploadJobFile;
    }

}
