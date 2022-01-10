/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

/**
 * Utility class
 *
 * @author jlouis
 * @author mkain
 */
public class ImportUtils {

	private static final String INTO = " into ";

	private static final Logger LOG = LoggerFactory.getLogger(ImportUtils.class);

	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String APPLICATION_ZIP = "application/zip";

	private static final String ZIP_FILE_SUFFIX = ".zip";

	private static final String FILE_POINT = ".";

	private static final String DICOMDIR = "DICOMDIR";

	private static final String UPLOAD_FILE_SUFFIX = ".upload";

	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * @todo: read from application.yml -> Yao
	 */
	// dcmdjpeg (from dcmtk) path under linux
	private static final String DCMDJPEG_LINUX_PATH = "/usr/bin/dcmdjpeg";

	// dcmdjpeg (from dcmtk) path under windows
	private static final String DCMDJPEG_WINDOWS_PATH = "dcmdjpeg/windows/dcmdjpeg.exe";

	/** The Constant KB. */
	private static final int KB = 1024;

	/** The Constant BUFFER_SIZE. */
	private static final int BUFFER_SIZE = 2 * KB;

	/**
	 * Convert Iterable to List
	 *
	 * @param iterable
	 * @return a list
	 */
	public static <E> List<E> toList(Iterable<E> iterable) {
		if (iterable instanceof List) {
			return (List<E>) iterable;
		}
		ArrayList<E> list = new ArrayList<>();
		if (iterable != null) {
			for (E e : iterable) {
				list.add(e);
			}
		}
		return list;
	}

	public static boolean equalsIgnoreNull(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		if (o2 == null) {
			return o1 == null;
		}
		if (o1 instanceof AbstractEntity && o2 instanceof AbstractEntity) {
			return ((AbstractEntity) o1).getId().equals(((AbstractEntity) o2).getId());
		}
		return o1.equals(o2) || o2.equals(o1);
		// o1.equals(o2) is not equivalent to o2.equals(o1) ! For instance with
		// java.sql.Timestamp and java.util.Date
	}

	/**
	 * Returns the path to dcmdjpeg.
	 *
	 * @return the dcmdjpeg path
	 */
	public static String getDcmdjpegPath() {
		String cmd = "";
		if (SystemUtils.IS_OS_WINDOWS) {
			cmd = DCMDJPEG_WINDOWS_PATH;
		} else if (SystemUtils.IS_OS_LINUX) {
			cmd = DCMDJPEG_LINUX_PATH;
		}
		return cmd;
	}

