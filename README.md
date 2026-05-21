# Configuración del Proxy en Windows

## Integrantes

- Makin Artavia Zúñiga
- Cipriano Rivera Escobar
- Reyner Rojas Gutiérrez

1. Obtener la dirección IP del equipo

Abrir una terminal de Windows (CMD).

Puede abrirse presionando la lupa de búsqueda de Windows y escribiendo:

cmd

Luego ejecutar el siguiente comando:

ipconfig

Buscar la línea:

Dirección IPv4

Ejemplo:

Dirección IPv4 . . . . . . . . . . . : 192.168.1.15

Esa será la dirección IP del equipo donde se está ejecutando el proxy.

2. Abrir la configuración de Proxy

Presionar la lupa de búsqueda de Windows y escribir:

Proxy

Seleccionar la opción:

Configuración de Proxy
3. Configurar el servidor proxy

En la ventana de configuración:

Activar la opción:

Usar servidor proxy

Completar los campos:

Campo	Valor
Dirección IP de proxy	IP obtenida con ipconfig
Puerto	8080

Ejemplo:

Campo	Valor
Dirección IP de proxy	192.168.1.15
Puerto	8080
<img width="568" height="490" alt="image" src="https://github.com/user-attachments/assets/9049e705-964c-4306-a31b-fa6e965045ea" />


Finalmente presionar:

Guardar
