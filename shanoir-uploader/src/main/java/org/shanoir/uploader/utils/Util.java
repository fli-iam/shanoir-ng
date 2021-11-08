package org.shanoir.uploader.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.shanoir.dicom.importer.MRI;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.model.DicomTreeNode;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShanoirUploader;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Util class needed by the ImportDetailsListener controller
 * 
 * @author mkain
 * @author atouboul
 *
 */
public final class Util {

	private static Logger logger = Logger.getLogger(Util.class);

	/**
	 * Generic method for converting Json object (of type list) to a list of pojo.
	 * 
	 * @param List<T>
	 */
	public static <T> List<T> getMappedList(CloseableHttpResponse response, Class<T> classname) {
		StringBuffer result = new StringBuffer();
		ObjectMapper mapper = new ObjectMapper();
		result = readStringBuffer(response);
		if (result != null) {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, classname);
				List<T> myObjects = mapper.readValue(result.toString(), type);
				return myObjects;
			} catch (JsonGenerationException e) {
				logger.error(e.getMessage(), e);
			} catch (JsonMappingException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	public static <T> T getMappedObject(CloseableHttpResponse response, Class<T> classname) {
		StringBuffer result = new StringBuffer();
		ObjectMapper mapper = new ObjectMapper();
		result = readStringBuffer(response);
		if (result != null) {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			try {
				JavaType type = mapper.constructType(classname);
				T myObjects = mapper.readValue(result.toString(), type);
				return myObjects;
			} catch (JsonGenerationException e) {
				logger.error(e.getMessage(), e);
			} catch (JsonMappingException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * method for deserializing HttpResponse in a StringBuffer
	 */
	public static StringBuffer readStringBuffer(CloseableHttpResponse response) {
		if (response == null) {
			return null;
		} else {
			BufferedReader rd = null;
			try {
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			} catch (UnsupportedOperationException e2) {
				logger.error("Error on methode util.readStringBuffer()", e2);
			} catch (IOException e2) {
				logger.error("Error on methode util.readStringBuffer()", e2);
			}
			StringBuffer result = new StringBuffer();
			String line = "";
			try {
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
			} catch (IOException e2) {
				logger.error("Error on methode util.readStringBuffer()", e2);
			}
			return result;
		}
	}
	
	/**
	 * Add MRI info to Serie (MR series only) using the first DICOM file.
	 * @param rootDir
	 * @param serie
	 */
	public static void processSerieMriInfo(File rootDir, DicomTreeNode serie) {
		final String modality = serie.getDescriptionMap().get("modality");
		if (modality != null) {
			List<String> imageFileNames = ((Serie) serie).getFileNames();
			for (final Iterator<String> iteImages = imageFileNames.iterator(); iteImages.hasNext();) {
				String imageFileName = iteImages.next();
				if (!"PR".equals(modality) && !"SR".equals(modality)) {
					String imageFilePath = rootDir.toString() + File.separator + imageFileName;
					// create the MRI Object containing MRI information
					DicomObject dcmObj;
					DicomInputStream din = null;
					MRI mriInformation = new MRI();
					try {
						din = new DicomInputStream(new File(imageFilePath));
						dcmObj = din.readDicomObject();
						mriInformation.setInstitutionName(dcmObj.getString(Tag.InstitutionName));
						mriInformation.setInstitutionAddress(dcmObj.getString(Tag.InstitutionAddress));
						mriInformation.setStationName(dcmObj.getString(Tag.StationName));
						mriInformation.setManufacturer(dcmObj.getString(Tag.Manufacturer));
						mriInformation.setManufacturersModelName(dcmObj.getString(Tag.ManufacturerModelName));
						mriInformation.setDeviceSerialNumber(dcmObj.getString(Tag.DeviceSerialNumber));
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
						return;
					} finally {
						try {
							din.close();
						} catch (IOException ignore) {
						}
					}
					((Serie) serie).setMriInformation(mriInformation);
					return;
				}
			}
		}
	}

	public static boolean isHttpResponseValid(HttpResponse response) {
		int code = response.getCode();
		switch (code) {
		case 200:
			return true;
		case 204:
			return true;
		case 401:
			logger.warn("Error " + code + " : Rest Service is reachable but you are not authorized to access it.");
			return false;
		case 403:
			logger.warn("Error " + code + " : Rest Service is reachable but it is forbidden to access it.");
			return false;
		case 404:
			logger.warn("Error " + code + " : Rest Service is not reachable.");
			return false;
		case 500:
			logger.warn("Error " + code + " : Rest Service is not reachable.");
			return false;
		default:
			logger.warn("Error " + code + " : An error has occured.");
			return false;
		}
	}

	/**
	 * For OFSEP, do not transfer the real birth date but the first day of the year
	 *
	 * @return the date of the first day of the year
	 */
	public static Date getFirstDayOfTheYear(Date pBirthDate) {
		if (logger.isDebugEnabled()) {
			logger.debug("getFirstDayOfTheYear : Begin");
			logger.debug("getFirstDayOfTheYear : current subject birth date=" + pBirthDate);
		}

		if (pBirthDate != null) {
			final GregorianCalendar birthDate = new GregorianCalendar();
			birthDate.setTime(pBirthDate);
			// set day and month to 01/01
			birthDate.set(Calendar.MONTH, Calendar.JANUARY);
			birthDate.set(Calendar.DAY_OF_MONTH, 1);
			birthDate.set(Calendar.HOUR, 1);
			if (logger.isDebugEnabled()) {
				logger.debug("getFirstDayOfTheYear : anonymity birth date=" + birthDate.getTime());
				logger.debug("getFirstDayOfTheYear : End");
			}

			return birthDate.getTime();
		}

		logger.debug("getFirstDayOfTheYear : End - return null");

		return null;
	}

	/*
	 * Converts java.util.Date to javax.xml.datatype.XMLGregorianCalendar
	 */
	public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
		GregorianCalendar gCalendar = new GregorianCalendar();
		gCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		gCalendar.setTime(date);
		XMLGregorianCalendar xmlCalendar = null;
		try {
			xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCalendar);
		} catch (DatatypeConfigurationException ex) {
			logger.error("Error on methode util.XMLGregorianCalendar() Unable to convert date to XML GREGORIAN DATE",
					ex);
		}
		return xmlCalendar;
	}

	/*
	 * Converts XMLGregorianCalendar to java.util.Date in Java
	 */
	public static Date toDate(XMLGregorianCalendar calendar) {
		if (calendar == null) {
			return null;
		}
		return calendar.toGregorianCalendar().getTime();
	}
	
	public static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
	    return dateToConvert.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDate();
	}

	public static String generateErrorMessage(ResourceBundle resourceBundle, String message, boolean contactAdmin) {
		if (contactAdmin) {
			return "<html><body><p>" + resourceBundle.getString(message) + "</p><p><i>"
					+ resourceBundle.getString("shanoir.uploader.systemErrorDialog.contactAdmin")
					+ "</i></p></body></html>";
		} else {
			return "<html><body><p>" + resourceBundle.getString(message) + "</p></body></html>";
		}
	}

	/**
	 * Copy the contents of pseudonymus folder into .SU
	 * 
	 * @param folderName
	 */
	public static void copyPseudonymusFolder(String folderName) {
		logger.info("start " + folderName + " copy");
		final File destinationFolder = new File(ShUpConfig.shanoirUploaderFolder + File.separator + folderName);
		boolean propertiesExists = destinationFolder.exists();
		if (propertiesExists) {
			// do nothing
			logger.info(folderName + " folder already exists.");
		} else {
			try {
				// create pseudonymus folder
				destinationFolder.mkdirs();
				String[] folderContents = getResourceListing(ShanoirUploader.class,
						Pseudonymizer.PSEUDONYMUS_FOLDER + "/");
				for (int i = 0; i < folderContents.length; i++) {
					final File subDestinationFolder = new File(ShUpConfig.shanoirUploaderFolder + File.separator
							+ folderName + File.separator + folderContents[i]);
					// create folders inside pseudonymus folder
					subDestinationFolder.mkdir();
					// copy files
					String[] subFolderContents = getResourceListing(ShanoirUploader.class,
							Pseudonymizer.PSEUDONYMUS_FOLDER + "/" + folderContents[i] + "/");
					for (int j = 0; j < subFolderContents.length; j++) {
						final File destinationFolder2 = new File(
								ShUpConfig.shanoirUploaderFolder + File.separator + folderName + File.separator
										+ folderContents[i] + File.separator + subFolderContents[j]);
						copyFileFromJar(folderName + "/" + folderContents[i] + "/" + subFolderContents[j],
								destinationFolder2);
						logger.info(folderName + "/" + folderContents[i] + "/" + subFolderContents[j]
								+ " successfully copied");
					}
				}
				logger.info("end " + folderName + " copy");
			} catch (Exception e) {
				logger.error(folderName + " could not be copied: " + e.getMessage(), e);
			}
		}
	}

	/**
	 * List directory contents for a resource folder. Not recursive. This is
	 * basically a brute-force implementation. Works for regular files and also
	 * JARs.
	 * 
	 * @param clazz
	 *            Any java class that lives in the same place as the resources you
	 *            want.
	 * @param path
	 *            Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
		URL dirURL = clazz.getClassLoader().getResource(path);
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			/* A file path: easy enough */
			return new File(dirURL.toURI()).list();
		}

