<h1 align="center">
  PintaPicón
</h1>
Proyecto integrador final de la **Tecnicatura en Desarrollo de Software** Este README está orientado a explicar qué es la app y cómo está construída.
<br>


## Descripción


PintaPicón es una aplicación para organizar partidos de fútbol amateur: crear un partido, invitar jugadores, gestionar confirmaciones y pagos, armar equipos, buscar canchas, etc.
El objetivo es mostrar una solución simple y entendible al problema de coordinar un partido con amigos, donde surgen algunos problemas de asistencia, pagos no equitativos y reserva de canchas.
<br>

## Estado del proyecto 

  - **Simulación sin backend:** no hay servidor ni API. Toda la lógica viven en el cliente conectados a una base de datos SQL Server.
  - **Persistencia local / mocks** para representar flujos reales (invitaciones, confirmaciones, equipos).
  - Enfoque en UX y recorridos clave más que en infraestructura.

## Funcionalidades
  - Crear partidos con fecha, hora, lugar (google maps) y modo de pago.
  - Invitar jugadores y registrar respuestas (asiste/no asiste/pagó/no pagó)
  - Armar equipos y posiciones.
  - Marcar pagos o gastos compartidos de forma simple.
  - Ver un resumen del estado del partido.

## Tecnologías
  - Android nativo (Kotlin).
  - Base de datos SQL Server.
  - UI con vistas nativas

## Cómo ejecutar
  1. Clonar el repositorio: git clone https://github.com/Fecu3799/pintapicon.git
  2. Abrir el proyecto en Android Studio.
  3. Ejecutar en un emulador.

### Autor
  Facundo Reynoso © 2025 
