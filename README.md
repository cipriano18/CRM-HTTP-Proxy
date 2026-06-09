# CRM-HTTP-Proxy

Proyecto de Comunicación y Redes de Computadores
Universidad Nacional – I Ciclo 2026

## Integrantes

* Makin Artavia Zúñiga
* Cipriano Rivera Escobar
* Reyner Rojas Gutiérrez

---

# Descripción del Proyecto

CRM-HTTP-Proxy es un servidor proxy HTTP desarrollado para interceptar, reenviar y monitorear solicitudes realizadas por clientes dentro de una red local.

El sistema permite:

* Interceptar tráfico HTTP
* Filtrar dominios bloqueados
* Registrar solicitudes realizadas por los clientes
* Monitorear métricas de tráfico en tiempo real
* Visualizar estadísticas mediante un panel web

El proxy funciona como intermediario entre el navegador del cliente e Internet, permitiendo analizar las comunicaciones y aplicar reglas de filtrado.

---

# Tecnologías Utilizadas

## Backend

* Java
* Sockets TCP
* Multithreading

## Frontend

* React
* Vite

## Herramientas

* GitHub
* Visual Studio Code
* NetBeans

---

# Puertos Utilizados

| Servicio      | Puerto |
| ------------- | ------ |
| Proxy HTTP    | 8080   |
| Dashboard Web | 3000   |

---

# Estructura del Proyecto

| Carpeta               | Descripción                                 |
| --------------------- | ------------------------------------------- |
| proxy-block-extension | Extensión utilizada por el navegador        |
| proxy-dashboard       | Dashboard web para monitoreo y estadísticas |
| server-proxy          | Servidor principal del proxy HTTP           |
| README.md             | Archivo de instrucciones del proyecto       |

---

## Estructura de Paquetes del Servidor

Dentro de `server-proxy/src/main/java`, el servidor se organiza por responsabilidad para separar la interfaz, la lógica del proxy y los componentes auxiliares.

| Paquete      | Descripción |
| ------------ | ----------- |
| `app`        | Contiene el punto de entrada de la aplicación JavaFX. |
| `ui`         | Incluye los controladores de la interfaz gráfica del servidor. |
| `proxy.core` | Contiene la lógica principal del proxy, como el servidor y el manejo de clientes. |
| `proxy.tls`  | Agrupa las clases encargadas de procesar conexiones HTTPS, como lectura de `ClientHello` y extracción de SNI. |
| `dashboard`  | Contiene los componentes auxiliares del panel de monitoreo y exposición de métricas. |
| `filter`     | Maneja las reglas de bloqueo de dominios y palabras clave. |
| `logger`     | Contiene la lógica de escritura del archivo de logs del proxy. |

Esta separación facilita el mantenimiento del proyecto y permite identificar con claridad qué parte del sistema corresponde a la interfaz, al procesamiento del tráfico HTTP/HTTPS y al monitoreo.

---

# Ejecución del Servidor Proxy

## 1. Abrir el proyecto en NetBeans

Abrir NetBeans y seleccionar:

```txt
File -> Open Project
```

Luego abrir la carpeta:

```txt
server-proxy
```

---

## 2. Compilar el proyecto

Una vez abierto el proyecto en NetBeans, seleccionar:

```txt
Clean and Build Project
```

Esto compilará automáticamente el servidor proxy.

---

## 3. Ejecutar el proyecto

Después de compilar, seleccionar:

```txt
Run Project
```

---

## 4. Iniciar el servidor

Al ejecutar el proyecto se desplegará la interfaz gráfica del servidor proxy.

Dentro de la interfaz presionar el botón:

```txt
Encender Servidor
```

Cuando el servidor inicie correctamente aparecerá un mensaje similar a:

```txt
Proxy iniciado en puerto 8080
```

Esto indica que el proxy ya se encuentra escuchando solicitudes HTTP correctamente.

---

# Administración de Dominios Bloqueados

El sistema permite agregar y eliminar dominios bloqueados directamente desde el servidor.

Los dominios bloqueados también son almacenados en el archivo:

```txt
blocked.txt
```

Ejemplo:

```txt
facebook.com
youtube.com
tiktok.com
```

Cuando un dominio bloqueado es solicitado, el proxy responderá con una página de restricción.

Ejemplo:

```txt
[BLOQUEADO] youtube.com
```

---

# Configuración de la IP del Servidor en el Dashboard

Antes de ejecutar el dashboard es necesario configurar la dirección IP del servidor proxy.

Abrir el archivo:

```txt
proxy-dashboard/src/config/appConfig.js
```

Modificar la línea:

```js
'http://localhost:8080/api/dashboard/metrics'
```

y reemplazar `localhost` por la dirección IP del equipo donde se está ejecutando el proxy.

Ejemplo:

```js
'http://192.168.1.15:8080/api/dashboard/metrics'
```

