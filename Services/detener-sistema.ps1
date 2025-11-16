# ========================================
# Script para Detener el Sistema SOA
# Sistema de Venta de Entradas
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DETENIENDO SISTEMA SOA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Obtener procesos Java
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "Procesos Java encontrados:" -ForegroundColor Yellow
    $javaProcesses | Format-Table Id, ProcessName, @{Label="Memoria (MB)"; Expression={[math]::Round($_.WorkingSet / 1MB, 2)}} -AutoSize
    
    Write-Host ""
    $confirm = Read-Host "¿Deseas detener todos los procesos Java? (S/N)"
    
    if ($confirm -eq "S" -or $confirm -eq "s") {
        Write-Host ""
        Write-Host "Deteniendo procesos..." -ForegroundColor Yellow
        
        $javaProcesses | ForEach-Object {
            try {
                Stop-Process -Id $_.Id -Force
                Write-Host "  Proceso $($_.Id) detenido" -ForegroundColor Green
            } catch {
                Write-Host "  Error al detener proceso $($_.Id)" -ForegroundColor Red
            }
        }
        
        Write-Host ""
        Write-Host "Todos los servicios han sido detenidos" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "Operación cancelada" -ForegroundColor Yellow
    }
} else {
    Write-Host "No se encontraron procesos Java corriendo" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
