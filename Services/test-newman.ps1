# ===============================================
# Script: test-newman.ps1
# Descripcion: Ejecuta pruebas E2E con Newman
# ===============================================

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "   Newman E2E Tests - SOA Ticketing" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Verificar si Newman esta instalado
Write-Host "[1/4] Verificando Newman..." -ForegroundColor Yellow
$newmanInstalled = Get-Command newman -ErrorAction SilentlyContinue

if (-not $newmanInstalled) {
    Write-Host "ERROR: Newman no esta instalado" -ForegroundColor Red
    Write-Host ""
    Write-Host "Instalalo con npm:" -ForegroundColor Yellow
    Write-Host "  npm install -g newman" -ForegroundColor White
    Write-Host ""
    Write-Host "O ejecuta las pruebas en Postman importando:" -ForegroundColor Yellow
    Write-Host "  SOA-Ticketing.postman_collection.json" -ForegroundColor White
    exit 1
}

Write-Host "OK Newman esta instalado" -ForegroundColor Green
Write-Host ""

# Verificar que los servicios esten corriendo (opcional)
Write-Host "[2/4] Nota: Asegurate de que los servicios esten iniciados" -ForegroundColor Yellow
Write-Host "      Usa: .\start-services.ps1" -ForegroundColor Gray
Write-Host ""

# Ejecutar coleccion con Newman
Write-Host "[3/4] Ejecutando coleccion de Postman..." -ForegroundColor Yellow
Write-Host ""

newman run SOA-Ticketing.postman_collection.json --color on --delay-request 500

$exitCode = $LASTEXITCODE

Write-Host ""
Write-Host "[4/4] Resultados" -ForegroundColor Yellow

if ($exitCode -eq 0) {
    Write-Host "OK Todas las pruebas pasaron exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "Reporte JSON generado: newman-results.json" -ForegroundColor Cyan
} else {
    Write-Host "ERROR Algunas pruebas fallaron" -ForegroundColor Red
    Write-Host ""
    Write-Host "Revisa los detalles arriba o el archivo: newman-results.json" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan

exit $exitCode
