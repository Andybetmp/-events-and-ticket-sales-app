# Script para detener todos los servicios SOA con Camunda

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "DETENIENDO SERVICIOS SOA CON CAMUNDA" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

# Paso 1: Detener Jobs de PowerShell
Write-Host "`n[1/3] Deteniendo Jobs de PowerShell..." -ForegroundColor Yellow
Get-Job -ErrorAction SilentlyContinue | Stop-Job -ErrorAction SilentlyContinue
Get-Job -ErrorAction SilentlyContinue | Remove-Job -ErrorAction SilentlyContinue
Write-Host "  OK - Jobs detenidos" -ForegroundColor Green

# Paso 2: Detener procesos Java
Write-Host "`n[2/3] Deteniendo procesos Java..." -ForegroundColor Yellow
$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Host "  OK - $($javaProcs.Count) procesos Java detenidos" -ForegroundColor Green
} else {
    Write-Host "  OK - No hay procesos Java corriendo" -ForegroundColor Green
}

# Paso 3: Verificar puertos
Write-Host "`n[3/3] Verificando puertos..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
$portsInUse = 0
8080..8086 | ForEach-Object {
    $conn = Get-NetTCPConnection -LocalPort $_ -State Listen -ErrorAction SilentlyContinue
    if ($conn) { $portsInUse++ }
}

if ($portsInUse -eq 0) {
    Write-Host "  OK - Todos los puertos liberados (8080-8086)" -ForegroundColor Green
} else {
    Write-Host "  AVISO - $portsInUse puertos aun ocupados" -ForegroundColor Yellow
}

Write-Host "`n================================================================" -ForegroundColor Cyan
Write-Host "SERVICIOS DETENIDOS" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Cyan