La IP utilizada debe ser la misma obtenida anteriormente mediante el comando:

```bash
ipconfig
```

Guardar los cambios antes de ejecutar el dashboard.

---

# Ejecución del Dashboard Web

## 1. Abrir Visual Studio Code

Abrir Visual Studio Code dentro de la carpeta:

```txt
proxy-dashboard
```

---

## 2. Abrir una terminal

Dentro de Visual Studio Code abrir una nueva terminal.

---

## 3. Instalar dependencias

Ejecutar:

```bash
npm install
```

---

## 4. Ejecutar el dashboard

Ejecutar:

```bash
npm run dev
```

Cuando el dashboard inicie correctamente aparecerá algo similar a:

```txt
Local:   http://localhost:3000/
Network: http://192.168.1.15:3000/
```

---

# Acceder al Dashboard

Abrir el navegador y entrar a:

```txt
http://localhost:3000
```

O desde otro dispositivo conectado a la misma red:

```txt
http://IP_DEL_PROXY:3000
```

Ejemplo:

```txt
http://192.168.1.15:3000
```

El dashboard debe permanecer ejecutándose mientras el proxy esté activo.

---

# Configuración del Proxy en Windows

## 1. Obtener la dirección IP del equipo

Abrir una terminal de Windows.

Presionar la lupa de búsqueda y escribir:

```txt
cmd
```

Luego ejecutar:

```bash
ipconfig
```

Buscar la línea:

```txt
Dirección IPv4
```

Ejemplo:

```txt
Dirección IPv4 . . . . . . . . . . . : 192.168.1.15
```

Esa será la dirección IP del equipo donde se está ejecutando el proxy.

---

## 2. Abrir la configuración de Proxy

Presionar la lupa de búsqueda de Windows y escribir:

```txt
Proxy
```

Seleccionar:

```txt
Configuración de proxy
```

---

## 3. Configurar el servidor Proxy

Activar la opción:

```txt
Usar servidor proxy
```

Completar los campos:

| Campo     | Valor                    |
| --------- | ------------------------ |
| Dirección | IP obtenida con ipconfig |
| Puerto    | 8080                     |

Ejemplo:

| Campo     | Valor        |
| --------- | ------------ |
| Dirección | 192.168.1.15 |
| Puerto    | 8080         |

Finalmente presionar:

```txt
Guardar
```

---

# Instalación de la Extensión del Navegador

La extensión utilizada por el proyecto se encuentra dentro de la carpeta:

```txt
proxy-block-extension
```

La extensión debe instalarse manualmente antes de realizar las pruebas.

---

# Instalación en Google Chrome y Opera GX

## 1. Abrir el administrador de extensiones

Abrir el navegador e ingresar:

```txt
chrome://extensions
```

En Opera GX funciona exactamente la misma dirección.

---

## 2. Activar modo desarrollador

En la esquina superior derecha activar:

```txt
Modo desarrollador
```

---

## 3. Cargar la extensión

Presionar:

```txt
Cargar descomprimida
```

Luego seleccionar la carpeta:

```txt
proxy-block-extension
```

Después de seleccionarla, la extensión aparecerá instalada en el navegador.

---

# Verificar Funcionamiento

Abrir el navegador e ingresar a:

```txt
http://example.com
```

Si el proxy funciona correctamente:

* La página cargará normalmente
* El proxy registrará la solicitud
* El dashboard mostrará la actividad en tiempo real

Ejemplo:

```txt
[PERMITIDO] GET http://example.com
```

---

# Probar Bloqueo de Dominios

Agregar un dominio bloqueado desde el servidor o mediante el archivo:

```txt
blocked.txt
```

Ejemplo:

```txt
facebook.com
youtube.com
tiktok.com
```

Intentar ingresar nuevamente al dominio desde el navegador.

El proxy responderá con una página de bloqueo y registrará el evento.

Ejemplo:

```txt
[BLOQUEADO] youtube.com
```

---

# Información Mostrada en el Dashboard

El panel web muestra:

* Total de solicitudes realizadas
* Solicitudes bloqueadas y permitidas
* Top 5 de dominios más visitados
* Clientes activos
* Volumen de datos transferidos
* Logs del sistema

---

# Desactivar el Proxy

Cuando se terminen las pruebas:

1. Volver a:

```txt
Configuración de proxy
```

2. Desactivar:

```txt
Usar servidor proxy
```

3. Guardar los cambios.

Esto restaurará la conexión normal a Internet.

---

# Notas

* Para tráfico HTTP el proxy puede inspeccionar URLs completas.
* Para HTTPS únicamente se filtra el dominio mediante SNI.
* Algunas páginas modernas utilizan HTTPS exclusivamente.
* Se recomienda probar primero con páginas HTTP simples.
>>>>>>> panel-web
