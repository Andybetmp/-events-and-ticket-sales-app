# Test E2E completo - Sistema SOA Ticketing
$ErrorActionPreference = "Continue"
$gateway = "http://localhost:8080"

Write-Host "`n========== TEST E2E - TICKETING SOA ==========" -ForegroundColor Cyan

# PASO 1: Registro
Write-Host "`n[1/5] Registrando usuario..." -ForegroundColor Yellow
$timestamp = Get-Date -Format "HHmmss"
$registerBody = @{
    nombre = "Carlos"
    apellido = "Lopez"
    email = "u20202269@utp.edu.pe"
    contrasena = "pass123456"
}

$user = Invoke-RestMethod -Uri "$gateway/api/orchestration/register" -Method Post -Body ($registerBody | ConvertTo-Json) -ContentType "application/json"
Write-Host "  OK - Usuario: $($user.usuario.nombre) $($user.usuario.apellido)" -ForegroundColor Green

# PASO 2: Login
Write-Host "`n[2/5] Login..." -ForegroundColor Yellow
$loginBody = @{ email = $user.usuario.email; contrasena = "pass123456" }
$login = Invoke-RestMethod -Uri "$gateway/api/users/login" -Method Post -Body ($loginBody | ConvertTo-Json) -ContentType "application/json"
$token = $login.token
Write-Host "  OK - JWT obtenido" -ForegroundColor Green

# PASO 3: Crear evento
Write-Host "`n[3/5] Creando evento..." -ForegroundColor Yellow
$eventoBody = @{
    nombre = "Rock Fest 2026"
    descripcion = "Festival de rock"
    ubicacion = "Estadio"
    fechaEvento = "2026-06-20T20:00:00"
    categoria = "Musica"
    tiposEntrada = @(
        @{ nombre = "General"; descripcion = "Entrada general"; precio = 60.00; cantidad = 800; orden = 1 }
    )
}
$headers = @{ "Authorization" = "Bearer $token" }
$evento = Invoke-RestMethod -Uri "$gateway/api/orchestration/create-event" -Method Post -Body ($eventoBody | ConvertTo-Json -Depth 3) -Headers $headers -ContentType "application/json"
Write-Host "  OK - Evento: $($evento.nombre) (ID: $($evento.id))" -ForegroundColor Green

# PASO 4: Comprar ticket
Write-Host "`n[4/5] Comprando tickets..." -ForegroundColor Yellow
$purchaseBody = @{
    tipoEntradaId = $evento.tiposEntrada[0].id
    cantidad = 2
    paymentMethod = @{
        cardNumber = "4532123456789012"
        cvv = "123"
        expiryDate = "12/28"
        cardHolder = "CARLOS LOPEZ"
    }
}
$ticket = Invoke-RestMethod -Uri "$gateway/api/orchestration/purchase-ticket" -Method Post -Body ($purchaseBody | ConvertTo-Json -Depth 3) -Headers $headers -ContentType "application/json"
Write-Host "  OK - Ticket ID: $($ticket.id) - Total: $($ticket.totalPagado)" -ForegroundColor Green

# PASO 5: Consultar tickets
Write-Host "`n[5/5] Consultando mis tickets..." -ForegroundColor Yellow
$tickets = Invoke-RestMethod -Uri "$gateway/api/orchestration/my-tickets" -Method Get -Headers $headers
Write-Host "  OK - Total tickets: $($tickets.Count)" -ForegroundColor Green

Write-Host "`n========== PRUEBAS COMPLETADAS ==========" -ForegroundColor Green
Write-Host "REGISTRO + LOGIN + EVENTO + COMPRA + CONSULTA" -ForegroundColor White
Write-Host "Revisa los logs de notification-service para ver las 3 notificaciones" -ForegroundColor Yellow
