keystore password is: password

generated with a command like:
```
openssl pkcs12 -export -out server-keystore.p12 -inkey server-key.pem -in server-crt.pem -name eureka_test
keytool -import -trustcacerts -alias rootCA -keystore "server-keystore.p12" -storepass password -file ca-crt.pem -noprompt
```

truststore password is: password

```
keytool -import -trustcacerts -alias rootCA -keystore "truststore.p12" -storepass password -file ca-crt.pem -noprompt
```
