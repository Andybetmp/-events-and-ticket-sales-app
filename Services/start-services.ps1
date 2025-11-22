# Script para iniciar servicios en la terminal actual de VS Code
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "INICIANDO SERVICIOS SOA" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

$basePath = "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\"

Write-Host "`nIniciando servicios como Jobs en PowerShell..." -ForegroundColor Yellow
Write-Host "Los logs estarán disponibles en archivos .log`n" -ForegroundColor Gray

# Crear carpeta para logs
New-Item -ItemType Directory -Force -Path "$basePath\logs" | Out-Null

# Detener servicios anteriores
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# Iniciar cada servicio como Job
Write-Host "[1/7] Iniciando user-service (puerto 8081)..." -ForegroundColor Cyan
Start-Job -Name "user-service" -ScriptBlock {
    Set-Location "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\user-service"
    java -jar target\user-service-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath "..\logs\user-service.log"
} | Out-Null
Start-Sleep -Seconds 12

Write-Host "[2/7] Iniciando event-service (puerto 8082)..." -ForegroundColor Cyan
Start-Job -Name "event-service" -ScriptBlock {
    Set-Location "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\event-service"
    java -jar target\event-service-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath "..\logs\event-service.log"
} | Out-Null
Start-Sleep -Seconds 12

Write-Host "[3/7] Iniciando orchestration-service (puerto 8083)..." -ForegroundColor Cyan
Start-Job -Name "orchestration-service" -ScriptBlock {
    Set-Location "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\orchestration-service"
    java -jar target\orchestration-service-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath "..\logs\orchestration-service.log"
} | Out-Null
Start-Sleep -Seconds 12

Write-Host "[4/7] Iniciando payment-service (puerto 8084)..." -ForegroundColor Cyan
Start-Job -Name "payment-service" -ScriptBlock {
    Set-Location "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\payment-service"
    java -jar target\payment-service-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath "..\logs\payment-service.log"
} | Out-Null
Start-Sleep -Seconds 12

Write-Host "[5/7] Iniciando notification-service (puerto 8085)..." -ForegroundColor Cyan
Start-Job -Name "notification-service" -ScriptBlock {
    Set-Location "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\notification-service"
    java -jar target\notification-service-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath "..\logs\notification-service.log"
} | Out-Null
Start-Sleep -Seconds 12

Write-Host "[6/7] Iniciando ticket-service (puerto 8086)..." -ForegroundColor Cyan
Start-Job -Name "ticket-service" -ScriptBlock {
    Set-Location "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\ticket-service"
    java -jar target\ticket-service-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath "..\logs\ticket-service.log"
} | Out-Null
Start-Sleep -Seconds 12

Write-Host "[7/7] Iniciando gateway (puerto 8080)..." -ForegroundColor Cyan
Start-Job -Name "gateway" -ScriptBlock {
    Set-Location "C:\Users\pc\Documents\-events-and-ticket-sales-app-main\Services\gateway"
    java -jar target\gateway-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath "..\logs\gateway.log"
} | Out-Null
Start-Sleep -Seconds 12

Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Yellow
Write-Host "VERIFICANDO SERVICIOS" -ForegroundColor Yellow
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Yellow

Start-Sleep -Seconds 5

# Verificar Jobs
Write-Host "`nEstado de los Jobs:" -ForegroundColor Cyan
Get-Job | Format-Table -Property Id, Name, State

# Verificar puertos
$ports = @()
foreach ($port in 8080,8081,8082,8083,8084,8085,8086) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $ports += $port
    }
}

Write-Host "`nServicios activos: $($ports.Count)/7" -ForegroundColor $(if ($ports.Count -eq 7) { "Green" } else { "Yellow" })
if ($ports.Count -gt 0) {
    Write-Host "Puertos escuchando: $($ports -join ', ')" -ForegroundColor Green
}

if ($ports.Count -eq 7) {
    Write-Host "`n✅ TODOS LOS SERVICIOS INICIADOS CORRECTAMENTE`n" -ForegroundColor Green
} else {
    Write-Host "`n⚠️  Algunos servicios están iniciando, espera unos segundos más" -ForegroundColor Yellow
    Write-Host "Puedes revisar los logs en la carpeta 'logs\'`n" -ForegroundColor Gray
}

Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "COMANDOS ÚTILES" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ver estado de servicios:" -ForegroundColor Yellow
Write-Host "  Get-Job" -ForegroundColor White
Write-Host ""
Write-Host "Ver logs de un servicio:" -ForegroundColor Yellow
Write-Host "  Get-Content logs\user-service.log -Tail 20 -Wait" -ForegroundColor White
Write-Host ""
Write-Host "Detener todos los servicios:" -ForegroundColor Yellow
Write-Host "  Get-Job | Stop-Job; Get-Job | Remove-Job" -ForegroundColor White
Write-Host "  Get-Process java | Stop-Process -Force" -ForegroundColor White
Write-Host ""
Write-Host "O usar el script:" -ForegroundColor Yellow
Write-Host "  .\stop-services.ps1" -ForegroundColor White
Write-Host ""
Write-Host "Probar el sistema:" -ForegroundColor Yellow
Write-Host "  .\test-e2e.ps1" -ForegroundColor White
Write-Host ""
