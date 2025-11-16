# Script para detener todos los servicios SOA

Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "DETENIENDO SERVICIOS SOA" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan

# Detener procesos Java en los puertos específicos
$ports = @(8080, 8081, 8082, 8083, 8084, 8085)

foreach ($port in $ports) {
    Write-Host "`nBuscando proceso en puerto $port..." -ForegroundColor Yellow
    
    $connection = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    
    if ($connection) {
        $processId = $connection.OwningProcess
        $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
        
        if ($process) {
            Write-Host "  Deteniendo proceso: $($process.Name) (PID: $processId)" -ForegroundColor Gray
            Stop-Process -Id $processId -Force
            Write-Host "  ✓ Proceso detenido" -ForegroundColor Green
        }
    } else {
        Write-Host "  No hay proceso corriendo en puerto $port" -ForegroundColor Gray
    }
}

# Detener todos los jobs de PowerShell
Write-Host "`nDeteniendo jobs de PowerShell..." -ForegroundColor Yellow
Get-Job | Stop-Job
Get-Job | Remove-Job
Write-Host "✓ Jobs detenidos" -ForegroundColor Green

Write-Host "`n═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "TODOS LOS SERVICIOS DETENIDOS" -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════" -ForegroundColor Cyan
