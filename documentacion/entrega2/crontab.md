# Guía de Programación de Tareas con Crontab y WSL

Este documento resume los pasos, comandos y configuraciones necesarias para empaquetar la aplicación, preparar el entorno de ejecución en WSL (Ubuntu) y programar la ejecución periódica mediante `cron`.

---

## 1. Configuración en IntelliJ y Maven

Pasos previos para generar el ejecutable (`.jar`) con sus dependencias:

1. **Crear clase Main:** Asegurarse de tener creada la clase principal en planificación.
2. **Configurar `pom.xml`:** Agregar la configuración `<execution>` correspondiente dentro del `pom.xml`.
3. **Empaquetar el proyecto:** Ejecutar el comando para compilar y generar el artefacto:
   ```bash
   mvn package
   ```

---

## 2. Gestión del Entorno WSL (Ubuntu)

Debido a que la distribución Alpine por defecto en WSL tiene espacio limitado (aprox. 136 MB) y no permite instalar Java, se utiliza **Ubuntu**:

| Acción | Comando | Descripción |
| :--- | :--- | :--- |
| **Instalar Ubuntu** | `wsl --install -d Ubuntu` | Instala la distribución Ubuntu en WSL. |
| **Instalar Java (JRE)** | `sudo apt update && sudo apt install default-jre` | Instala Java Runtime Environment en Ubuntu. |
| **Iniciar Cron** | `sudo service cron start` | Inicia el servicio de programación `cron`. |
| **Detener Cron** | `sudo service cron stop` | Detiene el servicio `cron`. |
| **Estado de Cron** | `sudo service cron status` | Verifica el estado del servicio `cron`. |
| **Monitorear Logs** | `tail -F archivoLog.txt` | Permite ver el contenido de un archivo de log en tiempo real. |
| **Apagar Ubuntu** | `wsl --terminate Ubuntu` | Cierra la instancia en ejecución de Ubuntu. |
| **Apagar WSL** | `wsl --shutdown` | Apaga todas las instancias activas de WSL. |

---

## 3. Comandos de Crontab

Comandos principales para administrar las tareas programadas:

```bash
# Edita el archivo crontab del usuario
crontab -e

# Lista los procesos programados en el crontab
crontab -l
```

---

## 4. Estructura y Ejemplos de Tareas Cron

### Sintaxis General

Cada expresión en `crontab` consta de 5 campos de tiempo seguidos por el comando a ejecutar:

```text
# ┌───────────── minuto (0 - 59)
# │ ┌───────────── hora (0 - 23)
# │ │ ┌───────────── día del mes (1 - 31)
# │ │ │ ┌───────────── mes (1 - 12)
# │ │ │ │ ┌───────────── día de la semana (0 - 6) (Domingo a Sábado)
# │ │ │ │ │
# * * * * * comando >> ruta_output 2>&1
```

> [!NOTE]
> La sintaxis `2>&1` redirige tanto la salida estándar como la salida de errores al archivo especificado.

### Ejemplos Prácticos

1. **Ejecutar el `.jar` cada minuto:**
   ```cron
   * * * * * java -jar "/mnt/c/Users/usuario/dona-track/target/notificador-usuarios-inactivos.jar" >> /mnt/c/Users/usuario/log_cron.txt 2>&1
   ```

2. **Ejecutar el `.jar` a las 3 am de lunes a viernes:**
   ```cron
   0 3 * * 1-5 java -jar "/mnt/c/Users/usuario/dona-track/target/notificador-usuarios-inactivos.jar" >> /mnt/c/Users/usuario/log_cron.txt 2>&1
   ```
