package org.shanoir.uploader.service.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.shanoir.uploader.ShUpConfig;
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

	private static final Logger logger = LoggerFactory.getLogger(HttpService.class);

	private static ServiceConfiguration serviceConfiguration = ServiceConfiguration.getInstance();

	private static final String DEV_LOCAL = "https://shanoir-ng-nginx";

	private static final String NEURINFO_URL = "https://shanoir.irisa.fr";

	private static final String OFSEP_URL = "https://shanoir-ofsep.irisa.fr";
	
	private static final String CONTENT_TYPE_MULTIPART = "multipart/related";

	private static final String CONTENT_TYPE_DICOM = "application/dicom";

	private static final String BOUNDARY = "--import_dicom_shanoir--";

	private CloseableHttpClient httpClient;
	
	private HttpClientContext context;

	private static final String certsDirPath = System.getProperty(ShUpConfig.USER_HOME) + File.separator + ShUpConfig.SU + "_" 
												+ ShUpConfig.SHANOIR_UPLOADER_VERSION + File.separator + ShUpConfig.CERTS_FOLDER;

	public HttpService(String serverURL) {
		try {
			httpClient = buildHttpClient(serverURL);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void closeHttpClient() {
		try {
			httpClient.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public CloseableHttpResponse get(String url) throws Exception {
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			CloseableHttpResponse response = httpClient.execute(httpGet, context);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public CloseableHttpResponse post(String url, String json, boolean isLoginPost) throws Exception {
		try {
			HttpPost httpPost = new HttpPost(url);
			if (isLoginPost) {
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			} else {
				httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			}
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			CloseableHttpResponse response = httpClient.execute(httpPost, context);
			return response;				
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public CloseableHttpResponse postFile(String url, String tempDirId, File file) throws Exception {
		try {
			HttpPost httpPost = new HttpPost(url + "/" + tempDirId);
			httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addBinaryBody("file", file, ContentType.create("application/octet-stream"), file.getName());
			HttpEntity entity = builder.build();
			httpPost.setEntity(entity);
			CloseableHttpResponse response = httpClient.execute(httpPost, context);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public CloseableHttpResponse postFile(String url, File file) throws Exception {
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addBinaryBody("file", file, ContentType.create("application/octet-stream"), file.getName());
			HttpEntity entity = builder.build();
			httpPost.setEntity(entity);
			CloseableHttpResponse response = httpClient.execute(httpPost, context);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	public CloseableHttpResponse postFileMultipartRelated(String url, File file) throws Exception {
		try {
			MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setBoundary(BOUNDARY);
			multipartEntityBuilder.addBinaryBody("dcm_upload", file, ContentType.create(CONTENT_TYPE_DICOM), "filename");
			HttpEntity entity = multipartEntityBuilder.build();
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			httpPost.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_MULTIPART+";type="+CONTENT_TYPE_DICOM+";boundary="+BOUNDARY);
			httpPost.setEntity(entity);
			CloseableHttpResponse response = httpClient.execute(httpPost, context);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public CloseableHttpResponse put(String url, String json) throws Exception {
		try {
			HttpPut httpPut = new HttpPut(url);
			httpPut.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPut.setEntity(requestEntity);
			CloseableHttpResponse response = httpClient.execute(httpPut, context);
			return response;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
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
			logger.info("buildHttpClient: sslContextDev build.");
		}
		// In case of proxy: generate credentials provider with correct host
		HttpHost proxyHost = null;
		BasicCredentialsProvider credentialsProvider = null;
		if (serviceConfiguration.isProxyEnabled()) {
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
					logger.info("buildHttpClient: credentialsProvider build.");
					createHttpClientContext(proxyHost, credentialsProvider);
				}
				logger.info("buildHttpClient: proxyHost (host+port) build.");
			// Only host is configured, so do not set port
			} else if (serviceConfiguration.getProxyHost() != null) {
				proxyHost = new HttpHost(serviceConfiguration.getProxyHost());
				// user and password are additionally set
				if (serviceConfiguration.getProxyUser() != null && serviceConfiguration.getProxyPassword() != null) {
					credentialsProvider = new BasicCredentialsProvider();
					credentialsProvider.setCredentials(new AuthScope(proxyHost),
							new UsernamePasswordCredentials(serviceConfiguration.getProxyUser(),
									serviceConfiguration.getProxyPassword().toCharArray()));
					logger.info("buildHttpClient: credentialsProvider build.");
					createHttpClientContext(proxyHost, credentialsProvider);
				}
				logger.info("buildHttpClient: proxyHost (host) build.");
			} else {
				throw new Exception("Proxy enabled, but no host set or only port does not work.");
			}
		}
		return buildHttpClient(sslContextDev, proxyHost, credentialsProvider);
	}

	/**
	 * Create and assign a HttpContext necessary only for proxy authentication.
	 * 
	 * @param proxyHost
	 * @param credentialsProvider
	 */
	private void createHttpClientContext(HttpHost proxyHost, BasicCredentialsProvider credentialsProvider) {
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(proxyHost, basicAuth);
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credentialsProvider);
		context.setAuthCache(authCache);
		this.context = context;
		logger.info("createHttpClientContext: context created and assigned.");
	}

	/**
	 * This method builds a CloseableHttpClient depending if a special SSLContext
	 * for development or production is required, and if credentials should be used
	 * for the proxy.
	 * 
	 * @param sslContextDev
	 * @param credentialsProvider
	 * @return
	 * @throws Exception 
	 */
	private CloseableHttpClient buildHttpClient(final SSLContext sslContextDev, final HttpHost proxyHost, final BasicCredentialsProvider credentialsProvider) throws Exception {
		final SSLConnectionSocketFactory sslSocketFactory;
		if (sslContextDev != null) {
			sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
					.setSslContext(sslContextDev)
					.setTlsVersions(TLS.V_1_2)
					.build();
			logger.info("DEV SSLSocketFactory used.");
		} else {
			// We check the validity of the certificates in the certsDirPath
			// If the certificates are not valid, we download them
			try {
				checkCertificates(certsDirPath);
			} catch (Exception e) {
				logger.error("Error during certificate check: " + e.getMessage());
			}
			// Build SSLContext from certificates in certsDirPath
			SSLContext sslContextProd = buildSSLContextFromCertificates();
			sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
				.setSslContext(sslContextProd)
				.setHostnameVerifier(new CustomHostnameVerifier())
				.setTlsVersions(TLS.V_1_2)
				.build();
			logger.info("Standard SSLSocketFactory used with CustomHostnameVerifier.");
		}
		final HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
					.setMaxConnTotal(500)
					.setMaxConnPerRoute(500)
					.setSSLSocketFactory(sslSocketFactory)
					.build();
		if (proxyHost != null && credentialsProvider != null) {
			final DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
			final CloseableHttpClient httpClient = HttpClients.custom()
					.setConnectionManager(connectionManager)
					.setConnectionManagerShared(true)
					.setRoutePlanner(routePlanner)
					.setDefaultCredentialsProvider(credentialsProvider)
					.setProxy(proxyHost)
					.build();
			logger.info("CloseableHttpClient created with proxyHost: "
					+ proxyHost.getHostName() + ":" + proxyHost.getPort()
					+ " and credentialsProvider: " + credentialsProvider.toString() + ".");
			return httpClient;			
		} else {
			if (proxyHost != null) {
				final DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
				final CloseableHttpClient httpClient = HttpClients.custom()
						.setConnectionManager(connectionManager)
						.setConnectionManagerShared(true)
						.setRoutePlanner(routePlanner)
						.setProxy(proxyHost)
						.build();
				logger.info("CloseableHttpClient created with proxyHost: "
						+ proxyHost.getHostName() + ":" + proxyHost.getPort()
						+ " and without a credentialsProvider.");
				return httpClient;			
			} else {
				final CloseableHttpClient httpClient = HttpClients.custom()
						.setConnectionManager(connectionManager)
						.setConnectionManagerShared(true)
						.build();
				logger.info("CloseableHttpClient created without proxyHost"
						+ " and without a credentialsProvider.");
				return httpClient;			
			}		
		}
	}

	// TODO : check if certificates already downloaded and if still valid to avoid downloading them everytime
	private static void checkCertificates(String certsDirPath) throws Exception {
	File certsDir = new File(certsDirPath);
        if (!certsDir.exists()) {
            certsDir.mkdirs();
        } else {
			for (File file : certsDir.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".pem")) {
					try (FileInputStream fis = new FileInputStream(file.getAbsolutePath())) {
            			CertificateFactory cf = CertificateFactory.getInstance("X.509");
            			X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);

            			cert.checkValidity();

        			} catch (Exception e) {
            			logger.error("Error during HTTPS certificate validity check : " + e.getMessage());
						// if certificate is not valid, we delete it
						file.delete();
						// we download new certificates
						List<String> urls = List.of(NEURINFO_URL, OFSEP_URL);
						downloadCerts(urls);
					}
				}
			}
		}
	}

	private static void downloadCerts(List<String> urls) throws Exception {

        for (String httpsUrl : urls) {
            try {
                logger.info("Getting java certificate from " + httpsUrl); // to delete afterwards
				URL url = new URL(httpsUrl);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.connect();

                Certificate[] certs = conn.getServerCertificates();

                int certIndex = 0;
                for (Certificate cert : certs) {
                    if (cert instanceof X509Certificate) {
                        X509Certificate x509 = (X509Certificate) cert;

                        // Renaming the file based on the host and certificate index
                        String host = url.getHost().replaceAll("[^a-zA-Z0-9.-]", "_");
                        String filename = String.format("%s/cert_%s_%d.pem", certsDirPath, host, certIndex++);

                        // Writing the file
                        try (FileWriter writer = new FileWriter(filename)) {
                            writer.write("-----BEGIN CERTIFICATE-----\n");
                            writer.write(Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(x509.getEncoded()));
                            writer.write("\n-----END CERTIFICATE-----\n");
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        System.out.println("Certificate saved : " + filename);
                        System.out.println("   ↳ Expires : " + sdf.format(x509.getNotAfter()));
                    }
                }

            } catch (Exception e) {
                System.err.println("Error on " + httpsUrl + " : " + e.getMessage());
            }
        }
	}

	private static SSLContext buildSSLContextFromCertificates() throws Exception {
		List<String> pemFilePaths = new ArrayList<>();
		// We get all the .pem  files located in certsDirPath
		for (File file : new File(certsDirPath).listFiles()) {
			if (file.isFile() && file.getName().endsWith(".pem")) {
				pemFilePaths.add(file.getAbsolutePath());
			}
		}
        // We create an empty keystore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null); // initialise à vide

        // Instanciate a CertificateFactory for X.509
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        int i = 0;
        for (String pemPath : pemFilePaths) {
            try (InputStream in = new FileInputStream(pemPath)) {
                Certificate cert = certFactory.generateCertificate(in);
                keyStore.setCertificateEntry("cert" + i, cert);
                i++;
            }
        }

        // Creating TrustManager from keystore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // Creating SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

}
