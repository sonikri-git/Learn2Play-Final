# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the Maven project
COPY pom.xml .
COPY src ./src

# Build the Spring Boot application
RUN mvn clean package -DskipTests


# Run stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Install Python
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    rm -rf /var/lib/apt/lists/*

# Copy Spring Boot application
COPY --from=build /app/target/*.jar app.jar

# Copy AI folder
COPY ai ./ai

# Install Python dependencies
RUN pip3 install --no-cache-dir \
    requests \
    pdfplumber \
    pandas \
    tqdm \
    python-docx

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]