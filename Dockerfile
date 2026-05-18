# ============================================================
# STAGE 1 — BUILD
# We use a Maven + Java 17 image to compile our code.
# This stage only exists to build the JAR file.
# It won't be in the final image (keeps final image small).
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory inside the container
# Think of this like cd /app
WORKDIR /app

# Copy pom.xml first — this is a caching trick.
# Docker caches each step. If pom.xml hasn't changed,
# Docker skips downloading dependencies again.
# This makes rebuilds much faster.
COPY pom.xml .

# Download all dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Now copy all your source code
COPY src ./src

# Build the JAR, skip tests (tests need DB which isn't available here)
RUN mvn clean package -DskipTests

# ============================================================
# STAGE 2 — RUN
# We use a much smaller image — just Java 17, no Maven.
# We only copy the JAR from stage 1.
# Final image is ~200MB instead of ~500MB.
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy ONLY the built JAR from the build stage
# We renamed it app.jar (from the finalName in pom.xml)
COPY --from=build /app/target/app.jar app.jar

# Tell Docker this app listens on port 8080
EXPOSE 8080

# Command to run when container starts
# This is exactly what you'd run on your own machine
ENTRYPOINT ["java", "-jar", "app.jar"]