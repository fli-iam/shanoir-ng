package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

/**
 * A utility class introduced to perform
 * optimized operations in Java within the file system.
 * @author mkain
 *
 */
public class FileUtil {

	private static Logger logger = Logger.getLogger(FileUtil.class);
	
	/**
	 * This method receives a directory name as a string
	 * and checks if there already exists a file like this.
	 * If the file doesn't exist a new directory is created.
	 * @param dirName
	 * @return true - if the directory exists, false - otherwise
	 */
	public static void dirExistsWithCreate(String dirName) {
		final File folder = new File(dirName);
		if (!folder.exists()) {
			if(!folder.mkdir()) {
				logger.error("dirExistsWithCreate: error while creating directory " + dirName);
			}
		}
	}
	
	/**
	 * Use this method for calculating the file size.
	 * @param size
	 * @return
	 */
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	/**
	 * Java NIO file copying to improve the performance of file copying.
	 * In general this solution requires only 2/3 of the time of standard
	 * Java IO. The difference of time is growing regarding bigger files,
	 * starting with 5 MB up to x GB.
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) {
		FileChannel source = null;
		FileChannel destination = null;
		try {
			if (!destFile.exists()) {
				destFile.createNewFile();
			}
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (FileNotFoundException e) {
			logger.error("File " +sourceFile.getAbsolutePath()+" not found",e);
		} catch (IOException e) {
			logger.error("IO Exception" , e);
		} finally {
			try {
				if (source != null) {
					source.close();
				}
				if (destination != null) {
					destination.close();
				}
			} catch (IOException e) {
				logger.error("IO Exception" , e);
			}
		}
	}

}
