package org.shanoir.uploader.service.rest;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.ssl.HttpClientHostnameVerifier;

public class CustomHostnameVerifier implements HttpClientHostnameVerifier {

	@Override
	public boolean verify(String host, SSLSession session) {
		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
		return hv.verify(host, session);
	}

	@Override
	public void verify(String host, X509Certificate cert) throws SSLException {
	}

}