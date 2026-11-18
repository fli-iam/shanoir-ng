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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks that the socket factory used for Shanoir servers rejects a
 * certificate that is trusted, but issued for another host name.
 */
public class TlsSocketFactoriesTest {

    private static final char[] PASSWORD = "changeit".toCharArray();

    @TempDir
    private Path tempDir;

    @Test
    public void acceptsTrustedCertificateIssuedForTheHost() throws Exception {
        KeyStore serverKeyStore = createKeyStore("san=dns:localhost,ip:127.0.0.1");
        try (TlsServer server = new TlsServer(serverKeyStore)) {
            assertEquals(200, get("https://localhost:" + server.getPort() + "/", serverKeyStore));
        }
    }

    @Test
    public void rejectsTrustedCertificateIssuedForAnotherHost() throws Exception {
        KeyStore serverKeyStore = createKeyStore("san=dns:other-host.example.org");
        try (TlsServer server = new TlsServer(serverKeyStore)) {
            assertThrows(SSLPeerUnverifiedException.class,
                    () -> get("https://localhost:" + server.getPort() + "/", serverKeyStore));
        }
    }

    /**
     * Sends a GET with a client built like in HttpService, trusting only
     * the certificate of the test server.
     */
    private int get(String url, KeyStore serverKeyStore) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("server", serverKeyStore.getCertificate("server"));
        SSLContext clientContext = SSLContexts.custom().loadTrustMaterial(trustStore, null).build();
        try (CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(TlsSocketFactories.withHostnameVerification(clientContext))
                        .build())
                .disableAutomaticRetries()
                .build();
                CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            return response.getCode();
        }
    }

    /**
     * Generates a self-signed key pair with the given subject alternative names, using the JDK keytool.
     */
    private KeyStore createKeyStore(String subjectAlternativeNames) throws Exception {
        File keyStoreFile = tempDir.resolve("server-" + System.nanoTime() + ".p12").toFile();
        String keytool = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();
        Process process = new ProcessBuilder(keytool, "-genkeypair", "-alias", "server",
                "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
                "-dname", "CN=Shanoir test server", "-ext", subjectAlternativeNames,
                "-storetype", "PKCS12", "-keystore", keyStoreFile.getAbsolutePath(),
                "-storepass", new String(PASSWORD), "-keypass", new String(PASSWORD))
                .redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(60, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("keytool failed: " + output);
        }
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = new FileInputStream(keyStoreFile)) {
            keyStore.load(in, PASSWORD);
        }
        return keyStore;
    }

    /**
     * Minimal HTTPS server answering 200 to every request.
     */
    private static final class TlsServer implements AutoCloseable {

        private final SSLServerSocket serverSocket;

        TlsServer(KeyStore keyStore) throws Exception {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, PASSWORD);
            SSLContext serverContext = SSLContext.getInstance("TLS");
            serverContext.init(kmf.getKeyManagers(), null, null);
            serverSocket = (SSLServerSocket) serverContext.getServerSocketFactory().createServerSocket(0);
            Thread acceptor = new Thread(this::serve, "tls-test-server");
            acceptor.setDaemon(true);
            acceptor.start();
        }

        int getPort() {
            return serverSocket.getLocalPort();
        }

        private void serve() {
            while (!serverSocket.isClosed()) {
                try (Socket socket = serverSocket.accept()) {
                    InputStream in = socket.getInputStream();
                    // read the request headers, up to the empty line
                    int matched = 0;
                    int b;
                    while (matched < 4 && (b = in.read()) != -1) {
                        matched = (b == "\r\n\r\n".charAt(matched)) ? matched + 1 : (b == '\r' ? 1 : 0);
                    }
                    OutputStream out = socket.getOutputStream();
                    out.write("HTTP/1.1 200 OK\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                            .getBytes(StandardCharsets.US_ASCII));
                    out.flush();
                } catch (IOException e) {
                    // client rejected the certificate or server closed: wait for next connection
                }
            }
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
        }
    }

}
