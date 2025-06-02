# Sistema de Votación 

Este sistema simula una infraestructura distribuida para la gestión de elecciones, permitiendo registrar y consolidar votos desde múltiples estaciones de votación hacia un servidor central, con garantía de confiabilidad y unicidad mediante el uso de técnicas como mensajes confiables (Reliable Messaging) y generación de identificadores únicos para los votos.

## Integrantes

- Mariana De La Cruz - A00399618  
- Juan Andrés Cano - A00399560  
- Juan David Quintero - A00399438  

---

## Configuración del Entorno

### Cambiar JAVA_HOME solo en tu consola actual (Git Bash)

#### 1. Establece JAVA_HOME y PATH temporalmente:

> **Ajusta las versiones según la que tengas instalada en tu equipo.** Asegúrate de que el path coincida con el directorio real de tu JDK.

#### Para Java 11:

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-11.0.X.X-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
```

#### Para volver a Java 17:

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.X.X-hotspot"
export PATH="$JAVA_HOME/bin:$PATH"
```


#### 2. Verifica la versión de Java:

```bash
java -version
```

Debe mostrar algo como:
```
java version "11.XX.XX" ...
```

#### 3. Verificar la versión de gradle

```bash
gradle --version
```

Debe mostrar algo como:
```
------------------------------------------------------------
Gradle 6.6
------------------------------------------------------------
```
> Solo funciona con esa versión

---

## Compilar y Ejecutar el Proyecto
> 1. Clonar el repositorio
> git clone https://github.com/Juanda2005123/Ice---Votacion.git
> 2. Ruta donde deverias estar parado
> cd ice---votacion/sistema-votacion


### Para compilar todo:

```bash
./gradlew clean build
```
o

```bash
./gradlew clean
```


### Para compilar y ejecutar 
> Cabe aclarar que son en dos consolas diferentes

#### **1. Servidor Central**:

```bash
./gradlew :servidor-central:jar

java -cp servidor-central/build/libs/servidor-central.jar ServidorCentralApp
```

#### **2. Mesa de Votación**:

```bash
./gradlew :mesa-votacion:jar

java -cp mesa-votacion/build/libs/mesa-votacion.jar MesaVotacionApp
```

---

## Recomendaciones

- Ejecuta `java -version` antes de compilar para asegurarte de estar usando la versión correcta.
- Puedes alternar entre versiones de Java con los comandos `export JAVA_HOME` descritos arriba.
