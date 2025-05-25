FROM bellsoft/liberica-runtime-container:jdk-21-crac-musl AS runner

ADD Example.java /app/Example.java
WORKDIR /app
RUN javac Example.java
ENTRYPOINT ["java", "-XX:CRaCCheckpointTo=/app/checkpoint", "Example"]
