# 🌐 Despliegue con ngrok - Exposición Pública Local

Sistema SOA Ticketing - URL pública desde tu localhost (100% Gratis)

## 📋 ¿Qué es ngrok?

ngrok crea un túnel seguro que expone tu servidor local (localhost) a internet con una URL pública.

**Perfecto para:**
- ✅ Demos y presentaciones
- ✅ Proyectos académicos
- ✅ Pruebas rápidas
- ✅ Sin necesidad de desplegar en la nube

---

## 💰 Plan Gratuito

- ✅ **Totalmente gratis**
- ✅ **Sin tarjeta de crédito**
- ✅ **Sin límite de tiempo**
- ⚠️ La URL cambia cada vez que reinicias ngrok
- ⚠️ Requiere que tu PC esté encendida

---

## 🚀 Instalación y Configuración

### 1️⃣ Descargar e Instalar ngrok

**Opción A: Chocolatey (Recomendado)**
```powershell
choco install ngrok
```

**Opción B: Descarga Manual**
1. Ve a https://ngrok.com/download
2. Descarga la versión para Windows
3. Descomprime el archivo
4. Mueve `ngrok.exe` a una carpeta en tu PATH o úsalo desde donde está

### 2️⃣ Crear Cuenta (Gratis)

1. Ve a https://dashboard.ngrok.com/signup
2. Regístrate con email o GitHub
3. Copia tu **Authtoken** del dashboard

### 3️⃣ Autenticar ngrok

```powershell
ngrok config add-authtoken TU_TOKEN_AQUI
```

---

## 🎯 Cómo Usar con tu Proyecto SOA

### Paso 1: Inicia tu sistema localmente

**Terminal 1 - Backend:**
```powershell
cd 'D:\Tareas de programacion\SOA'
.\start-services-camunda.ps1
```
Esto iniciará MySQL, Camunda y todos los microservicios.

**Terminal 2 - Frontend:**
```powershell
cd 'D:\Tareas de programacion\SOA\Frontend'
npm run dev
```
El frontend se iniciará en el puerto 5173 o 5174 (verifica en la terminal).

Verifica que todo esté corriendo:
- Frontend: http://localhost:5173 (o el puerto que muestre)
- Gateway: http://localhost:8080

### Paso 2: Exponer el Frontend con ngrok

**Terminal 3 - ngrok:**
```powershell
cd 'D:\Tareas de programacion\SOA'
.\ngrok.exe http 5173
```
⚠️ **Importante:** Usa el puerto que muestre tu frontend (5173 o 5174).

Obtendrás una URL como: `https://abc123.ngrok-free.dev`

### Paso 3: Configuración de Vite (Ya configurado)

El archivo `Frontend/vite.config.js` ya está configurado para aceptar hosts de ngrok:

```javascript
server: {
  host: true,
  allowedHosts: [
    'localhost',
    '.ngrok-free.dev',
    '.ngrok.io',
    '.ngrok-free.app'
  ]
}
```

✅ **No necesitas hacer cambios adicionales.**

### Paso 4: Acceder a tu aplicación

