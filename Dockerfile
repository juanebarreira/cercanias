FROM arm64v8/gradle:8.12.0-jdk21-alpine AS build

# Copiar solo los archivos necesarios para la dependencias primero
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew .

# Descargar dependencias (esto se cacheará si no cambian los archivos de gradle)
RUN gradle dependencies --no-daemon

# Ahora copiar el código fuente
COPY src src

# Compilar
RUN gradle build --no-daemon --exclude-task test

# Segunda etapa: imagen final
FROM eclipse-temurin:21-jre-alpine

# Instalar Chrome y sus dependencias con opciones de optimización
RUN apk add --no-cache \
    chromium \
    chromium-chromedriver \
    ttf-freefont \
    && rm -rf /var/cache/apk/* \
    && mkdir -p /usr/share/fonts/truetype/freefont

WORKDIR /app

# Copiar el certificado y configurarlo
COPY api.telegram.org.pem /tmp/
RUN keytool -importcert \
    -alias telegram \
    -cacerts \
    -file /tmp/api.telegram.org.pem \
    -storepass changeit \
    -noprompt

# Copiar el jar compilado desde la etapa de build
COPY --from=build /home/gradle/build/libs/*.jar ./app.jar

# Variables de entorno necesarias
ENV BOT_TOKEN=""
ENV CHAT_ID=""
ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver
ENV DOCKER_ENV=true
ENV TO_TELEGRAM=true

# Configuraciones de optimización
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xmx512m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENV CHROME_OPTS="--disable-gpu --no-sandbox --disable-dev-shm-usage --disable-software-rasterizer"

# Crear directorio para screenshots
RUN mkdir -p /app/screenshots

# Comando para ejecutar la aplicación
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]