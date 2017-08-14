/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kickthedrawer.things.piano;

import com.amazonaws.services.iot.client.AWSIotMqttClient;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static java.lang.String.format;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author warnet
 */
@Configuration
public class IoTConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(IoTConfiguration.class);

    @Bean
    public AWSIotMqttClient awsIotMqttClient(@Value("#{aws.clientEndpoint}") String clientEndpoint,
            @Value("#{aws.clientId}") String clientId,
            @Value("#{aws.certificateFile}") String certificateFile,
            @Value("#{aws.privateKeyFile}") String privateKeyFile) throws Exception {
        logger.info("Constructing AWS IOT client");
        Pair<KeyStore, String> keyStorePair = getKeyStorePasswordPair(certificateFile, privateKeyFile, null);

        AWSIotMqttClient awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, keyStorePair.getLeft(), keyStorePair.getRight());

        return awsIotClient;
    }

    private Pair<KeyStore, String> getKeyStorePasswordPair(final String certificateFile, final String privateKeyFile,
            String keyAlgorithm) {
        if (certificateFile == null || privateKeyFile == null) {
            System.out.println("Certificate or private key file missing");
            return null;
        }
        System.out.println("Cert file:" + certificateFile + " Private key: " + privateKeyFile);

        final PrivateKey privateKey = loadPrivateKeyFromFile(privateKeyFile, keyAlgorithm);

        final List<Certificate> certChain = loadCertificatesFromFile(certificateFile);

        if (certChain == null || privateKey == null) {
            return null;
        }

        return getKeyStorePasswordPair(certChain, privateKey);
    }

    private static List<Certificate> loadCertificatesFromFile(final String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Certificate file: " + filename + " is not found.");
            return null;
        }

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (List<Certificate>) certFactory.generateCertificates(stream);
        } catch (IOException | CertificateException e) {
            System.out.println("Failed to load certificate file " + filename);
        }
        return null;
    }

    public static Pair<KeyStore, String> getKeyStorePasswordPair(final List<Certificate> certificates, final PrivateKey privateKey) {
        KeyStore keyStore;
        String keyPassword;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            // randomly generated key password for the key in the KeyStore
            keyPassword = new BigInteger(128, new SecureRandom()).toString(32);

            Certificate[] certChain = new Certificate[certificates.size()];
            certChain = certificates.toArray(certChain);
            keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certChain);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            logger.error("Failed to create key store", e);
            return null;
        }

        return Pair.of(keyStore, keyPassword);
    }

    private PrivateKey loadPrivateKeyFromFile(final String filename, final String algorithm) {
        PrivateKey privateKey = null;

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Private key file not found: " + filename);
            return null;
        }
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
            privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
        } catch (IOException | GeneralSecurityException e) {
            logger.warn(format("Failed to load private key from file %s", filename), e);
        }

        return privateKey;
    }
}
