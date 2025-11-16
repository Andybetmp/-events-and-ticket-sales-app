# ========================================
# Script de Inicio del Sistema SOA
# Sistema de Venta de Entradas
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INICIANDO SISTEMA SOA - TICKETING" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar MySQL
Write-Host "1. Verificando MySQL..." -ForegroundColor Yellow
$mysql = Get-Process mysqld -ErrorAction SilentlyContinue
if ($mysql) {
    Write-Host "   MySQL está corriendo (PID: $($mysql.Id))" -ForegroundColor Green
} else {
    Write-Host "   MySQL NO está corriendo!" -ForegroundColor Red
    Write-Host "   Por favor inicia MySQL desde XAMPP" -ForegroundColor Yellow
    Read-Host "Presiona Enter cuando MySQL esté corriendo"
}
Write-Host ""

# Compilar User-Service
Write-Host "2. Compilando User-Service..." -ForegroundColor Yellow
cd 'd:\Tareas de programacion\SOA\user-service'
$compile = mvn clean package -DskipTests 2>&1 | Select-String "BUILD SUCCESS"
if ($compile) {
    Write-Host "   User-Service compilado correctamente" -ForegroundColor Green
} else {
    Write-Host "   Error al compilar User-Service" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Iniciar User-Service
Write-Host "3. Iniciando User-Service (puerto 8081)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd 'd:\Tareas de programacion\SOA\user-service'; Write-Host 'USER-SERVICE (8081)' -ForegroundColor Cyan; java -jar target/user-service-0.0.1-SNAPSHOT.jar" -WindowStyle Normal
Write-Host "   Esperando 15 segundos para que inicie..." -ForegroundColor Gray
Start-Sleep -Seconds 15
Write-Host "   User-Service iniciado" -ForegroundColor Green
Write-Host ""

# Iniciar Gateway
Write-Host "4. Iniciando API Gateway (puerto 8080)..." -ForegroundColor Yellow
cd 'd:\Tareas de programacion\SOA\gateway'
Start-Process powershell -ArgumentList '-NoExit', '-Command', "cd 'd:\Tareas de programacion\SOA\gateway'; Write-Host 'API GATEWAY (8080)' -ForegroundColor Cyan; java -jar target/gateway-0.0.1-SNAPSHOT.jar" -WindowStyle Normal
Write-Host "   Esperando 15 segundos para que inicie..." -ForegroundColor Gray
Start-Sleep -Seconds 15
Write-Host "   Gateway iniciado" -ForegroundColor Green
Write-Host ""

# Verificar servicios
Write-Host "5. Verificando servicios..." -ForegroundColor Yellow
Write-Host ""

# Gateway
try {
    $gw = Invoke-RestMethod -Uri "http://localhost:8080/health" -TimeoutSec 5
    Write-Host "   Gateway (8080): $($gw.status)" -ForegroundColor Green
} catch {
    Write-Host "   Gateway (8080): ERROR" -ForegroundColor Red
}

# User-Service via Gateway
try {
    $us = Invoke-RestMethod -Uri "http://localhost:8080/api/users/health" -TimeoutSec 5
    Write-Host "   User-Service via Gateway: OK" -ForegroundColor Green
} catch {
    Write-Host "   User-Service via Gateway: ERROR" -ForegroundColor Red
}

# Acceso directo (debe fallar)
try {
    $direct = Invoke-RestMethod -Uri "http://localhost:8081/api/users/health" -TimeoutSec 3 -ErrorAction Stop
    Write-Host "   Seguridad: ALERTA - Acceso directo permitido" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "   Seguridad: OK - Acceso directo bloqueado" -ForegroundColor Green
    } else {
        Write-Host "   Seguridad: ERROR inesperado" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SISTEMA INICIADO CORRECTAMENTE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "URLs disponibles:" -ForegroundColor White
Write-Host "  Gateway:              http://localhost:8080" -ForegroundColor Gray
Write-Host "  Gateway Health:       http://localhost:8080/health" -ForegroundColor Gray
Write-Host "  User Service:         http://localhost:8080/api/users/**" -ForegroundColor Gray
Write-Host "  Gateway Swagger:      http://localhost:8080/swagger-ui.html" -ForegroundColor Gray
Write-Host "  User-Service Swagger: http://localhost:8081/swagger-ui.html" -ForegroundColor Gray
Write-Host ""
Write-Host "Prueba rápida:" -ForegroundColor Yellow
Write-Host '  $response = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -ContentType "application/json" -Body ''{"email":"test@example.com","contrasena":"Password123","nombre":"Juan","apellido":"Perez","telefono":"123456789"}''' -ForegroundColor Gray
Write-Host ""
Write-Host "Para detener los servicios:" -ForegroundColor Yellow
Write-Host "  Get-Process -Name java | Stop-Process -Force" -ForegroundColor Gray
Write-Host ""
