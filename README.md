# Simulador de Ecosistema — versión Swing (2° Instancia Evaluativa)

Aplicación de escritorio en Java que transforma el simulador de ecosistema de la IE1 (que corría en la terminal) en una interfaz gráfica **Swing**, organizada con arquitectura **MVC** y con guardado y retoma de la simulación mediante un **DAO con archivo de texto plano**.

La mecánica no cambia: la persona configura el ecosistema (plantas, conejos, lobos, clima y turnos), avanza turno a turno, interviene cada 3 turnos y al final ve un reporte completo.

## Integrantes y roles

| Integrante | GitHub | Aporte principal |
|---|---|---|
| Gino Pistolia | @GinoPistolia2014 | Estructura inicial del proyecto: arquitectura MVC en paquetes, DAO inicial y primera versión de la interfaz. |
| Milagros Tissera | @militissera11 | Intervención del jugador: panel en la pantalla de simulación, reglas en el modelo (cada 3 turnos, una vez por turno) y conexión vista–controlador–modelo. |
| Tomás Gauna | @TomiGauna | Refactorización, persistencia de la simulación (guardar y retomar desde el archivo) y reporte final. |
| Joaquín Espil | @joaquinespil-code | Avisos de error de guardado en la interfaz, Nueva simulación con confirmación, menú superior (JMenuBar), estadísticas de población guardadas en el archivo, planta venenosa en los eventos y README. |

## Requisitos y ejecución

- **NetBeans:** Apache NetBeans 27 (probado).
- **JDK:** 17 o superior (probado con JDK 21).
- **Librerías externas:** ninguna, solo Java estándar.

**Pasos:**
1. Clonar el repositorio: `https://github.com/GinoPistolia2014/InterfazGrafica-IE2.git`
2. En NetBeans: **File → Open Project** y elegir la carpeta `InterfazGrafica-IE2`.
3. **Run** (clase principal: `principal.Principal`).

El archivo de guardado `ecosistema.txt` se crea en la carpeta del proyecto (ruta relativa, sin rutas absolutas).

## Estructura del proyecto

```
src/
├── modelo/        Entidades y reglas de la simulación
├── vista/         Ventana Swing hecha con el diseñador de NetBeans
├── controlador/   Valida datos y coordina vista, modelo y DAO
├── dao/           Guarda y lee la simulación en ecosistema.txt
└── principal/     main: crea las piezas, las conecta y muestra la ventana
```

| Paquete | Responsabilidad | No hace |
|---|---|---|
| `modelo` | Clases de la IE1 (`Entidad`, `Animal`, `Planta`, `PlantaVenenosa`, `Conejo`, `Lobo`, interfaces `Reproducible`, `Mortal`, `Peligroso`, enum `Clima`) y `Ecosistema`, que procesa cada turno, aplica las reglas, guarda los eventos y calcula los datos del reporte. | No usa Swing, `Scanner` ni `System.out`. |
| `vista` | `EcosistemaView` (JFrame Form): pantallas de configuración, simulación y reporte con `CardLayout`, tabla de entidades vivas, lista de eventos, panel de intervención, menú superior y cuadros de mensaje. Cada evento llama al controlador en una sola línea. | No tiene lógica de negocio ni lee archivos ni el modelo. |
| `controlador` | `EcosistemaController`: valida formato, rangos y campos vacíos; coordina modelo y DAO; entrega a la vista los datos ya listos para mostrar; decide qué mensaje mostrar cuando algo falla. | No crea ni maneja componentes Swing. |
| `dao` | `EcosistemaDAO`: escribe y lee `ecosistema.txt`, maneja los errores de entrada/salida y avisa el resultado con `true` / `false` / `null`. | No usa Swing ni muestra mensajes. |
| `principal` | `Principal`: crea modelo, DAO, vista y controlador, los conecta y muestra la ventana. | — |

