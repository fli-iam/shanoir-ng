package org.shanoir.uploader.utils;

import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.shanoir.uploader.service.http.ProxyAuthenticator;
import org.shanoir.uploader.service.wsdl.ServiceConfiguration;

/**
 * 
 * @author atouboul
 * 
 *         This util class is used in order to initialize, set and test the
 *         proxy.
 *
 */
public class ProxyUtil {

	private static Logger logger = Logger.getLogger(Util.class);

	private static ServiceConfiguration serviceConfiguration = ServiceConfiguration.getInstance();

	public static void initializeSystemProxy() {
		if (serviceConfiguration.getTlsProtocol() != null && !serviceConfiguration.getTlsProtocol().equals("")) {
			System.setProperty("https.protocols", serviceConfiguration.getTlsProtocol());
		}
		if (serviceConfiguration.getTlsCypherSuite() != null) {
			System.setProperty("https.cipherSuites", serviceConfiguration.getTlsCypherSuite());
		}

		if (serviceConfiguration.isProxyEnable()) {
			if ((serviceConfiguration.getProxyUser() != null) && (serviceConfiguration.getProxyPassword() != null)) {
				Authenticator.setDefault(new ProxyAuthenticator(serviceConfiguration.getProxyUser(),
						serviceConfiguration.getProxyPassword()));
			} else {
				Authenticator.setDefault(null);
			}

			if (serviceConfiguration.isProxySecure()) {
				System.clearProperty("http.proxyHost");
				System.clearProperty("http.proxyPort");
				System.clearProperty("http.proxyUser");
				System.clearProperty("http.proxyPassword");
				if (serviceConfiguration.getProxyHost() != null) {
					System.setProperty("https.proxyHost", serviceConfiguration.getProxyHost());
				}
				if (serviceConfiguration.getProxyPort() != null) {
					System.setProperty("https.proxyPort", serviceConfiguration.getProxyPort());
				}
				if (serviceConfiguration.getProxyUser() != null) {
					System.setProperty("https.proxyUser", serviceConfiguration.getProxyUser());
				}
				if (serviceConfiguration.getProxyPassword() != null) {
					System.setProperty("https.proxyPassword", serviceConfiguration.getProxyPassword());
				}
			} else {
				System.clearProperty("https.proxyHost");
				System.clearProperty("https.proxyPort");
				System.clearProperty("https.proxyUser");
				System.clearProperty("https.proxyPassword");

				if (serviceConfiguration.getProxyHost() != null) {
					System.setProperty("http.proxyHost", serviceConfiguration.getProxyHost());
				}
				if (serviceConfiguration.getProxyPort() != null) {
					System.setProperty("http.proxyPort", serviceConfiguration.getProxyPort());
				}
				if (serviceConfiguration.getProxyUser() != null) {
					System.setProperty("http.proxyUser", serviceConfiguration.getProxyUser());
				}
				if (serviceConfiguration.getProxyPassword() != null) {
					System.setProperty("http.proxyPassword", serviceConfiguration.getProxyPassword());
				}
			}
		} else {
			Authenticator.setDefault(null);
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
			System.clearProperty("https.proxyUser");
			System.clearProperty("https.proxyPassword");
			System.clearProperty("http.proxyHost");
			System.clearProperty("http.proxyPort");
			System.clearProperty("http.proxyUser");
			System.clearProperty("http.proxyPassword");
		}
	}

	public static int testProxy() {
		try {
			URL url = new URL(serviceConfiguration.getTestURL());
			HttpURLConnection myURLConnection = (HttpURLConnection) url.openConnection();
			myURLConnection.setRequestMethod("GET");
			myURLConnection.setConnectTimeout(2000);
			myURLConnection.setReadTimeout(2000);
			myURLConnection.connect();
			int code = myURLConnection.getResponseCode();
			return code;
		} catch (java.net.SocketTimeoutException e) {
			logger.error(
					"Unable to contact following url for proxy testing purpose : " + serviceConfiguration.getTestURL(),
					e);
			return -2;
		} catch (Exception e) {
			logger.error(
					"Unable to contact following url for proxy testing purpose : " + serviceConfiguration.getTestURL(),
					e);

			/**
			 * TODO ATO manage possible error java.net.UnknownHostException when host is not
			 * reachable java.net.ConnectException host is reachable but no service
			 * answering if (e instanceof UnknownHostException) { return -X; //where is an
			 * int. } etc..
			 */
			return -1;
		}
	}

	public static void setProxyForBinding(BindingProvider bindingProvider) {
		if (serviceConfiguration.isProxyEnable()) {
			if (serviceConfiguration.isProxySecure()) {
				if (serviceConfiguration.getProxyHost() != null) {
					bindingProvider.getRequestContext().put("https.proxyHost", serviceConfiguration.getProxyHost());
				}
				if (serviceConfiguration.getProxyPort() != null) {
					bindingProvider.getRequestContext().put("https.proxyPort", serviceConfiguration.getProxyPort());
				}
				if (serviceConfiguration.getProxyUser() != null) {
					bindingProvider.getRequestContext().put("https.proxyUser", serviceConfiguration.getProxyUser());
				}
				if (serviceConfiguration.getProxyPassword() != null) {
					bindingProvider.getRequestContext().put("https.proxyPassword",
							serviceConfiguration.getProxyPassword());
				}
			} else {
				if (serviceConfiguration.getProxyHost() != null) {
					bindingProvider.getRequestContext().put("http.proxyHost", serviceConfiguration.getProxyHost());
				}
				if (serviceConfiguration.getProxyPort() != null) {
					bindingProvider.getRequestContext().put("http.proxyPort", serviceConfiguration.getProxyPort());
				}
				if (serviceConfiguration.getProxyUser() != null) {
					bindingProvider.getRequestContext().put("http.proxyUser", serviceConfiguration.getProxyUser());
				}
				if (serviceConfiguration.getProxyPassword() != null) {
					bindingProvider.getRequestContext().put("http.proxyPassword",
							serviceConfiguration.getProxyPassword());
				}
			}
		} else {
			try {
				if (bindingProvider.getRequestContext().get("https.proxyHost") != null)
					bindingProvider.getRequestContext().remove("https.proxyHost");
				if (bindingProvider.getRequestContext().get("https.proxyPort") != null)
					bindingProvider.getRequestContext().remove("https.proxyPort");
				if (bindingProvider.getRequestContext().get("https.proxyUser") != null)
					bindingProvider.getRequestContext().remove("https.proxyUser");
				if (bindingProvider.getRequestContext().get("https.proxyPassword") != null)
					bindingProvider.getRequestContext().remove("https.proxyPassword");
				if (bindingProvider.getRequestContext().get("http.proxyHost") != null)
					bindingProvider.getRequestContext().remove("http.proxyHost");
				if (bindingProvider.getRequestContext().get("http.proxyPort") != null)
					bindingProvider.getRequestContext().remove("http.proxyPort");
				if (bindingProvider.getRequestContext().get("http.proxyUser") != null)
					bindingProvider.getRequestContext().remove("http.proxyUser");
				if (bindingProvider.getRequestContext().get("http.proxyPassword") != null)
					bindingProvider.getRequestContext().remove("http.proxyPassword");
			} catch (Exception e) {
				logger.error("fail to change binding provider", e);
			} finally {

			}
		}
	}

}
