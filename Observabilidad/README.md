1. Prerrequisitos

tener instalado lo siguiente:

k6 (Instalado globalmente).
Instalación rápida: winget install k6

2. Instalación de Herramientas de Observabilidad

Debido a que los ejecutables no se incluyen en el repositorio, debe descargarlos y colocarlos en la carpeta Observabilidad/.

A. Descargas
Loki \& Promtail: Descargar desde GitHub Releases: https://github.com/grafana/loki/releases
Busca loki-windows-amd64.exe.zip
Busca promtail-windows-amd64.exe.zip

Prometheus: Descargar desde web oficial (windows-amd64): https://prometheus.io/download/

Grafana OSS: Descargar desde web oficial (Versión ZIP): https://grafana.com/grafana/download?platform=windows\&edition=oss

B. Estructura de Carpetas

Descomprima los archivos para que la estructura quede exactamente así:

SOA/
├── Observabilidad/
│   ├── loki/
│   │   ├── loki-windows-amd64.exe
│   │   └── loki-config.yaml      <-- (Ya incluido en el repo)
│   ├── promtail/
│   │   ├── promtail-windows-amd64.exe
│   │   └── promtail-config.yaml  <-- (Ya incluido en el repo)
│   ├── prometheus/
│   │   ├── prometheus.exe
│   │   └── prometheus.yml        <-- (Ya incluido en el repo)
│   ├── grafana/
│   │   └── bin/grafana-server.exe
│   └── Pruebas/
│       └── load-test.js          <-- (Script k6 incluido)
3. Ejecución del Sistema

Para iniciar todo, abra PowerShell y ejecute los comandos en el siguiente orden. Se recomienda usar múltiples pestañas.

Paso 1: Base de Datos

Asegúrese de que MySQL esté corriendo en XAMPP y la base de datos ticketing exista.

Paso 2: Backend de Observabilidad (Logs)

Terminal 1 - Loki:

cd Observabilidad\\loki
.\\loki-windows-amd64.exe --config.file=loki-config.yaml

Terminal 2 - Promtail:

cd Observabilidad\\promtail
.\\promtail-windows-amd64.exe --config.file=promtail-config.yaml

Paso 3: Servicios SOA

Terminal 3 - Start Services:

cd Services
mvn clean install -DskipTests
.\\start-services.ps1

Espere hasta que todos los servicios indiquen "Running".

Paso 4: Métricas y Visualización

Terminal 4 - Prometheus:

cd Observabilidad\\prometheus
.\\prometheus.exe --config.file=prometheus.yml

Terminal 5 - Grafana:

cd Observabilidad\\grafana\\bin
.\\grafana-server.exe

4. Configuración de Grafana

Abra el navegador en http://localhost:3000.

User: admin / Pass: admin

Conectar Data Sources:

Ir a Connections > Data Sources > Add new data source

Prometheus:

URL: http://localhost:9090

Click Save \& Test.

Loki:

URL: http://localhost:3100

Click Save \& Test.

Importar Dashboard:

Vaya a Dashboards > New > Import.

Copie el contenido del archivo SOA\_Observabilidad\_Dashboard.json (ubicado en la raíz o carpeta Observabilidad).

Pegue el JSON y haga clic en Load.

Seleccione los Data Sources (Prometheus y Loki) si le pida.

Click Import.

5. Pruebas de Carga (Generar Datos)

Para ver las gráficas en movimiento, debemos generar tráfico con k6.

Terminal 6 - k6:

(Opcional) Si la BD está limpia, cree el usuario base:

cd Services
.\\test-e2e.ps1

Ejecute la prueba de carga:

cd Observabilidad\\Pruebas
k6 run .\\load-test.js

Resultados:
Vaya a Grafana, seleccione el dashboard importado, elija el servicio (ej. soa-services o gateway) y observe:

✅ Tasa de solicitudes: Aumento de tráfico.

✅ Latencia P95: Tiempos de respuesta.

✅ Logs: Logs en tiempo real en la parte inferior. (Falta)

🆘 Solución de Problemas

Gráficas (Tasa de solicitudes, Latencia p95, uptime) "No data": Asegúrese de que Prometheus diga "UP" en http://localhost:9090/targets. Si dice "DOWN", verifique que SecurityConfig permita acceso a /actuator/\*\*.

