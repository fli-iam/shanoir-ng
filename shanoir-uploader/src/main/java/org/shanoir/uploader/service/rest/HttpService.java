package org.shanoir.uploader.service.rest;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.soap.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps the usage of Apache HttpClient, currently 4.3.1.
 * 
 * In case of development environments with self-signed certificates a special
 * SocketFactory is used, that avoid the below exception:
 * sun.security.provider.certpath.SunCertPathBuilderException: unable to find
 * valid certification path to requested target And as even the import of the
 * certificate did not bring a solution, HttpService creates a socketFactory in
 * the constructor, that "solves" the certificate issue for testing/development
 * purpose only.
 * 
 * The SocketFactory is only used in case of "-dev" or "shanoir-ng-nginx" is
 * present in the URL.
 * 
 * @author mkain
 *
 */
public class HttpService {

	private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
	
	private static ServiceConfiguration serviceConfiguration = ServiceConfiguration.getInstance();

	private static final String DEV_SERVER = "-dev";

	private static final String DEV_LOCAL = "shanoir-ng-nginx";

	// only used for dev environments, not for prod
	private SSLConnectionSocketFactory socketFactoryDevEnv;

	/**
	 * Initiates a SocketFactory only for qualif testing.
	 */
	public HttpService() {
		try {
			TrustManager[] trustManager = initWeakTrustManager();
			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(null, trustManager, new SecureRandom());
			socketFactoryDevEnv = new SSLConnectionSocketFactory(context,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e.getMessage(), e);
		} catch (KeyManagementException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private TrustManager[] initWeakTrustManager() {
		TrustManager[] trustManager = new TrustManager[] {
			new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
	
				public void checkClientTrusted(X509Certificate[] certificate, String str) {
				}
	
				public void checkServerTrusted(X509Certificate[] certificate, String str) {
				}
			}
		};
		return trustManager;
	}

	public HttpResponse get(String url) {
		try {
			HttpClient httpClient = buildHttpClient(url);
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			HttpResponse response = httpClient.execute(httpGet);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public HttpResponse post(String url, String json, boolean isLoginPost) {
		try {
			HttpClient httpClient = buildHttpClient(url);
			HttpPost httpPost = new HttpPost(url);
			if (isLoginPost) {
				httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
			} else {
				httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());				
			}
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			HttpResponse response = httpClient.execute(httpPost);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public HttpResponse postFile(String url, String tempDirId, File file) {
		try {
			HttpClient httpClient = buildHttpClient(url);
			HttpPost httpPost = new HttpPost(url + tempDirId);
			httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			// for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			// File file = (File) iterator.next();
			builder.addBinaryBody("file", file, ContentType.create("application/octet-stream"), file.getName());
			// }
			HttpEntity entity = builder.build();
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public HttpResponse put(String url, String json) {
		try {
			HttpClient httpClient = buildHttpClient(url);
			HttpPut httpPut = new HttpPut(url);
			httpPut.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPut.setEntity(requestEntity);
			HttpResponse response = httpClient.execute(httpPut);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	private CloseableHttpClient buildHttpClient(String url) throws Exception {
		if (serviceConfiguration.isProxyEnable()) {
			HttpHost proxy = null;
			CredentialsProvider credsProvider = null;
			// Host and port are given
			if (serviceConfiguration.getProxyHost() != null && serviceConfiguration.getProxyPort() != null) {
				proxy = new HttpHost(serviceConfiguration.getProxyHost(), Integer.valueOf(serviceConfiguration.getProxyPort()));
				// user and password are additionally set
				if (serviceConfiguration.getProxyUser() != null && serviceConfiguration.getProxyPassword() != null) {
					credsProvider = new BasicCredentialsProvider();
				    credsProvider.setCredentials(
				            new AuthScope(proxy),
				            new UsernamePasswordCredentials(serviceConfiguration.getProxyUser(), serviceConfiguration.getProxyPassword()));
				}
			// Only host is configured, so do not set port
			} else if (serviceConfiguration.getProxyHost() != null) {
				proxy = new HttpHost(serviceConfiguration.getProxyHost());
				// user and password are additionally set
				if (serviceConfiguration.getProxyUser() != null && serviceConfiguration.getProxyPassword() != null) {
					credsProvider = new BasicCredentialsProvider();
				    credsProvider.setCredentials(
				            new AuthScope(proxy),
				            new UsernamePasswordCredentials(serviceConfiguration.getProxyUser(), serviceConfiguration.getProxyPassword()));
				}
			} else {
				throw new Exception("Proxy enabled, but no host set or only port does not work.");
			}
			if (proxy != null && credsProvider != null) {
				if (url.contains(DEV_SERVER) || url.contains(DEV_LOCAL)) {
					return HttpClientBuilder.create()
							.setSSLSocketFactory(socketFactoryDevEnv)
							.setDefaultCredentialsProvider(credsProvider)
							.setProxy(proxy).build();
				} else {
					// the below code solves the GitHub issue: https://github.com/fli-iam/shanoir-ng/issues/582,
					// as Apache HttpClient does not per default use the HostnameVerifier from HttpsURLConnection (JDK/JRE)
					return HttpClientBuilder.create()
							.setHostnameVerifier(new CustomHostnameVerifier())
							.setDefaultCredentialsProvider(credsProvider)
							.setProxy(proxy).build();
				}
			} else if (proxy != null) {
				if (url.contains(DEV_SERVER) || url.contains(DEV_LOCAL)) {
					return HttpClientBuilder.create()
							.setSSLSocketFactory(socketFactoryDevEnv)
							.setProxy(proxy).build();
				} else {
					// the below code solves the GitHub issue: https://github.com/fli-iam/shanoir-ng/issues/582,
					// as Apache HttpClient does not per default use the HostnameVerifier from HttpsURLConnection (JDK/JRE)
					return HttpClientBuilder.create()
							.setHostnameVerifier(new CustomHostnameVerifier())
							.setProxy(proxy).build();
				}
			}
		/**
		 * No proxy case:
		 */
		} else {
			if (url.contains(DEV_SERVER) || url.contains(DEV_LOCAL)) {
				return HttpClientBuilder.create().setSSLSocketFactory(socketFactoryDevEnv).build();
			} else {
				// the below code solves the GitHub issue: https://github.com/fli-iam/shanoir-ng/issues/582,
				// as Apache HttpClient does not per default use the HostnameVerifier from HttpsURLConnection (JDK/JRE)
				return HttpClientBuilder.create().setHostnameVerifier(new CustomHostnameVerifier()).build();
			}
		}
		return null;
	}

}
