package org.shanoir.uploader.service.rest;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpService {

	private static final Logger LOG = LoggerFactory.getLogger(HttpService.class);
	
	public HttpService() {
	}

	public HttpResponse get(String url) {
		try {
			HttpClient httpClient = HttpClientBuilder.create().build();
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
			HttpClient httpClient = HttpClientBuilder.create().build();
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
			HttpClient httpClient = HttpClientBuilder.create().build();
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
			HttpClient httpClient = HttpClientBuilder.create().build();
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

}
