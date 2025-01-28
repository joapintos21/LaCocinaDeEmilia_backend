#!/bin/bash

# Detecta la rama actual
BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Configura la variable `env` en función de la rama
if [ "$BRANCH" == "master" ]; then
    ENVIRONMENT="production"
elif [ "$BRANCH" == "develop" ]; then
    ENVIRONMENT="development"
else
    echo "Branch desconocida, configurando como desarrollo por defecto."
    ENVIRONMENT="development"
fi

# Ejecuta Maven con el perfil adecuado
echo "Usando entorno: $ENVIRONMENT"
mvn clean install -Denv=$ENVIRONMENT