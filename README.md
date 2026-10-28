# InterfazGrafica-IE2

Simulador de Ecosistema desarrollado en Java con Swing y arquitectura MVC, preparado como proyecto **Java with Ant** para NetBeans.

## Requisitos

- JDK 17 o compatible.
- NetBeans con soporte para proyectos Java with Ant.

## Ejecución

1. Abrir NetBeans.
2. Elegir **File > Open Project**.
3. Seleccionar la carpeta `InterfazGrafica-IE2`.
4. Ejecutar el proyecto.

La clase principal configurada es:

```text
principal.Principal
```

## Estructura

```text
src/
├── modelo/
├── vista/
├── controlador/
├── dao/
└── principal/
```

### modelo
Contiene las entidades y reglas del ecosistema: `Entidad`, `Animal`, `Conejo`, `Lobo`, `Planta`, `PlantaVenenosa`, `Clima`, interfaces y `Ecosistema`.

### vista
Contiene la interfaz Swing creada como formulario de NetBeans y la navegación entre pantallas mediante `CardLayout`.

### controlador
Valida los datos ingresados por la persona y coordina la comunicación entre Vista, Modelo y DAO.

### dao
Contiene la responsabilidad de persistencia del estado de la simulación en archivos de texto.

### principal
Contiene el `main` que crea y conecta las capas de la aplicación.

## Reglas de arquitectura

- El Modelo no utiliza `Scanner`, `System.out` ni Swing.
- El DAO no utiliza Swing ni conoce la Vista.
- El Controlador no manipula componentes Swing ni registra listeners.
- La Vista contiene los componentes gráficos y delega las acciones al Controlador.

## Documentación

La carpeta `documentacion` contiene los archivos destinados al enlace de Figma y al registro de IA y fuentes utilizadas.
