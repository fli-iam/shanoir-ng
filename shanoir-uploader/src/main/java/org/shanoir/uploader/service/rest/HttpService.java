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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps the usage of Apache HttpClient, currently 4.3.1.
 * 
 * As with https://shanoir-qualif.irisa.fr ShUp did not work because of an exception:
 * sun.security.provider.certpath.SunCertPathBuilderException:
 * unable to find valid certification path to requested target
 * And as even the import of the certificate did not bring a solution,
 * HttpService creates a socketFactory in the constructor, that "solves"
 * the certificate issue for testing purpose only.
 * 
 * The SocketFactory is only used in case of "-qualif" is present in the URL.
 * 
 * @author mkain
 *
 */
public class HttpService {

	private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
	
	private static final String QUALIF = "-qualif";

	// only used for qualif environments, not for prod
	private SSLConnectionSocketFactory socketFactory;
	
	/**
	 * Initiates a SocketFactory only for qualif testing.
	 */
	public HttpService() {
		TrustManager[] trustManager = new TrustManager[] {
			    new X509TrustManager() {
			       public X509Certificate[] getAcceptedIssuers() {
			           return new X509Certificate[0];
			       }
			       public void checkClientTrusted(X509Certificate[] certificate, String str) {}
			       public void checkServerTrusted(X509Certificate[] certificate, String str) {}
			    }
			};
		try {
			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(null, trustManager, new SecureRandom());
			socketFactory = new SSLConnectionSocketFactory(context,
			        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} catch (NoSuchAlgorithmException e) {
			LOG.error(e.getMessage(), e);
		} catch (KeyManagementException e) {
			LOG.error(e.getMessage(), e);
		}
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

	public HttpResponse post(String url, String json) {
		try {
			HttpClient httpClient = buildHttpClient(url);
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getTokenString());
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			HttpResponse	 response = httpClient.execute(httpPost);
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
//			for (Iterator iterator = files.iterator(); iterator.hasNext();) {
//				File file = (File) iterator.next();
				builder.addBinaryBody("file", file, ContentType.create("application/octet-stream"), file.getName());				
//			}
			HttpEntity entity = builder.build();
			httpPost.setEntity(entity);
			HttpResponse	 response = httpClient.execute(httpPost);
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
			HttpResponse	 response = httpClient.execute(httpPut);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	private CloseableHttpClient buildHttpClient(String url) {
		if (url.contains(QUALIF)) {
			return HttpClientBuilder.create().setSSLSocketFactory(socketFactory).build();			
		} else {
			return HttpClientBuilder.create().build();
		}
	}

}
