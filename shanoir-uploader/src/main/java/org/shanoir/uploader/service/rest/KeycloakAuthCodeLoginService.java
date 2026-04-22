/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.uploader.service.rest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.json.JSONObject;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implements the OAuth2 Authorization Code + PKCE login flow against Keycloak
 * for the shanoir-uploader desktop app.
 *
 * <p>A cookie-aware Apache HttpClient with redirect handling disabled drives
 * Keycloak's form-post authentication session entirely within the JVM — no
 * external browser is involved. It GETs the Keycloak login page, POSTs
 * credentials, then follows Keycloak-internal redirects manually. Each 200
 * response is inspected to detect whether the next step is an OTP input
 * challenge, an initial OTP setup (TOTP enrollment), or a final redirect to
 * the callback URI carrying the authorization code. The code is then exchanged
 * for tokens via the standard token endpoint.
 *
 * <p>One {@link LoginSession} covers the complete login attempt. The caller
 * must hold on to the session and call the appropriate submit method after
 * displaying each intermediate panel.
 */
@Component
public class KeycloakAuthCodeLoginService {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakAuthCodeLoginService.class);

    private static final String REALM_PATH = "/auth/realms/shanoir-ng/protocol/openid-connect";
    private static final String REDIRECT_URI = "http://localhost:12345/callback";
    private static final String CLIENT_ID = "shanoir-uploader";
    private static final String DEV_LOCAL = "https://shanoir-ng-nginx";
    private static final int MAX_REDIRECTS = 15;

    // ─── Public API ──────────────────────────────────────────────────────────

    public enum AuthStep {
        SUCCESS, OTP_REQUIRED, OTP_SETUP_REQUIRED, BAD_CREDENTIALS
    }

    public static class AuthResult {
        public final AuthStep step;
        /** Non-null on SUCCESS. */
        public final String accessToken;
        /** Non-null on OTP_SETUP_REQUIRED: the QR-code PNG bytes. */
        public final byte[] qrCodeBytes;
        /** Non-null on OTP_SETUP_REQUIRED: base32 key for manual entry. */
        public final String totpManualKey;

        private AuthResult(AuthStep step, String accessToken, byte[] qrCodeBytes, String totpManualKey) {
            this.step = step;
            this.accessToken = accessToken;
            this.qrCodeBytes = qrCodeBytes;
            this.totpManualKey = totpManualKey;
        }

        public static AuthResult success(String accessToken) {
            return new AuthResult(AuthStep.SUCCESS, accessToken, null, null);
        }

        public static AuthResult otpRequired() {
            return new AuthResult(AuthStep.OTP_REQUIRED, null, null, null);
        }

        public static AuthResult otpSetupRequired(byte[] qrCodeBytes, String totpManualKey) {
            return new AuthResult(AuthStep.OTP_SETUP_REQUIRED, null, qrCodeBytes, totpManualKey);
        }

        public static AuthResult badCredentials() {
            return new AuthResult(AuthStep.BAD_CREDENTIALS, null, null, null);
        }
    }

    /**
     * Creates a new stateful {@link LoginSession} for the given server URL.
     * Call this once per login attempt and keep the reference across OTP steps.
     */
    public LoginSession createSession(String serverUrl) throws Exception {
        return new LoginSession(serverUrl);
    }

    // ─── LoginSession ─────────────────────────────────────────────────────────

    public static class LoginSession {

        private final String serverUrl;
        private final String codeVerifier;
        private final String codeChallenge;
        private final CloseableHttpClient authClient;

        /** The form action URL of the current Keycloak page. Updated at each step. */
        String currentActionUrl;
        /** The totpSecret hidden-field value present on the OTP-setup page. */
        String currentTotpSecret;

        LoginSession(String serverUrl) throws Exception {
            this.serverUrl = serverUrl;
            this.codeVerifier = generateCodeVerifier();
            this.codeChallenge = computeCodeChallenge(this.codeVerifier);
            this.authClient = buildAuthClient(serverUrl, new BasicCookieStore());
        }

        /**
         * Step 1: GETs the Keycloak login page to start a fresh auth session,
         * then POSTs the credentials to the extracted form action URL.
         *
         * @return SUCCESS, OTP_REQUIRED, OTP_SETUP_REQUIRED, or BAD_CREDENTIALS.
         */
        public AuthResult submitCredentials(String username, String password) throws Exception {
            String authUrl = serverUrl + REALM_PATH + "/auth"
                + "?client_id=" + CLIENT_ID
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256"
                + "&scope=openid+offline_access";

            String loginPageHtml;
            try (CloseableHttpResponse response = authClient.execute(new HttpGet(authUrl))) {
                int status = response.getCode();
                if (status != 200) {
                    LOG.error("Keycloak login page returned HTTP {}", status);
                    return AuthResult.badCredentials();
                }
                loginPageHtml = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }

            currentActionUrl = extractFormAction(loginPageHtml);
            if (currentActionUrl == null) {
                throw new Exception("Could not extract login form action URL from Keycloak page");
            }

            String postBody = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8)
                + "&credentialId=";

            return doPost(currentActionUrl, postBody);
        }

        /**
         * Step 2a: POSTs the one-time code to the OTP-input form action URL.
         */
        public AuthResult submitOtp(String otpCode) throws Exception {
            String postBody = "otp=" + URLEncoder.encode(otpCode, StandardCharsets.UTF_8)
                + "&credentialId=";
            return doPost(currentActionUrl, postBody);
        }

        /**
         * Step 2b: POSTs the TOTP enrollment form (first-time OTP setup).
         * {@code userLabel} is the device name shown in Keycloak; may be empty.
         */
        public AuthResult submitOtpSetup(String otpCode, String userLabel) throws Exception {
            String postBody = "totp=" + URLEncoder.encode(otpCode, StandardCharsets.UTF_8)
                + "&totpSecret=" + URLEncoder.encode(currentTotpSecret != null ? currentTotpSecret : "", StandardCharsets.UTF_8)
                + "&userLabel=" + URLEncoder.encode(userLabel != null ? userLabel : "", StandardCharsets.UTF_8)
                + "&credentialId=";
            return doPost(currentActionUrl, postBody);
        }

        // ─── Private helpers ──────────────────────────────────────────────────

        private AuthResult doPost(String actionUrl, String formBody) throws Exception {
            HttpPost post = new HttpPost(actionUrl);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity(formBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = authClient.execute(post)) {
                return processResponse(response);
            }
        }

        private AuthResult processResponse(CloseableHttpResponse response) throws Exception {
            int status = response.getCode();

            if (status == 302 || status == 301) {
                String location = response.getFirstHeader("Location").getValue();
                EntityUtils.consume(response.getEntity());
                return followRedirects(location);
            } else if (status == 200) {
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return classifyPage(body);
            } else {
                EntityUtils.consume(response.getEntity());
                LOG.error("Unexpected HTTP status {} during Keycloak auth flow", status);
                return AuthResult.badCredentials();
            }
        }

        /**
         * Manually follows Keycloak-internal redirects until either the callback
         * URI is reached (carry the auth code) or a 200 page requires user action.
         */
        private AuthResult followRedirects(String startLocation) throws Exception {
            String location = startLocation;

            for (int i = 0; i < MAX_REDIRECTS; i++) {
                if (location.startsWith(REDIRECT_URI)) {
                    String code = extractQueryParam(location, "code");
                    if (code == null) {
                        throw new Exception("No authorization code found in callback URL");
                    }
                    String accessToken = exchangeCode(code);
                    return AuthResult.success(accessToken);
                }

                try (CloseableHttpResponse response = authClient.execute(new HttpGet(location))) {
                    int status = response.getCode();
                    if (status == 302 || status == 301) {
                        location = response.getFirstHeader("Location").getValue();
                        EntityUtils.consume(response.getEntity());
                    } else if (status == 200) {
                        String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        return classifyPage(body);
                    } else {
                        EntityUtils.consume(response.getEntity());
                        return AuthResult.badCredentials();
                    }
                }
            }

            throw new Exception("Exceeded maximum redirect count during Keycloak auth flow");
        }

        /**
         * Inspects the HTML body to determine which Keycloak page was returned.
         * Updates {@link #currentActionUrl} and {@link #currentTotpSecret} as needed.
         */
        private AuthResult classifyPage(String html) {
            if (html.contains("id=\"kc-totp-settings-form\"")) {
                currentActionUrl = extractFormAction(html);
                currentTotpSecret = extractTotpSecret(html);
                byte[] qrBytes = extractQrCode(html);
                String manualKey = extractTotpManualKey(html);
                return AuthResult.otpSetupRequired(qrBytes, manualKey);
            } else if (html.contains("id=\"kc-otp-login-form\"")) {
                currentActionUrl = extractFormAction(html);
                return AuthResult.otpRequired();
            } else {
                // Either back on the login page (bad credentials) or an unknown page
                return AuthResult.badCredentials();
            }
        }

        /**
         * Exchanges the authorization code for an access token + refresh token,
         * then schedules a background token refresh job.
         */
        private String exchangeCode(String code) throws Exception {
            String tokenUrl = serverUrl + REALM_PATH + "/token";
            String postBody = "grant_type=authorization_code"
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&client_id=" + CLIENT_ID
                + "&code_verifier=" + codeVerifier;

            HttpPost post = new HttpPost(tokenUrl);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setEntity(new StringEntity(postBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = authClient.execute(post)) {
                if (response.getCode() != 200) {
                    throw new Exception("Authorization code exchange failed: HTTP " + response.getCode());
                }
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(body);
                String accessToken = json.getString("access_token");
                String refreshToken = json.optString("refresh_token", null);
                if (refreshToken != null) {
                    scheduleTokenRefresh(tokenUrl, refreshToken);
                }
                return accessToken;
            }
        }

        /**
         * Starts a background job that refreshes the access token every 240 s,
         * matching the behaviour of the existing ROPC-based login.
         */
        private void scheduleTokenRefresh(String tokenUrl, String refreshToken) {
            String refreshBody = "client_id=" + CLIENT_ID
                + "&grant_type=refresh_token"
                + "&refresh_token=" + refreshToken;

            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(() -> {
                try {
                    HttpPost post = new HttpPost(tokenUrl);
                    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    post.setEntity(new StringEntity(refreshBody, StandardCharsets.UTF_8));
                    try (CloseableHttpResponse response = authClient.execute(post)) {
                        if (response.getCode() == 200) {
                            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                            String newToken = new JSONObject(body).getString("access_token");
                            ShUpOnloadConfig.setTokenString(newToken);
                            LOG.debug("Access token refreshed via browser-flow session.");
                        } else {
                            LOG.error("Token refresh failed: HTTP {}", response.getCode());
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Token refresh error: {}", e.getMessage(), e);
                }
            }, 240, 240, TimeUnit.SECONDS);
        }
    }

    // ─── HTML parsing utilities ───────────────────────────────────────────────

    private static String extractFormAction(String html) {
        Matcher m = Pattern.compile(
            "<form\\b[^>]*\\baction=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        ).matcher(html);
        return m.find() ? unescapeHtml(m.group(1)) : null;
    }

    private static byte[] extractQrCode(String html) {
        Matcher img = Pattern.compile(
            "<img[^>]*id=\"kc-totp-secret-qr-code\"[^>]*>", Pattern.DOTALL
        ).matcher(html);
        if (!img.find()) return null;
        Matcher src = Pattern.compile(
            "src=\"data:image/png;base64,([^\"]+)\""
        ).matcher(img.group());
        return src.find() ? Base64.getDecoder().decode(src.group(1).trim()) : null;
    }

    private static String extractTotpSecret(String html) {
        // The hidden totpSecret field is what we must POST back during setup
        Matcher m = Pattern.compile(
            "name=\"totpSecret\"[^>]*value=\"([^\"]+)\"", Pattern.DOTALL
        ).matcher(html);
        if (m.find()) return m.group(1);
        // Attribute order may vary
        m = Pattern.compile(
            "value=\"([^\"]+)\"[^>]*name=\"totpSecret\"", Pattern.DOTALL
        ).matcher(html);
        return m.find() ? m.group(1) : null;
    }

    private static String extractTotpManualKey(String html) {
        // The base32 key shown to users for manual entry into authenticator apps
        Matcher m = Pattern.compile(
            "<span id=\"kc-totp-secret-key\">([^<]+)</span>"
        ).matcher(html);
        return m.find() ? m.group(1).trim() : null;
    }

    private static String extractQueryParam(String url, String param) {
        Matcher m = Pattern.compile(
            "[?&]" + Pattern.quote(param) + "=([^&#]+)"
        ).matcher(url);
        return m.find() ? m.group(1) : null;
    }

    private static String unescapeHtml(String s) {
        return s.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    // ─── HTTP client factory ──────────────────────────────────────────────────

    /**
     * Builds a dedicated Apache HttpClient for the Keycloak browser flow.
     * Key differences from the main {@link HttpService} client:
     * <ul>
     *   <li>Has a {@link BasicCookieStore} to carry the Keycloak session cookie.</li>
     *   <li>Redirect handling is disabled so we can inspect each Location header.</li>
     * </ul>
     */
    private static CloseableHttpClient buildAuthClient(String serverUrl, BasicCookieStore cookieStore)
            throws Exception {
        SSLConnectionSocketFactory sslFactory;
        if (serverUrl.startsWith(DEV_LOCAL)) {
            SSLContext trustAll = SSLContexts.custom()
                .loadTrustMaterial(new TrustStrategy() {
                    @Override
                    public boolean isTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        return true;
                    }
                }).build();
            sslFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(trustAll).build();
            LOG.info("KeycloakBrowserLoginService: using trust-all SSL context for dev.");
        } else {
            sslFactory = SSLConnectionSocketFactoryBuilder.create()
                .setHostnameVerifier(new CustomHostnameVerifier()).build();
        }

        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(sslFactory)
            .build();

        ServiceConfiguration sc = ServiceConfiguration.getInstance();
        if (sc.isProxyEnabled() && sc.getProxyHost() != null) {
            HttpHost proxyHost = sc.getProxyPort() != null
                ? new HttpHost(sc.getProxyHost(), Integer.parseInt(sc.getProxyPort()))
                : new HttpHost(sc.getProxyHost());

            var builder = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCookieStore(cookieStore)
                .disableRedirectHandling()
                .setRoutePlanner(new DefaultProxyRoutePlanner(proxyHost))
                .setProxy(proxyHost);

            if (sc.getProxyUser() != null && sc.getProxyPassword() != null) {
                BasicCredentialsProvider cp = new BasicCredentialsProvider();
                cp.setCredentials(new AuthScope(proxyHost),
                    new UsernamePasswordCredentials(sc.getProxyUser(),
                        sc.getProxyPassword().toCharArray()));
                builder.setDefaultCredentialsProvider(cp);
            }
            return builder.build();
        }

        return HttpClients.custom()
            .setConnectionManager(cm)
            .setDefaultCookieStore(cookieStore)
            .disableRedirectHandling()
            .build();
    }

    // ─── PKCE helpers ─────────────────────────────────────────────────────────

    private static String generateCodeVerifier() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String computeCodeChallenge(String codeVerifier) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
            .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
