#! /bin/bash -eux

kubectl get secret -n secretgen-test server-cert -oyaml | yq -r '.data["key.pem"]' | base64 -d > server-key.pem
kubectl get secret -n secretgen-test client-cert -oyaml | yq -r '.data["key.pem"]' | base64 -d > client-key.pem
kubectl get secret -n secretgen-test client-cert -oyaml | yq -r '.data["crt.pem"]' | base64 -d > client-crt.pem
kubectl get secret -n secretgen-test server-cert -oyaml | yq -r '.data["crt.pem"]' | base64 -d > server-crt.pem
kubectl get secret -n secretgen-test root-ca-cert -oyaml | yq -r '.data["key.pem"]' | base64 -d > ca-key.pem
kubectl get secret -n secretgen-test root-ca-cert -oyaml | yq -r '.data["crt.pem"]' | base64 -d > ca-crt.pem
rm server-keystore.p12 || true
openssl pkcs12 -export -out server-keystore.p12 -inkey server-key.pem -in server-crt.pem -name eureka_test -password pass:password
keytool -import -trustcacerts -alias rootCA -keystore "server-keystore.p12" -storepass password -file ca-crt.pem -noprompt
rm client-keystore.p12 || true
openssl pkcs12 -export -out client-keystore.p12 -inkey client-key.pem -in client-crt.pem -name eureka_test -password pass:password
keytool -import -trustcacerts -alias rootCA -keystore "client-keystore.p12" -storepass password -file ca-crt.pem -noprompt
rm truststore.p12 || true
keytool -import -trustcacerts -alias rootCA -keystore "truststore.p12" -storepass password -file ca-crt.pem -noprompt

base64 -w0 -i server-keystore.p12 > server-keystore.p12.b64
base64 -w0 -i client-keystore.p12 > client-keystore.p12.b64
base64 -w0 -i truststore.p12 > truststore.p12.b64
