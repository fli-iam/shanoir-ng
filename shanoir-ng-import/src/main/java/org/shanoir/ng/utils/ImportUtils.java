package org.shanoir.ng.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.SystemUtils;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Utility class
 *
 * @author jlouis
 * @author mkain
 */
public class ImportUtils {

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
		ArrayList<E> list = new ArrayList<E>();
		if (iterable != null) {
			for (E e : iterable) {
				list.add(e);
			}
		}
		return list;
	}

	public static boolean equalsIgnoreNull(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		if (o2 == null)
			return o1 == null;
		if (o1 instanceof AbstractGenericItem && o2 instanceof AbstractGenericItem) {
			return ((AbstractGenericItem) o1).getId().equals(((AbstractGenericItem) o2).getId());
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
		boolean result = false;
		ZipEntry entry;
		ZipFile zipfile = new ZipFile(file);
		final Enumeration e = zipfile.entries();
		boolean found = false;
		while (e.hasMoreElements() && !found) {
			entry = (ZipEntry) e.nextElement();
			if (entry.getName().toUpperCase().endsWith(fileName.toUpperCase())) {
				found = true;
				result = true;
			}
		}
		zipfile.close();
		return result;
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
		ZipInputStream zipIn = null;
		try {
			zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
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
		} catch (IOException e) {
			throw e;
		} finally {
			if (zipIn != null) {
				zipIn.close();
			}
		}
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
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(filePath));
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (bos != null) {
				bos.close();
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
			result = new ArrayList<File>();
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
	 * @param wildcard
	 *            the wildcard
	 *
	 * @return the string
	 */
	public static String wildcardToRegex(String wildcard) {
		StringBuffer s = new StringBuffer(wildcard.length());
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
		return (s.toString());
	}

}
