FROM bellsoft/liberica-runtime-container:jdk-21-crac-musl AS runner

ADD ./build/libs/demo-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT java -XX:CRaCCheckpointTo=/app/checkpoint -jar /app/app.jar
