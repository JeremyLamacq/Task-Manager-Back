FROM openjdk:24-slim
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.tar.gz | tar -xz -C /opt && \
    ln -s /opt/apache-maven-3.8.7/bin/mvn /usr/bin/mvn
WORKDIR /app
COPY . /app
RUN ./mvnw clean install
CMD ["java", "-jar", "target/task-manager-back-0.0.1-SNAPSHOT.jar"]