		if (dirURL == null) {
			/*
			 * In case of a jar file, we can't actually find a directory. Have to assume the
			 * same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip
																							// out
																							// only
																							// the
																							// JAR
																							// file
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries
															// in jar
			Set<String> result = new HashSet<String>(); // avoid duplicates in
														// case it is a
														// subdirectory
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(path)) { // filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0) {
						// if it is a subdirectory, we just return the directory
						// name
						entry = entry.substring(0, checkSubdir);
					}
					result.add(entry);
				}
			}
			return result.toArray(new String[result.size()]);
		}

		throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
	}

	public static void copyFileFromJar(final String fileName, final File file) {
		InputStream iS = null;
		FileOutputStream fOS = null;
		try {
			iS = Util.class.getResourceAsStream("/" + fileName);
			if (iS != null) {
				boolean created = file.createNewFile();
				if (created) {
					fOS = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					int len;
					while ((len = iS.read(buffer)) != -1) {
						fOS.write(buffer, 0, len);
					}
				}
			}
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			try {
				if (iS != null) {
					iS.close();
				}
				if (fOS != null) {
					fOS.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * This method copies the content of the dicom.server.properties in the .jar
	 * file to the same file in the user.home.
	 * 
	 * @param propertiesFile
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void encryptPasswordAndCopyPropertiesFile(File shanoirUploaderFolder, Properties propertyObject,
			String propertyFile, final String passwordPropertyName) {
		if (propertyObject.getProperty(passwordPropertyName) != null
				&& !propertyObject.getProperty(passwordPropertyName).equals("")) {
			propertyObject.setProperty(passwordPropertyName,
					ShUpConfig.encryption.cryptEncryptedString(propertyObject.getProperty(passwordPropertyName)));
		} else {
			propertyObject.setProperty(passwordPropertyName, "");
		}
		final File propertiesFile = new File(shanoirUploaderFolder + File.separator + propertyFile);
		OutputStream out;
		try {
			out = new FileOutputStream(propertiesFile);
			try {
				propertyObject.store(out, "Configuration");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			out.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
}
