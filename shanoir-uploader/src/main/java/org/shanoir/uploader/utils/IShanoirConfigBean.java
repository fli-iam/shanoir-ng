package org.shanoir.uploader.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * The Interface IShanoirConfigBean.
 */
public interface IShanoirConfigBean {

	/**
	 * Convert FALSE to 'false', TRUE to 'true', null to ''.
	 *
	 * @param bool
	 *            the bool
	 *
	 * @return the string
	 */
	public String booleanObjectToString(final Boolean bool);

	/**
	 * Format.
	 *
	 * @param date
	 *            the date
	 *
	 * @return the string
	 */
	String format(final Date date);

	/**
	 * Format.
	 *
	 * @param date the date
	 * @param dateFormat the format
	 * @return the string
	 */
	String format(final Date date, final String dateFormat);

	/**
	 * Format dicom format.
	 *
	 * @param date
	 *            the date
	 *
	 * @return the string
	 */
//	String convertDicomDateToString(final Date date);

	/**
	 * Format a duration into the format XXh:XXm:XXs.
	 *
	 * @param duration
	 *            the duration
	 *
	 * @return the duration formatted as XXh:XXm:XXs
	 */
	String formatDuration(final Double duration);

	/**
	 * Format with hour.
	 *
	 * @param date
	 *            the date
	 *
	 * @return the string
	 */
	String formatWithHour(final Date date);

	/**
	 * Gets the administrator mail.
	 *
	 * @return the administrator mail
	 */
	String getAdministratorMail();

	/**
	 * Gets the anonymization tags.
	 *
	 * @return the anonymization tags
	 */
	HashMap<Integer, String> getAnonymizationTags();

	/**
	 * Gets the available locales.
	 *
	 * @param event
	 *            the event
	 *
	 * @return the available locales
	 */
	List<Locale> getAvailableLocales(final Object event);

	/**
	 * Return the backup dicom server administrator email.
	 *
	 * @return the backup dicom server administrator email
	 */
	String getBackupDicomServerAdministratorEmail();

	/**
	 * Return the backup Dicom server called AET.
	 *
	 * @return the backup dicom server called aet
	 */
	String getBackupDicomServerCalledAET();

	/**
	 * Return the backup dicom server host.
	 *
	 * @return the backup dicom server host
	 */
	String getBackupDicomServerHost();

	/**
	 * Return the file path or URL of JKS truststore.
	 *
	 * @return the backup dicom server key store password
	 */
	String getBackupDicomServerKeyStorePassword();

	/**
	 * Return the password for keystore file, 'dcm4chee' by default".
	 *
	 * @return the backup dicom server key store url
	 */
	String getBackupDicomServerKeyStoreURL();

	/**
	 * Return the backup dicom server port.
	 *
	 * @return the backup dicom server port
	 */
	int getBackupDicomServerPort();

	/**
	 * Return the backup Dicom server protocol.
	 *
	 * @return the backup dicom server protocol
	 */
	String getBackupDicomServerProtocol();

	/**
	 * Return password for truststore file, 'dcm4chee' by default.
	 *
	 * @return the backup dicom server trust store password
	 */
	String getBackupDicomServerTrustStorePassword();

	/**
	 * Return file path or URL of JKS truststore.
	 *
	 * @return the backup dicom server trust store url
	 */
	String getBackupDicomServerTrustStoreURL();

	/**
	 * Return the backup PACS web port.
	 *
	 * @return the pacs web port
	 */
	int getBackupDicomServerWebPort();

	/**
	 * Gets the date format.
	 *
	 * @return the date format
	 */
	String getDateFormat();

	/**
	 * Gets the date format with hour.
	 *
	 * @return the date format with hour
	 */
	String getDateFormatWithHour();

	/**
	 * Gets the default list max size.
	 *
	 * @return the default list max size
	 */
	int getDefaultListMaxSize();

	/**
	 * Gets the institute address.
	 *
	 * @return the institute address
	 */
	String getInstituteAddress();

	/**
	 * Gets the institute name.
	 *
	 * @return the institute name
	 */
	String getInstituteName();

	/**
	 * Return the local Dicom server calling AET.
	 *
	 * @return the local dicom server calling aet
	 */
	String getLocalDicomServerCallingAET();

	/**
	 * Return the local dicom server host.
	 *
	 * @return the local dicom server host
	 */
	String getLocalDicomServerHost();

	/**
	 * Return the local dicom server port.
	 *
	 * @return the local dicom server port
	 */
	int getLocalDicomServerPort();

	/**
	 * Return the lookup dicom server administrator email.
	 *
	 * @return the lookup dicom server administrator email
	 */
	String getLookupDicomServerAdministratorEmail();

	/**
	 * Return the lookup Dicom server called AET.
	 *
	 * @return the lookup dicom server called aet
	 */
	String getLookupDicomServerCalledAET();

