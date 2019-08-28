package org.shanoir.uploader.service.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpService {

	private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
	
	private HttpClient httpClient;
	
	public HttpService() {
		this.httpClient = HttpClientBuilder.create().build();
	}

	public HttpResponse get(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getKeycloakInstalled().getTokenString());
			HttpResponse response = httpClient.execute(httpGet);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	public HttpResponse post(String url, String json) {
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("Authorization", "Bearer " + ShUpOnloadConfig.getKeycloakInstalled().getTokenString());
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(requestEntity);
			HttpResponse	 response = httpClient.execute(httpPost);
			return response;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

}