### Flujo al presionar «Avanzar turno»
1. **Vista:** el botón llama a `controlador.avanzarTurno()`.
2. **Controlador:** le pide al modelo que procese el turno.
3. **Modelo:** `procesarTurno()` devuelve la lista de eventos del turno.
4. **Controlador:** actualiza tabla, contadores y eventos, y le pide al DAO que guarde.
5. **DAO:** devuelve `true` o `false`; si falló, el controlador le pide a la vista que muestre el error.
6. **Controlador:** si hubo colapso o se cumplieron los turnos, muestra el reporte final.

### Formato de `ecosistema.txt`
```
ESTADO / clima / turnoActual / turnosTotales / siguienteId / lobosCreados / ultimoTurnoIntervenido
ACTIVIDAD / eventosTurno1 / eventosTurno2 / ...
HISTORIAL / plantas / conejos / lobos        (una línea por turno, desde el turno 0)
Especie / nombre / edad / energia / viva / nacidaEnSimulacion / cacerias   (una línea por entidad)
```
Si el archivo no existe se empieza una simulación nueva; si tiene datos inválidos se avisa en pantalla y la aplicación no se rompe.

## Funcionalidades

- **Configuración** desde un formulario, con validación en el controlador (5–30 plantas, 2–15 conejos, 1–5 lobos, 10–50 turnos).
- **Simulación turno a turno:** contadores (turno, clima, poblaciones), tabla de entidades vivas y lista de eventos del turno.
- **Intervención cada 3 turnos:** cambiar el clima o agregar una entidad (máximo 5 lobos en total); la interfaz indica cuándo se puede intervenir.
- **4 climas:** soleado, lluvioso, sequía e invierno, con los efectos de la IE1.
- **Guardado automático** después de cada turno y retoma al abrir la aplicación.
- **Nueva simulación** desde el botón o el menú, con confirmación si hay una en curso.
- **Reporte final:** causa de fin, turno de mayor actividad, entidad más longeva por tipo, lobo con más cacerías y nacimientos/muertes por tipo.

**Bonus implementados:**
- Menú superior (JMenuBar): *Simulación → Nueva simulación* y *Reportes → Ver reporte*, que llaman al controlador igual que los botones.
- Estadísticas de población: historial por turno y turno del máximo y del mínimo de cada tipo en el reporte, guardados también en el archivo.
- Planta venenosa: aparece diferenciada en la tabla (tipo `PlantaVenenosa`) y se registra en los eventos cuando un conejo la come.

## Diseño en Figma

Link: https://www.figma.com/design/XvJk9SqRHmrSOcVKPdrg3B/Sin-t%C3%ADtulo?node-id=0-1&t=xLZLfoKSbIZeP5QB-1


Ver también `documentacion/FIGMA_LINK.txt`.

## Video explicativo

Link: https://drive.google.com/drive/folders/1SifajfMp9xQyIkccp80j1AeevmD8Vyls

## Link Drive: https://drive.google.com/drive/folders/1uHSzjdGgZFdVKl3xajFWjBsrZWT93he2?usp=drive_link

## Desafíos encontrados

- **Separar las capas:** pasar del programa de consola a MVC obligó a sacar todos los `System.out` del modelo y hacer que `procesarTurno()` devuelva los eventos como datos.
- **Guardar y retomar sin perder información:** el archivo guarda también las entidades muertas, los nacimientos, la actividad por turno y el historial, para que el reporte final siga siendo correcto después de retomar.
- **Errores de entrada/salida:** el DAO no muestra mensajes; avisa con `true`, `false` o `null` y el controlador decide qué mostrar.
- **Interfaz con el diseñador:** todos los componentes se crearon con el diseñador visual de NetBeans; por código solo se cargan datos, se habilitan o deshabilitan componentes y se cambia de pantalla.
- **Trabajo en equipo con Git:** ramas por integrante y pull requests a `main`.

## Uso de IA y herramientas externas

Se utilizaron herramientas de inteligencia artificial como apoyo durante el desarrollo. Los links a las conversaciones de cada integrante están en `documentacion/IA_Y_FUENTES.md`.
