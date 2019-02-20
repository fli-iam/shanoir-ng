package org.shanoir.ng.utils.tests;

import javax.net.ssl.X509TrustManager;

public class TestTrustManager implements X509TrustManager {

		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[0];
		}
		
		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
		
		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
}