	/**
	 * Return the lookup dicom server host.
	 *
	 * @return the lookup dicom server host
	 */
	String getLookupDicomServerHost();

	/**
	 * Return the file path or URL of JKS truststore.
	 *
	 * @return the lookup dicom server key store password
	 */
	String getLookupDicomServerKeyStorePassword();

	/**
	 * Return the password for keystore file, 'dcm4chee' by default".
	 *
	 * @return the lookup dicom server key store url
	 */
	String getLookupDicomServerKeyStoreURL();

	/**
	 * Return the lookup dicom server port.
	 *
	 * @return the lookup dicom server port
	 */
	int getLookupDicomServerPort();

	/**
	 * Return the lookup Dicom server protocol.
	 *
	 * @return the lookup dicom server protocol
	 */
	String getLookupDicomServerProtocol();

	/**
	 * Return password for truststore file, 'dcm4chee' by default.
	 *
	 * @return the lookup dicom server trust store password
	 */
	String getLookupDicomServerTrustStorePassword();

	/**
	 * Return file path or URL of JKS truststore.
	 *
	 * @return the lookup dicom server trust store url
	 */
	String getLookupDicomServerTrustStoreURL();

	/**
	 * Return the lookup PACS web port.
	 *
	 * @return the pacs web port
	 */
	int getLookupDicomServerWebPort();

	/**
	 * Gets the message.
	 *
	 * @param key
	 *            the key
	 *
	 * @return the message
	 */
	String getMessage(final String key);

	/**
	 * Get the System Numerical Variable Max Value.
	 *
	 * @return a float
	 */
	Float getNumericalVariableMaxValue();

	/**
	 * Get the System Numerical Variable Min Value.
	 *
	 * @return a float
	 */
	Float getNumericalVariableMinValue();

	/**
	 * Return the server address.
	 *
	 * @return the server address
	 */
	String getServerAddress();

	/**
	 * Gets the web site.
	 *
	 * @return the web site
	 */
	String getWebSite();

	/**
	 * True if ciphering and certificate TLS/3DES enabled.
	 *
	 * @return true, if checks if is dicom server enable tls3des
	 */
	boolean isBackupDicomServerEnableTls3des();

	/**
	 * When importing DICOM from CD/DVD, selecting a serie makes the application
	 * to generate a thumbnail of the serie.
	 *
	 * @return true or false
	 */
	boolean isDisplayDicomThumbnail();

	/**
	 * True if ciphering and certificate TLS/3DES enabled.
	 *
	 * @return true, if checks if is dicom server enable tls3des
	 */
	boolean isLookupDicomServerEnableTls3des();

	/**
	 * Checks if is to be anonymized.
	 *
	 * @return true, if is to be anonymized
	 */
	boolean isToBeAnonymized();

	/**
	 * Lower first letter.
	 *
	 * @param string
	 *            the string
	 *
	 * @return the string
	 */
	String lowerFirstLetter(final String string);

	/**
	 * Lower last letter.
	 *
	 * @param string
	 *            the string
	 *
	 * @return the string
	 */
	String lowerLastLetter(final String string);

	/**
	 * Return false.
	 *
	 * @return the boolean
	 */
	Boolean returnFalse();

	/**
	 * Return null.
	 *
	 * @return the boolean
	 */
	Boolean returnNull();

	/**
	 * Return true.
	 *
	 * @return the boolean
	 */
	Boolean returnTrue();

	/**
	 * Assuming the string contains white spaces, it returns the part of the word befaore the first white space.
	 *
	 * @param string the string
	 * @return the first part
	 */
	String getFirstPart(final String string);


	/**
	 * Checks if timepoints module is activated
	 *
	 * @return true, if is activated
	 */
	boolean isHasTimepointActivated();
	
	/**
	 * Check if solr search functionnality is activated or not
	 *
	 * @return true or false
	 * */
	boolean isHasSolrSearchActivated();
	
	/**
	 * Check if IAM Cloud functionnality is activated or not
	 *
	 * @return true or false
	 * */
	boolean isHasIamCloudActivated();
	
	/**
	 * Check if data transfer module is activated or not
	 *
	 * @return true or false
	 * */
	boolean isHasDataTransferActivated();
	
	/**
	 * Check if carmin widget module is activated or not
	 *
	 * @return true or false
	 * */
	boolean isHasCarminWidgetActivated();
	
	/**
	 * Check if is OFSEP or not
	 *
	 * @return true or false
	 * */
	boolean isOfsep();

	/**
	 * Checks if writing dicom star module is activated
	 *
	 * @return true, if is activated
	 */
	boolean isWriteDicomStar();
	
	/**
	 * @return the sorlUrlForJsConfig
	 */
	String getSolrUrlForJsConfig(); 
}
