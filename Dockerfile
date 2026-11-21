# syntax=docker/dockerfile:1

# --- Etapa 1: compilar. Las pruebas ya corrieron en el CI (compilar-y-probar). ---
FROM eclipse-temurin:25-jdk@sha256:ce2ef9d2d67a68de5e196d4de9d743327c44a10b57fe02ec39f572c652cacfd3 AS construccion
WORKDIR /construccion
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp package -DskipTests -Dspotless.check.skip=true -Djacoco.skip=true \
    && cp target/fastfood-inventario-*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extraido

# --- Etapa 2: ejecutar. Solo el JRE y la aplicación separada en capas. ---
FROM eclipse-temurin:25-jre@sha256:4ed132f6832e9804b123e89a849a7b4502756ae64a4ee08ebab939d9d5d0a9fa
RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid app --no-create-home app
WORKDIR /app
# De lo que menos cambia a lo que más: si solo cambia el código, se reutilizan las capas de dependencias.
COPY --from=construccion /construccion/extraido/dependencies/ ./
COPY --from=construccion /construccion/extraido/spring-boot-loader/ ./
COPY --from=construccion /construccion/extraido/snapshot-dependencies/ ./
COPY --from=construccion /construccion/extraido/application/ ./
USER 10001:10001
EXPOSE 8080
# La JVM usa como máximo el 75 % de la memoria del contenedor, y se cierra si se queda sin memoria
# (Kubernetes la reinicia limpia, en lugar de dejarla viva pero rota).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
