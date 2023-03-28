package com.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.Random;

public final class EurekaXBinding implements BindingsPropertiesProcessor {

    public static final String TYPE = "eureka-x";

    private Log log = LogFactory.getLog(EurekaXBinding.class);

    @Override
    public void process(Environment environment, Bindings bindings, Map<String, Object> properties) {
        if (!environment.getProperty("com.example.bindings.eureka-x.enable", Boolean.class, true)) {
            return;
        }

        bindings.filterBindings(TYPE).forEach(binding -> {
            log.info("processing eureka-x binding");

            /* TODO UNCOMMENT
            MapMapper map = new MapMapper(binding.getSecret(), properties);

            map.from("client-id").to("eureka.client.oauth2.client-id");
            map.from("access-token-uri").to("eureka.client.oauth2.access-token-uri");
            map.from("uri").to("eureka.client.serviceUrl.defaultZone",
                    (uri) -> String.format("%s/eureka/", uri)
            );
            properties.put("eureka.client.region", "default");

            */

            Map<String, String> secret = binding.getSecret();
            String caCert = secret.get("ca.crt");
            if (caCert != null && !caCert.isEmpty()) {
                log.info("processing eureka-x ca certificate");

                Random random = new Random();
                String generatedPassword = random.ints(97 /* letter a */, 122 /* letter z */ + 1)
                        .limit(10)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();

                // properties.put("eureka.client.tls.enabled", true);
                // "trust-store": "classpath:truststore.p12", "trust-store-type": "PKCS12", "trust-store-password": "password"
                // Create a trust store from the CA cert
                try {
                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    InputStream in = new ByteArrayInputStream(caCert.getBytes());
                    ks.load(null, generatedPassword.toCharArray());

                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    Certificate cert = cf.generateCertificate(in);
                    KeyStore.TrustedCertificateEntry certEntry = new KeyStore.TrustedCertificateEntry(cert);

                    ks.setCertificateEntry("rootca", cert);
                    FileOutputStream fos = new FileOutputStream("client-truststore.p12");
                    ks.store(fos, generatedPassword.toCharArray());
                    fos.close();
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException("Unable to open keystore output file", e);
                } catch (KeyStoreException e) {
                    throw new IllegalStateException("Unable to write keystore", e);
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException("Unable to write keystore", e);
                } catch (CertificateException e) {
                    throw new IllegalStateException("Unable to process certificate", e);
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to create keystore", e);
                }
            }
        });
    }

}
