#!/bin/bash

# Configuración
USER="swarch"
BASE_DIR="VotationSystem9060xt"
CIUDADANO_DIR="$BASE_DIR/ciudadano"
START_PORT=12001
START_NODE=4
END_NODE=31

# Desplegar en cada nodo
for ((i = START_NODE; i <= END_NODE; i++)); do
    # Formatear número de nodo (04, 05, ...)
    NODE_NUM=$(printf "%02d" $i)
    NODE="x104m$NODE_NUM"
    IP="10.147.17.1$NODE_NUM"  # Ajusta según el patrón real de IPs
    
    # Calcular puerto único (12001, 12002, ...)
    PORT=$((START_PORT + i - START_NODE))
    
    # ID de instancia (consulta-01, consulta-02, ...)
    INSTANCE_ID="consulta-$(printf "%02d" $((i - START_NODE + 1)))"
    
    echo "Desplegando en $NODE ($IP) en puerto $PORT con ID $INSTANCE_ID"
    
    # Copiar JAR al nodo
    scp consulta-ciudadanos/build/libs/consulta-ciudadanos.jar $USER@$IP:~/$CIUDADANO_DIR/
    
    # Ejecutar en el nodo remoto
    ssh $USER@$IP "cd ~/$CIUDADANO_DIR && nohup java -Dinstance.id=$INSTANCE_ID -Dinstance.port=$PORT -jar consulta-ciudadanos.jar > $INSTANCE_ID.log 2>&1 &"
done