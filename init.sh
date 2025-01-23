#!/bin/sh

# Configurar el certificado
keytool -importcert \
    -alias telegram \
    -cacerts \
    -file /tmp/api.telegram.org.pem \
    -storepass changeit \
    -noprompt || true

# Mantener el contenedor vivo
exec "$@"