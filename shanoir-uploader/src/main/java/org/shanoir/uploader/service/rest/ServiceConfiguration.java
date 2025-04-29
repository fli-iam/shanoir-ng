package org.shanoir.uploader.service.rest;

import org.shanoir.uploader.ShUpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ServiceConfiguration.class);

	/** Constructeur privé */
	private ServiceConfiguration() {
	}

	/** Instance unique pré-initialisée */
	private static ServiceConfiguration INSTANCE = new ServiceConfiguration();

	/** Point d'accès pour l'instance unique du singleton */
	public static ServiceConfiguration getInstance() {
		return INSTANCE;
	}

	public String getProxyHost() {
		if (ShUpConfig.proxyProperties.getProperty("proxy.host") != null
				&& !ShUpConfig.proxyProperties.getProperty("proxy.host").equals("")) {
			return ShUpConfig.proxyProperties.getProperty("proxy.host");
		} else {
			return null;
		}
	}

	public String getProxyPort() {
		if (ShUpConfig.proxyProperties.getProperty("proxy.port") != null
				&& !ShUpConfig.proxyProperties.getProperty("proxy.port").equals("")) {
			return ShUpConfig.proxyProperties.getProperty("proxy.port");
		} else {
			return null;
		}
	}

	public String getProxyUser() {
		if (ShUpConfig.proxyProperties.getProperty("proxy.user") != null
				&& !ShUpConfig.proxyProperties.getProperty("proxy.user").equals("")) {
			return ShUpConfig.proxyProperties.getProperty("proxy.user");
		} else {
			return null;
		}
	}

	public String getProxyPassword() {
		if (ShUpConfig.proxyProperties.getProperty("proxy.password") != null
				&& !ShUpConfig.proxyProperties.getProperty("proxy.password").equals("")) {
			return ShUpConfig.proxyProperties.getProperty("proxy.password");
		} else {
			return null;
		}
	}

	public String getTlsProtocol() {
		if (ShUpConfig.proxyProperties.getProperty("tls.protocols") != null
				&& !ShUpConfig.proxyProperties.getProperty("tls.protocols").equals("")) {
			return ShUpConfig.proxyProperties.getProperty("tls.protocols");
		} else {
			return "TLSv1.2";
		}
	}

	public String getTlsCypherSuite() {
		if (ShUpConfig.proxyProperties.getProperty("tls.cipherSuites") != null
				&& !ShUpConfig.proxyProperties.getProperty("tls.cipherSuites").equals("")) {
			return ShUpConfig.proxyProperties.getProperty("tls.cipherSuites");
		} else {
			return null;
		}
	}

	public Boolean isProxySecure() {
		if (ShUpConfig.proxyProperties.getProperty("proxy.secure") != null
				&& Boolean.valueOf(ShUpConfig.proxyProperties.getProperty("proxy.secure"))) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean isProxyEnabled() {
		if (ShUpConfig.proxyProperties.getProperty("proxy.enabled") != null
				&& Boolean.valueOf(ShUpConfig.proxyProperties.getProperty("proxy.enabled"))) {
			return true;
		} else {
			return false;
		}
	}

	public String getTestURL() {
		String profile = ShUpConfig.basicProperties.getProperty("profile");
		if (profile != null && !profile.equals("")) {
					if (profile.contains("OFSEP")) {
						if (profile.contains("Qualif")) {
							return (ShUpConfig.OFSEP_QUALIF_SERVER);
						}
						return (ShUpConfig.OFSEP_SERVER);
					} else if (profile.contains("Neurinfo")){
						if (profile.contains("Qualif")) {
							return (ShUpConfig.NEURINFO_QUALIF_SERVER);
						}
						return (ShUpConfig.NEURINFO_SERVER);
					} else {
						return ShUpConfig.LOCAL_DEV_SERVER;
					}
				} else {
					logger.warn("Profile property is empty, using default server: {}", ShUpConfig.NEURINFO_SERVER);
					return ShUpConfig.NEURINFO_SERVER;
				}
			}
}
