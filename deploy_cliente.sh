#!/bin/bash

# Configuración del cliente
CLIENT_NODE="x104m03"
CLIENT_IP="10.147.17.103"
USER="swarch"
BASE_DIR="VotationSystem9060xt"
CLIENT_DIR="$BASE_DIR/cliente"

# Copiar JAR al nodo cliente
echo "Copiando archivos al cliente ($CLIENT_NODE)..."
scp consulta-votos/build/libs/consulta-votos.jar $USER@$CLIENT_IP:~/$CLIENT_DIR/

# Ejecutar el cliente en el nodo remoto
echo "Iniciando cliente en $CLIENT_NODE..."
ssh $USER@$CLIENT_IP "cd ~/$CLIENT_DIR && nohup java -jar consulta-votos.jar > cliente.log 2>&1 &"