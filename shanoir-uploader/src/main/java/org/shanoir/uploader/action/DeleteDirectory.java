package org.shanoir.uploader.action;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteDirectory {

    private static final Logger logger = LoggerFactory.getLogger(DeleteDirectory.class);

    public void delete(String filePath) {

        File directory = new File(filePath);

        // make sure directory exists
        if (!directory.exists()) {

            logger.debug("Directory does not exist.");
            System.exit(0);

        } else {

            try {

                delete(directory);

            } catch (IOException e) {
                logger.error(e.toString());
                System.exit(0);
            }
        }

        logger.info(" End delete process .");
    }

    public static void delete(File file) throws IOException {

        if (file.isDirectory()) {

            // directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                logger.info("Directory is deleted : " + file.getAbsolutePath());

            } else {

                // list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);

                    // recursive delete
                    delete(fileDelete);
                }

                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    logger.info("Directory is deleted : " + file.getAbsolutePath());
                }
            }

        } else {
            // if file, then delete it
            file.delete();
            logger.debug("File is deleted : " + file.getAbsolutePath());
        }
    }
}