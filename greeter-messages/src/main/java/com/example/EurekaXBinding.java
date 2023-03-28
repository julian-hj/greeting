package com.example;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class EurekaXBinding implements BindingsPropertiesProcessor {

    public static final String TYPE = "eureka-x";

    @Override
    public void process(Environment environment, Bindings bindings, Map<String, Object> properties) {
        if (!environment.getProperty("com.example.bindings.myservice.enable", Boolean.class, true)) {
            return;
        }

        bindings.filterBindings(TYPE).forEach(binding -> {
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
                // properties.put("eureka.client.tls.enabled", true);
                // "trust-store": "classpath:truststore.p12", "trust-store-type": "PKCS12", "trust-store-password": "password"
                // Create a trust store from the CA cert
                KeyStore ks = KeyStore.getInstance("PKCS12");

                Random random = new Random();
                String generatedPassword = random.ints(97 /* letter a */, 122 /* letter z */ + 1)
                        .limit(10)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();

                InputStream in = new ByteArrayInputStream(caCert.getBytes());
                ks.load(null, generatedPassword.toCharArray());

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate cert = cf.generateCertificate(in);
                KeyStore.TrustedCertificateEntry certEntry = new KeyStore.TrustedCertificateEntry(cert);

                ks.setCertificateEntry("rootca", cert);

                // Store away the keystore.
                FileOutputStream fos = new FileOutputStream("client-truststore.p12");
                ks.store(fos, generatedPassword.toCharArray());
                fos.close();
            }
        });
    }

}
