FROM bellsoft/liberica-runtime-container:jdk-21-crac-musl AS runner

ADD spring-petclinic-3.4.0.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-XX:CRaCCheckpointTo=/app/checkpoint", "-jar", "/app/app.jar"]
