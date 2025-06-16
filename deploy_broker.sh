#!/bin/bash

# Configuración del broker
BROKER_NODE="x104m02"
BROKER_IP="10.147.17.102"  # IP correcta para x104m02
USER="swarch"
BASE_DIR="VotationSystem9060xt"
BROKER_DIR="$BASE_DIR/broker"

# Copiar archivos al nodo broker
echo "Copiando archivos al broker ($BROKER_NODE)..."
scp broker-consulta/build/libs/broker-consulta.jar $USER@$BROKER_IP:~/$BROKER_DIR/
scp broker-consulta/src/main/resources/broker-consulta.properties $USER@$BROKER_IP:~/$BROKER_DIR/

# Ejecutar el broker en el nodo remoto
echo "Iniciando broker en $BROKER_NODE..."
ssh $USER@$BROKER_IP "cd ~/$BROKER_DIR && nohup java -jar broker-consulta.jar broker-consulta.properties > broker.log 2>&1 &"