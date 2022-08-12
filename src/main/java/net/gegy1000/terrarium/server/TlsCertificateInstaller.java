package net.gegy1000.terrarium.server;

import net.gegy1000.terrarium.Terrarium;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

// The version of Java 8 that Minecraft packages by default does not support the TLS certificates we need. Everything is awful!
public final class TlsCertificateInstaller implements AutoCloseable {
    // Thanks, I hate it.
    private static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();

    private static final Path KEY_STORE_PATH = Paths.get(System.getProperty("java.home"), "lib/security/cacerts");

    private final KeyStore keyStore;

    private TlsCertificateInstaller(final KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public static void installMicrosoftCertificates() {
        try (final TlsCertificateInstaller installer = TlsCertificateInstaller.open()) {
            try (final InputStream input = TlsCertificateInstaller.class.getResourceAsStream("/certificates/microsoft_azure_ecc_tls_issuing_ca_01.crt")) {
                installer.install("microsoft_azure_ecc_tls_issuing_ca_01", input);
            }
        } catch (final IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            Terrarium.LOGGER.error("Failed to install Microsoft TLS certificates. If you encounter issues, please try using the newest version of Java 8!", e);
        }
    }

    public static TlsCertificateInstaller open() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (final InputStream input = Files.newInputStream(KEY_STORE_PATH)) {
            keyStore.load(input, DEFAULT_PASSWORD);
        }

        return new TlsCertificateInstaller(keyStore);
    }

    public void install(final String alias, final InputStream input) throws CertificateException, KeyStoreException {
        final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        final Certificate certificate = certificateFactory.generateCertificate(input);
        keyStore.setCertificateEntry(alias, certificate);
    }

    @Override
    public void close() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        final SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
    }
}
