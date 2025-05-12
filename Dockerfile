FROM arm64v8/eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y --no-install-recommends maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY testng.xml .
RUN mvn dependency:go-offline -Dmaven.multiModuleProjectDirectory=.
RUN mvn clean package -DskipTests
CMD ["mvn", "test"]