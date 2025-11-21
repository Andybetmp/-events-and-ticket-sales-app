# Script para detener todos los servicios SOA

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "DETENIENDO SERVICIOS SOA" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

# Detener todos los jobs de PowerShell primero
Write-Host "`nDeteniendo jobs de PowerShell..." -ForegroundColor Yellow
Get-Job | Stop-Job -ErrorAction SilentlyContinue
Get-Job | Remove-Job -ErrorAction SilentlyContinue
Write-Host "  OK - Jobs detenidos" -ForegroundColor Green

# Esperar un momento
Start-Sleep -Seconds 2

# Detener todos los procesos Java
Write-Host "`nDeteniendo todos los procesos Java..." -ForegroundColor Yellow
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue

if ($javaProcesses) {
    $count = ($javaProcesses | Measure-Object).Count
    Write-Host "  Encontrados $count procesos Java" -ForegroundColor Gray
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  OK - Procesos detenidos" -ForegroundColor Green
} else {
    Write-Host "  No hay procesos Java corriendo" -ForegroundColor Gray
}

# Verificar puertos
Start-Sleep -Seconds 2
Write-Host "`nVerificando puertos..." -ForegroundColor Yellow
$ports = @(8080, 8081, 8082, 8083, 8084, 8085, 8086)
$activePorts = @()

foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $activePorts += $port
    }
}

if ($activePorts.Count -eq 0) {
    Write-Host "  OK - Todos los puertos liberados" -ForegroundColor Green
} else {
    Write-Host "  Advertencia: Puertos aun activos: $($activePorts -join ', ')" -ForegroundColor Yellow
}

Write-Host "`n================================================================" -ForegroundColor Cyan
Write-Host "SERVICIOS DETENIDOS" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
