FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/miles-per-awa
COPY build/install/miles-per-awa/ .
EXPOSE 8080
VOLUME ["/opt/miles-per-awa/conf"]
ENTRYPOINT ["./bin/miles-per-awa"]