	/**
	 * Check if the given compressed file contains a file whith name fileName.
	 *
	 * @param fileName
	 *            the file name
	 * @param file
	 *            the file
	 *
	 * @return true, if check zip contains file
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static boolean checkZipContainsFile(final String fileName, final File file) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		final Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().toUpperCase().equals(fileName.toUpperCase())) {
				zipFile.close();
				return true;
			}
		}
		zipFile.close();
		return false;
	}

	/**
	 * Extracts a zip file specified by the zipFilePath to a directory specified by
	 * destDirectory (will be created if does not exists)
	 * 
	 * @param zipFilePath
	 * @param destDirectory
	 * @throws IOException
	 */
	public static void unzip(String zipFilePath, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
			ZipEntry entry = zipIn.getNextEntry();
			String directoryFile;
			String name;
			// iterates over entries in the .zip file
			while (entry != null) {
				name = entry.getName();
				String filePath = destDirectory + File.separator + name;
				if (!entry.isDirectory()) {
					// if the entry is a file, extracts it
					// create the dir if necessary, file entry can come before directory entry where
					// is file located
					directoryFile = getDirectoryPart(name);
					if (directoryFile != null) {
						createDirectory(destDir, directoryFile);
					}
					extractFile(zipIn, filePath);
				} else {
					// if the entry is a directory, make the directory
					File dir = new File(filePath);
					dir.mkdir();
				}
				zipIn.closeEntry();
				entry = zipIn.getNextEntry();
			}
		}
	}

	/**
	 * Zips a file
	 * @param fileToZip file to zip
	 * @param fileName new fileName
	 * @param zipOut file touget out
	 * @throws IOException
	 */
    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, boolean first) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/") && !first) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else if (!first) {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, (first? "" : fileName + "/") + childFile.getName(), zipOut, false);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
   
	/**
	 * Check if sent file is of type .zip.
	 *
	 * @param file
	 */
	public static boolean isZipFile(final MultipartFile file) {
		return file.getOriginalFilename().endsWith(ZIP_FILE_SUFFIX) || file.getContentType().equals(APPLICATION_ZIP)
				|| file.getContentType().equals(APPLICATION_OCTET_STREAM);
	}

	public static File getUserImportDir(String importDir) {
		final Long userId = KeycloakUtil.getTokenUserId();
		final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
		final File userImportDir = new File(userImportDirFilePath);
		if (!userImportDir.exists()) {
			userImportDir.mkdirs(); // create if not yet existing
		} // else is wanted case, user has already its import directory
		return userImportDir;
	}

	private static void createDirectory(File outdir, String path) {
		File d = new File(outdir, path);
		if (!d.exists()) {
			d.mkdirs();
		}
	}

	private static String getDirectoryPart(String name) {
		int s = name.lastIndexOf(File.separatorChar);
		return s == -1 ? null : name.substring(0, s);
	}

	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		}
	}

	/**
	 * Replace the file separators to make it work under windows or unix system.
	 *
	 * @param firstImagePath
	 *            the first image path
	 *
	 * @return the string
	 */
	public static String convertFilePath(final String firstImagePath) {
		return firstImagePath.replaceAll("\\\\", "/");
	}
	
	/**
	 * List all the folders of the given directory.
	 *
	 * @param serieFolder
	 *            the serie folder
	 *
	 * @return the list< file>
	 */
	public static List<File> listFolders(File serieFolder) {
		List<File> result = null;
		if (serieFolder != null) {
			result = new ArrayList<>();
			final File[] listFiles = serieFolder.listFiles();
			for (final File file : listFiles) {
				if (file.isDirectory()) {
					result.add(file);
				}
			}
		}
		return result;
	}
	
	/**
	 * Convert a String with a wildcard to a regular expression.
	 *
	 * @param wildcard the wildcard
	 *
	 * @return the string
	 */
	public static String wildcardToRegex(String wildcard) {
		StringBuilder s = new StringBuilder(wildcard.length());
		s.append('^');
		for (int i = 0, is = wildcard.length(); i < is; i++) {
			char c = wildcard.charAt(i);
			switch (c) {
			case '*':
				s.append(".*");
				break;
			case '?':
				s.append(".");
				break;
				// escape special regexp-characters
			case '(':
			case ')':
			case '[':
			case ']':
			case '$':
			case '^':
			case '.':
			case '{':
			case '}':
			case '|':
			case '\\':
				s.append("\\");
				s.append(c);
				break;
			default:
				s.append(c);
				break;
			}
		}
		s.append('$');
		return s.toString();
	}
	
	/**
	 * copyFiles folder into destination,respecting the hierarchy
	 * 
	 * @param folder
	 * @param destination
	 */
	public static void copyAllFiles(File folder, File destination) {
		LOG.debug(" copyAllFiles from " + folder.getName() + " is directory " + folder.isDirectory() + INTO
				+ destination.getName());
		if (folder.isDirectory()) {
			for (File inner : Arrays.asList(folder.listFiles())) {
				if (inner.isDirectory()) {
					copyAllFiles(inner, destination);
				} else {
					LOG.debug("copyAllFiles copying file {}", inner.getName());
					copyFile(inner, new File(destination + File.pathSeparator + inner.getName()), false);
				}
			}
		} else {
			LOG.debug("copyAllFiles directly copying file {}", folder.getName());
			copyFile(folder, new File(destination + File.pathSeparator + folder.getName()), false);
		}
	}

	/***
	 * 
	 * Copy File in to file out.
	 * 
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * @param overwrite
	 *            the overwrite
	 * @return a hashmap with the key=success if the copy is ok and the value is the
	 *         real output file.
	 */

	public static Map<Boolean, File> copyFile(final File in, final File out, final boolean overwrite) {
		return moveOrCopyFile(in, out, overwrite, false);
	}

	/**
	 * Move or copy the file. It is possible to overwrite or not the destination
	 * file.
	 *
	 * @param in
	 *            the in
	 * @param out
	 *            the out
	 * @param overwrite
	 *            the overwrite
	 * @param move
	 *            the move
	 *
	 * @return the hash map< boolean, file>
	 */
	private static HashMap<Boolean, File> moveOrCopyFile(final File in, final File out, final boolean overwrite,
			final boolean move) {
		final HashMap<Boolean, File> result = new HashMap<>();

		LOG.debug("moveOrCopyFile : (File in {}, File out {}, overwrite {}, move {})", in, out, overwrite, move);

		// rename the file if needed
		if (out.exists() && !overwrite) {
			final String folder = out.getParent();
			String newName = getRenamedFile(out.getName());
			File realOut = new File(folder + '/' + newName);
			return moveOrCopyFile(in, realOut, overwrite, move);
		} else {
			result.clear();
			if (move) { // move file
				try {
					Files.move(in, out);
					result.put(Boolean.TRUE, out);
				} catch (IOException e) {
					result.put(Boolean.FALSE, out);
					LOG.error("Error while moving file " + in + INTO + out, e);
				} catch (IllegalArgumentException e2) {
					result.put(Boolean.FALSE, out);
					LOG.error("Error while moving same file " + in + INTO + out, e2);
				}
			} else { // copy file
				try {
					FileUtils.copyFile(in, out);
					result.put(Boolean.TRUE, out);
				} catch (IOException exc) {
					LOG.error("moveOrCopyFile : error while copying file : " + exc.getMessage(), exc);
					result.put(Boolean.FALSE, out);
				}
			}
		}
		return result;
	}

	/**
	 * Gets a new name for the given filename, by adding the next letter in the
	 * alphabet, (before extension)
	 * 
	 * @param name
	 * @return new filename
	 */
	private static String getRenamedFile(String name) {
		final String[] alphabet = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
				"R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		final List<String> alphabetList = Arrays.asList(alphabet);
		String newName = null;
		String nameNoExtension = name;
		String extension = "";
		if (name.lastIndexOf('.') != -1) {
			nameNoExtension = name.substring(0, name.lastIndexOf('.'));
			extension = name.substring(name.lastIndexOf('.'), name.length());
		}

		final String lastCharacter = Character.toString(nameNoExtension.charAt(nameNoExtension.length() - 1));
		if ("Z".equals(lastCharacter)) {
			newName = nameNoExtension.substring(0, nameNoExtension.length() - 1) + "AA" + extension;
		} else if (alphabetList.contains(lastCharacter)) {
			int index = alphabetList.indexOf(lastCharacter);
			newName = nameNoExtension.substring(0, nameNoExtension.length() - 1) + alphabetList.get(index + 1)
					+ extension;
		} else {
			newName = nameNoExtension + "A" + extension;
		}
		return newName;
	}

	/**
	 * This method stores an uploaded zip file in a temporary file, creates a new
	 * folder with the same name and unzips the content into this folder, and gives
	 * back the folder with the content.
	 * 
	 * @param tempFile
	 * @param dicomZipFile
	 * @return
	 * @throws IOException
	 * @throws RestServiceException
	 */
	public static File saveTempFileCreateFolderAndUnzip(final File tempFile, final MultipartFile dicomZipFile,
			final boolean fromDicom) throws IOException, RestServiceException {
		String fileName = tempFile.getName();
		int pos = fileName.lastIndexOf(FILE_POINT);
		if (pos > 0) {
			fileName = fileName.substring(0, pos);
		}
		File unzipFolderFile = new File(tempFile.getParentFile().getAbsolutePath() + File.separator + fileName);
		if (!unzipFolderFile.exists()) {
			unzipFolderFile.mkdirs();
		} else {
			throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while unzipping file: folder already exists.", null));
		}
		ImportUtils.unzip(tempFile.getAbsolutePath(), unzipFolderFile.getAbsolutePath());
		tempFile.delete();
		return unzipFolderFile;
	}

	/**
	 * This method takes a multipart file and stores it in a configured upload
	 * directory in relation with the userId with a random name and the suffix
	 * .upload
	 *
	 * @param file
	 * @throws IOException
	 */
	public static File saveTempFile(final File userImportDir, final MultipartFile file) throws IOException {
		long n = createRandomLong();
		File uploadFile = new File(userImportDir.getAbsolutePath(), Long.toString(n) + UPLOAD_FILE_SUFFIX);
		file.transferTo(uploadFile);
		return uploadFile;
	}

	/**
	 * This method creates a random long number.
	 * 
	 * @return long: random number
	 */
	public static long createRandomLong() {
		long n = RANDOM.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}
		return n;
	}

}