- **URL pública**: La que ngrok te generó (ej: https://abc123.ngrok-free.dev)
- **Primera vez**: Aparecerá una advertencia de ngrok, haz clic en "Visit Site"
- **Listo**: Tu aplicación es accesible públicamente

---

## 🔧 Configuración Avanzada (Múltiples Servicios)

Si quieres exponer múltiples servicios, crea un archivo de configuración:

**ngrok.yml**
```yaml
version: "2"
authtoken: TU_TOKEN_AQUI
tunnels:
  frontend:
    proto: http
    addr: 5173
  gateway:
    proto: http
    addr: 8080
```

Inicia todos los túneles:
```powershell
ngrok start --all --config ngrok.yml
```

---

## 📊 Flujo de Trabajo para Demos

### Antes de la Presentación:

1. **Inicia tu sistema local:**
   ```powershell
   # Terminal 1 - Backend
   cd 'D:\Tareas de programacion\SOA'
   .\start-services-camunda.ps1
   
   # Terminal 2 - Frontend
   cd 'D:\Tareas de programacion\SOA\Frontend'
   npm run dev
   ```
   ⚠️ Espera a que todos los servicios estén listos (1-2 minutos).

2. **Verifica el puerto del frontend:**
   - Revisa la terminal 2
   - Busca la línea: `Local: http://localhost:5173` (o 5174)
   - Anota el puerto

3. **Inicia ngrok:**
   ```powershell
   # Terminal 3 - ngrok (usa el puerto de tu frontend)
   cd 'D:\Tareas de programacion\SOA'
   .\ngrok.exe http 5173
   ```

4. **Copia la URL pública:**
   - Busca la línea que dice: `url=https://xxxxx.ngrok-free.dev`
   - Esa es tu URL pública

5. **Prueba la URL:**
   - Abre la URL en tu navegador
   - Primera vez: Clic en "Visit Site" en la advertencia de ngrok
   - Verifica que cargue correctamente

6. **Comparte la URL** con tu profesor/audiencia

### Durante la Presentación:

- Usa la URL pública de ngrok
- Tu aplicación está accesible desde cualquier lugar
- Funciona exactamente igual que localhost

### Después de la Presentación:

```powershell
# Detener ngrok (Ctrl+C en la terminal de ngrok)
# Detener frontend (Ctrl+C en la terminal de npm)
# Detener backend
.\stop-services.ps1
```

Las URLs se invalidan automáticamente al detener ngrok.

---

## 🎭 Alternativa: ngrok con Docker Compose

Puedes integrar ngrok en tu docker-compose:

```yaml
# Agregar al docker-compose.yml
  ngrok:
    image: ngrok/ngrok:latest
    restart: unless-stopped
    command:
      - "start"
      - "--all"
      - "--config"
      - "/etc/ngrok.yml"
    volumes:
      - ./ngrok.yml:/etc/ngrok.yml
    ports:
      - 4040:4040  # Dashboard de ngrok
```

Dashboard de ngrok: http://localhost:4040

---

## 💡 Consejos para Demos

### 1. URL Personalizada (Plan Pagado)
Si quieres una URL fija como `https://soa-ticketing.ngrok.io`, necesitas el plan pagado (~$8/mes).

### 2. Verificación de Túnel
Antes de la demo, verifica que todo funcione:
```powershell
# Ver estado de túneles activos
ngrok status

# O abre el dashboard
http://localhost:4040
```

### 3. Velocidad
ngrok puede ser un poco lento. Si es crítico:
- Usa una conexión a internet rápida
- Prueba antes de la presentación
- Ten un plan B (video de demostración)

### 4. HTTPS Automático
ngrok proporciona HTTPS gratis automáticamente. No necesitas certificados.

---

## 🐛 Troubleshooting

### Error: "ngrok not found"
- Asegúrate de haber instalado ngrok correctamente
- Reinicia PowerShell después de instalar

### Error: "Unauthorized"
- Configura tu authtoken: `ngrok config add-authtoken TU_TOKEN`
- Verifica que el token sea correcto

### Frontend no se conecta al Backend
- Asegúrate de que ambos túneles estén activos
- Verifica que el frontend use la URL correcta del Gateway
- Revisa la configuración de CORS en el Gateway

### Conexión muy lenta
- ngrok gratuito puede tener latencia
- Normal en plan gratuito
- Considera hacer la demo en localhost y compartir pantalla

---

## 📚 Comandos Útiles

```powershell
# Exponer puerto del frontend
.\ngrok.exe http 5173

# Ver túneles activos (dashboard web local)
# Abre: http://localhost:4040

# Detener todos los procesos de ngrok
Get-Process | Where-Object {$_.ProcessName -eq "ngrok"} | Stop-Process -Force

# Verificar qué puerto usa el frontend
netstat -ano | Select-String ":5173"
```

---

## 🎯 Resumen Rápido

**Para exponer tu aplicación:**

1. Inicia backend: `.\start-services-camunda.ps1`
2. Inicia frontend: `cd Frontend; npm run dev`
3. Verifica el puerto del frontend (5173 o 5174)
4. Inicia ngrok: `.\ngrok.exe http 5173` (usa el puerto correcto)
5. Copia la URL que genera ngrok
6. Comparte la URL - ¡listo!

**Cuando termines:**
- Ctrl+C en ngrok
- Ctrl+C en frontend
- `.\stop-services.ps1` para backend

---

## ✅ Checklist para Demo

- [ ] Sistema local corriendo (Docker o scripts)
- [ ] Frontend accesible en localhost:5173
- [ ] Gateway accesible en localhost:8080
- [ ] ngrok instalado y autenticado
- [ ] Túnel de frontend activo
- [ ] Túnel de gateway activo
- [ ] URL pública probada
- [ ] CORS configurado correctamente
- [ ] Demo funcional

---

## 🎯 Ventajas vs Desventajas

### ✅ Ventajas:
- Gratis permanentemente
- No requiere despliegue
- Setup en minutos
- HTTPS automático
- Perfecto para demos

### ⚠️ Desventajas:
- Requiere PC encendida
- URL cambia cada reinicio (plan gratuito)
- Puede tener latencia
- No es para producción

---

✅ **Con ngrok, tu aplicación local es accesible públicamente en minutos.**

**Perfecto para:** Presentaciones académicas, demos y pruebas rápidas.

**Última actualización**: Diciembre 2025
