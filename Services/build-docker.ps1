# ===========================================================================
# Script de Compilacion y Construccion de Imagenes Docker
# ===========================================================================

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "==========================================================="-ForegroundColor Cyan
Write-Host "  COMPILANDO Y CONSTRUYENDO IMAGENES DOCKER (Backend)      " -ForegroundColor Cyan
Write-Host "===========================================================" -ForegroundColor Cyan
Write-Host ""

# Lista de servicios backend
$services = @(
    "gateway",
    "user-service",
    "event-service",
    "camunda-service",
    "payment-service",
    "notification-service",
    "ticket-service",
    "image-service"
)

$total = $services.Count
$current = 0

# Compilar cada servicio con Maven
foreach ($service in $services) {
    $current++
    Write-Host "[$current/$total] Compilando $service..." -ForegroundColor Yellow
    
    Push-Location $service
    
    try {
        mvn clean package -DskipTests -q
        if ($LASTEXITCODE -ne 0) {
            throw "Error compilando $service"
        }
        Write-Host "  OK - $service compilado exitosamente" -ForegroundColor Green
    }
    catch {
        Write-Host "  ERROR compilando $service : $_" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    
    Pop-Location
}

Write-Host ""
Write-Host "===========================================================" -ForegroundColor Cyan
Write-Host "     CONSTRUYENDO IMAGENES DOCKER (Backend + Frontend)    " -ForegroundColor Cyan
Write-Host "===========================================================" -ForegroundColor Cyan
Write-Host ""

# Construir imágenes Docker (incluye frontend)
Write-Host "Construyendo imágenes con docker-compose..." -ForegroundColor Yellow
Write-Host "  - Backend services (Java Spring Boot)" -ForegroundColor Gray
Write-Host "  - Frontend (React + Vite + Nginx)" -ForegroundColor Gray
docker-compose build --no-cache

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR construyendo imagenes Docker" -ForegroundColor Red
    exit 1
}

Write-Host "OK - Imagenes Docker construidas exitosamente" -ForegroundColor Green

Write-Host ""
Write-Host "===========================================================" -ForegroundColor Cyan
Write-Host "     INICIANDO SERVICIOS                                   " -ForegroundColor Cyan
Write-Host "===========================================================" -ForegroundColor Cyan
Write-Host ""

# Iniciar servicios
Write-Host "Iniciando todos los servicios con docker-compose..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR iniciando servicios" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "===========================================================" -ForegroundColor Green
Write-Host "     OK - SISTEMA INICIADO EXITOSAMENTE                    " -ForegroundColor Green
Write-Host "===========================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Servicios corriendo:" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "Comandos utiles:" -ForegroundColor Yellow
Write-Host "  Abrir aplicacion:   http://localhost" -ForegroundColor Cyan
Write-Host "  Ver logs:           docker-compose logs -f" -ForegroundColor White
Write-Host "  Ver un servicio:    docker-compose logs -f gateway" -ForegroundColor White
Write-Host "  Ver frontend:       docker-compose logs -f frontend" -ForegroundColor White
Write-Host "  Detener todo:       docker-compose down" -ForegroundColor White
Write-Host "  Estado:             docker-compose ps" -ForegroundColor White
Write-Host ""
Write-Host "Espera ~30 segundos para que todos los servicios esten listos" -ForegroundColor Yellow
Write-Host "Luego abre tu navegador en: http://localhost" -ForegroundColor Cyan
Write-Host "O prueba desde terminal: .\test-e2e.ps1" -ForegroundColor Cyan
Write-Host ""
