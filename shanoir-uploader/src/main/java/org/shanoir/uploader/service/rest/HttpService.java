package org.shanoir.uploader.service.rest;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps the usage of Apache HttpClient, currently 5.1.
 * 
 * In case of development environments with self-signed certificates a special
 * SocketFactory is used, that avoid the below exception:
 * sun.security.provider.certpath.SunCertPathBuilderException: unable to find
 * valid certification path to requested target And as even the import of the
 * certificate did not bring a solution, HttpService creates a socketFactory in
 * the constructor, that "solves" the certificate issue for testing/development
 * purpose only.
 * 
 * The SocketFactory is only used in case of "https://shanoir-ng-nginx" is
 * present in the URL.
 * 
 * @author mkain
 *
 */
public class HttpService {

	private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);

	private static ServiceConfiguration serviceConfiguration = ServiceConfiguration.getInstance();

	private static final String DEV_LOCAL = "https://shanoir-ng-nginx";

	private CloseableHttpClient httpClient;

	public HttpService(String serverURL) {
		try {
			httpClient = buildHttpClient(serverURL);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public void closeHttpClient() {
		try {
			httpClient.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public CloseableHttpResponse get(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			CloseableHttpResponse response = httpClient.execute(httpGet);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public CloseableHttpResponse post(String url, String json, boolean isLoginPost) {
		try {
			HttpPost httpPost = new HttpPost(url);
			if (isLoginPost) {
				httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
			} else {
				httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			}
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			return response;				
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public CloseableHttpResponse postFile(String url, String tempDirId, File file) {
		try {
			HttpPost httpPost = new HttpPost(url + tempDirId);
			httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			// for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			// File file = (File) iterator.next();
			builder.addBinaryBody("file", file, ContentType.create("application/octet-stream"), file.getName());
			// }
			HttpEntity entity = builder.build();
			httpPost.setEntity(entity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public CloseableHttpResponse put(String url, String json) {
		try {
			HttpPut httpPut = new HttpPut(url);
			httpPut.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPut.setEntity(requestEntity);
			CloseableHttpResponse response = httpClient.execute(httpPut);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	private CloseableHttpClient buildHttpClient(String url) throws Exception {
		SSLContext sslContextDev = null;
		if (url.equals(DEV_LOCAL)) {
			// Create special SSLContext for local development server
			sslContextDev = SSLContexts.custom()
					.loadTrustMaterial(new TrustStrategy() {
						@Override
						public boolean isTrusted(final X509Certificate[] chain, final String authType)
								throws CertificateException {
							return true;
						}
			}).build();
		}
		// In case of proxy: generate credentials provider with correct host
		HttpHost proxyHost = null;
		BasicCredentialsProvider credentialsProvider = null;
		if (serviceConfiguration.isProxyEnable()) {
			// Host and port are given
			if (serviceConfiguration.getProxyHost() != null && serviceConfiguration.getProxyPort() != null) {
				proxyHost = new HttpHost(serviceConfiguration.getProxyHost(),
						Integer.valueOf(serviceConfiguration.getProxyPort()));
				// user and password are additionally set
				if (serviceConfiguration.getProxyUser() != null && serviceConfiguration.getProxyPassword() != null) {
					credentialsProvider = new BasicCredentialsProvider();
					credentialsProvider.setCredentials(new AuthScope(proxyHost),
							new UsernamePasswordCredentials(serviceConfiguration.getProxyUser(),
									serviceConfiguration.getProxyPassword().toCharArray()));
				}
			// Only host is configured, so do not set port
			} else if (serviceConfiguration.getProxyHost() != null) {
				proxyHost = new HttpHost(serviceConfiguration.getProxyHost());
				// user and password are additionally set
				if (serviceConfiguration.getProxyUser() != null && serviceConfiguration.getProxyPassword() != null) {
					credentialsProvider = new BasicCredentialsProvider();
					credentialsProvider.setCredentials(new AuthScope(proxyHost),
							new UsernamePasswordCredentials(serviceConfiguration.getProxyUser(),
									serviceConfiguration.getProxyPassword().toCharArray()));
				}
			} else {
				throw new Exception("Proxy enabled, but no host set or only port does not work.");
			}
		}
		return buildHttpClient(sslContextDev, proxyHost, credentialsProvider);
	}

	/**
	 * This method builds a CloseableHttpClient depending if a special SSLContext
	 * for development or production is required, and if credentials should be used
	 * for the proxy.
	 * 
	 * @param sslContextDev
	 * @param credentialsProvider
	 * @return
	 * @throws IOException
	 */
	private CloseableHttpClient buildHttpClient(final SSLContext sslContextDev, final HttpHost proxyHost, final BasicCredentialsProvider credentialsProvider) throws IOException {
		final SSLConnectionSocketFactory sslSocketFactory;
		if (sslContextDev != null) {
			sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
					.setSslContext(sslContextDev)
					.setTlsVersions(TLS.V_1_2)
					.build();
			LOG.info("DEV SSLSocketFactory used.");
		} else {
			sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
					.setHostnameVerifier(new CustomHostnameVerifier())
					.setTlsVersions(TLS.V_1_2)
					.build();
			LOG.info("Standard SSLSocketFactory used with CustomHostnameVerifier.");
		}
		final HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
					.setSSLSocketFactory(sslSocketFactory)
					.build();
		if (proxyHost != null && credentialsProvider != null) {
			final CloseableHttpClient httpClient = HttpClients.custom()
					.setConnectionManager(connectionManager)
					.setConnectionManagerShared(true)
					.setDefaultCredentialsProvider(credentialsProvider)
					.setProxy(proxyHost)
					.build();
			LOG.info("CloseableHttpClient created with proxyHost: "
					+ proxyHost.getHostName() + ":" + proxyHost.getPort()
					+ " and credentialsProvider: " + credentialsProvider.toString() + ".");
			return httpClient;			
		} else {
			if (proxyHost != null) {
				final CloseableHttpClient httpClient = HttpClients.custom()
						.setConnectionManager(connectionManager)
						.setConnectionManagerShared(true)
						.setProxy(proxyHost)
						.build();
				LOG.info("CloseableHttpClient created with proxyHost: "
						+ proxyHost.getHostName() + ":" + proxyHost.getPort()
						+ " and without a credentialsProvider.");
				return httpClient;			
			} else {
				final CloseableHttpClient httpClient = HttpClients.custom()
						.setConnectionManager(connectionManager)
						.setConnectionManagerShared(true)
						.build();
				LOG.info("CloseableHttpClient created without proxyHost"
						+ " and without a credentialsProvider.");
				return httpClient;			
			}		
		}
	}

}
