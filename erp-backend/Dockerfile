# Multi-stage build: compile with Maven + full JDK, ship only a JRE.
# Configuration comes from environment variables at runtime (ERP_DATASOURCE_HOST -> erp.datasource.host, ...),
# so the image itself contains no secrets.

# ---- Build stage ----
# A jar runs on any CPU, so compile on the builder's native platform (no emulation)
# even when the final image targets another architecture (e.g. the x86 server from an ARM Mac).
FROM --platform=$BUILDPLATFORM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app

# Copy the poms first so the dependency layer is reused when only source code changes.
COPY pom.xml .
COPY ERP-Common/pom.xml ERP-Common/
COPY ERP-Pojo/pom.xml ERP-Pojo/
COPY ERP-Server/pom.xml ERP-Server/
RUN mvn -B -q dependency:go-offline -pl ERP-Server -am

COPY ERP-Common/src ERP-Common/src
COPY ERP-Pojo/src ERP-Pojo/src
COPY ERP-Server/src ERP-Server/src
# Tests will run in CI; skip them here.
RUN mvn -B -q package -DskipTests && cp ERP-Server/target/ERP-Server-*.jar app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:11-jre
WORKDIR /app

# Don't run as root inside the container.
RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /app/app.jar app.jar

# 8090: API, 8091: actuator health (internal only)
EXPOSE 8090 8091
# MaxRAMPercentage: size the heap from the container's memory limit, not the host's.
# 50% leaves room for metaspace, code cache and thread stacks (~200 MB for this app).
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50", "-jar", "app.jar"]
