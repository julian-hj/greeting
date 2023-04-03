# docker image for POC
The jar file for this image is built from a patched version of spring-cloud-bindings in [this fork](https://github.com/julian-hj/spring-cloud-bindings/tree/eureka-tls-binding).

To build the base image for greeter-messages:
```bash
pack build --builder "paketobuildpacks/builder:full" --env BP_JVM_VERSION=17 --env BP_GRADLE_BUILT_MODULE="greeter-messages" --env BP_GRADLE_BUILD_ARGUMENTS="-x jar --no-daemon assemble" test/nojar
```

To build & upload the patched image:
```bash
docker build . -t tapacr.azurecr.io/tapacr/jmh/greeter-messages
```