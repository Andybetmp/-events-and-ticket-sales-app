# Script para iniciar todos los servicios SOA
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "INICIANDO SERVICIOS SOA TICKETING SYSTEM" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$basePath = "d:\Tareas de programacion\SOA"

# Compilar todos los servicios primero
Write-Host "`nCompilando servicios..." -ForegroundColor Yellow
Set-Location $basePath
mvn clean package -DskipTests -q

Write-Host "`n✓ Compilación completada" -ForegroundColor Green

# Iniciar cada servicio en una nueva ventana de PowerShell
Write-Host "`n[1/6] Iniciando user-service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$basePath\user-service'; java -jar target\user-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 10

Write-Host "[2/6] Iniciando event-service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$basePath\event-service'; java -jar target\event-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 10

Write-Host "[3/6] Iniciando orchestration-service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$basePath\orchestration-service'; java -jar target\ticket-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 10

Write-Host "[4/6] Iniciando payment-service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$basePath\payment-service'; java -jar target\payment-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 10

Write-Host "[5/6] Iniciando notification-service..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$basePath\notification-service'; java -jar target\notification-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 8

Write-Host "[6/6] Iniciando gateway..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$basePath\gateway'; java -jar target\gateway-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 8

Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "SERVICIOS INICIADOS" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

Write-Host "`nServicios corriendo en ventanas separadas:" -ForegroundColor White
Write-Host "  • Gateway:              http://localhost:8080" -ForegroundColor Gray
Write-Host "  • User Service:         http://localhost:8081" -ForegroundColor Gray
Write-Host "  • Event Service:        http://localhost:8082" -ForegroundColor Gray
Write-Host "  • Orchestration:        http://localhost:8083" -ForegroundColor Gray
Write-Host "  • Payment Service:      http://localhost:8084" -ForegroundColor Gray
Write-Host "  • Notification Service: http://localhost:8085" -ForegroundColor Gray

Write-Host "`nEsperando 15 segundos para que todos los servicios inicien completamente..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "`nVerificando servicios..." -ForegroundColor Yellow
$services = @(
    @{Name="Gateway"; Port=8080},
    @{Name="User Service"; Port=8081},
    @{Name="Event Service"; Port=8082},
    @{Name="Orchestration"; Port=8083},
    @{Name="Payment Service"; Port=8084},
    @{Name="Notification"; Port=8085}
)

foreach ($service in $services) {
    $connection = Get-NetTCPConnection -LocalPort $service.Port -ErrorAction SilentlyContinue
    if ($connection) {
        Write-Host "  ✓ $($service.Name) (puerto $($service.Port))" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ $($service.Name) (puerto $($service.Port)) - Aún iniciando o falló" -ForegroundColor Yellow
    }
}

Write-Host "`nPara probar el sistema ejecuta:" -ForegroundColor Cyan
Write-Host "  .\test-orchestration.ps1" -ForegroundColor White

Write-Host "`nPara detener todos los servicios:" -ForegroundColor Cyan
Write-Host "  .\stop-services.ps1" -ForegroundColor White

Write-Host "`n✅ Script completado" -ForegroundColor Green
