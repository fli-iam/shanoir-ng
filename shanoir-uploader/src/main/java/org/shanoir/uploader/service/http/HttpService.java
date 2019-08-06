package org.shanoir.uploader.service.http;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
* http service use to query and post data through rest services to shanoir ng webapp.
*
* @author atouboul
*
*/

public class HttpService {

	private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
	
	CrunchifyRetryOnExceptionStrategy retry = new CrunchifyRetryOnExceptionStrategy();

	public HttpResponse queryRestService(String url, AccessTokenResponse tokenReponse) {

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout((int)retry.DEFAULT_WAIT_TIME_IN_MILLI).build();
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		
		// add request header
		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
		HttpResponse response = null;
		
		while (retry.shouldRetry()) {
			try {
				response = client.execute(request);
				return response;
			} catch (ClientProtocolException e2) {
				LOG.error("Unable to access rest service at url: " + url + ". Return code is: "
						+ response.getStatusLine().getStatusCode(), e2);
				try {
					retry.errorOccured();
				} catch (RuntimeException e) {
					LOG.error("HTTP service error on method queryRestService:",e);					
					throw new RuntimeException("Exception while calling URL:"
							+ url, e);
				} catch (Exception e1) {
					LOG.error("HTTP service error on method queryRestService:",e1);
					throw new RuntimeException(e1);
				}
			} catch (IOException e2) {
				try {
					retry.errorOccured();
				} catch (Exception e) {
					LOG.error("HTTP service error on method queryRestService:",e);
					// TODO Auto-generated catch block
				}
				LOG.error("Unable to access rest service at url: " + url, e2);
			}
		}
		return null;

	}
	
	public HttpResponse postRestService(String url, AccessTokenResponse tokenReponse, String json) {

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout((int)retry.DEFAULT_WAIT_TIME_IN_MILLI).build();
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);
		StringEntity requestEntity = new StringEntity(
			    json,
			    ContentType.APPLICATION_JSON);
		request.setEntity(requestEntity);

		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
		HttpResponse response = null;
		
		while (retry.shouldRetry()) {
			try {
				response = client.execute(request);
				return response;
			} catch (ClientProtocolException e2) {
				LOG.error("Unable to access rest service at url: " + url + ". Return code is: "
						+ response.getStatusLine().getStatusCode(), e2);
				try {
					retry.errorOccured();
				} catch (RuntimeException e) {
					LOG.error("HTTP service error on method postRestService:",e);
					throw new RuntimeException("Exception while calling URL:"
							+ url, e);
				} catch (Exception e1) {
					LOG.error("HTTP service error on method postRestService:",e1);
					throw new RuntimeException(e1);
				}
			} catch (IOException e2) {
				try {
					retry.errorOccured();
				} catch (Exception e) {
					LOG.error("HTTP service error on method postRestService:",e);
				}
				LOG.error("Unable to access rest service at url: " + url, e2);
			}
		}
		return null;

	}
	
	
	static class CrunchifyRetryOnExceptionStrategy {
		public static final int DEFAULT_RETRIES = 3;
		public static final long DEFAULT_WAIT_TIME_IN_MILLI = 2000;
 
		private int numberOfRetries;
		private int numberOfTriesLeft;
		private long timeToWait;
 
		public CrunchifyRetryOnExceptionStrategy() {
			this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
		}
 
		public CrunchifyRetryOnExceptionStrategy(int numberOfRetries,
				long timeToWait) {
			this.numberOfRetries = numberOfRetries;
			numberOfTriesLeft = numberOfRetries;
			this.timeToWait = timeToWait;
		}
 
		/**
		 * @return true if there are tries left
		 */
		public boolean shouldRetry() {
			return numberOfTriesLeft > 0;
		}
 
		public void errorOccured() throws Exception {
			numberOfTriesLeft--;
			if (!shouldRetry()) {
				throw new Exception("Retry Failed: Total " + numberOfRetries
						+ " attempts made at interval " + getTimeToWait()
						+ "ms");
			}
			waitUntilNextTry();
		}
 
		public long getTimeToWait() {
			return timeToWait;
		}
 
		private void waitUntilNextTry() {
			try {
				Thread.sleep(getTimeToWait());
			} catch (InterruptedException ignored) {
				LOG.error("HTTP service error on waitUntilNextTry method:",ignored);
			}
		}
	}
}
