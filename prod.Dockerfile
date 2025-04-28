FROM alpine:latest

RUN apk add openjdk17

WORKDIR /app

COPY . .

RUN rm -f ./build/libs/*.jar
RUN ./gradlew bootJar
RUN cp ./build/libs/EventScope-*.jar ./service.jar

COPY application-dev.yaml .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "service.jar"]
CMD ["--spring.config.location=classpath:/application.yaml,file:application-dev.yaml"]