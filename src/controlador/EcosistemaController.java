package controlador;

import dao.EcosistemaDAO;
import java.util.ArrayList;
import modelo.Clima;
import modelo.Ecosistema;
import modelo.Entidad;
import modelo.Planta;
import modelo.Conejo;
import modelo.Lobo;
import vista.EcosistemaView;

/**
 * Controlador MVC.
 *
 * No contiene componentes Swing ni listeners. Recibe acciones de la Vista,
 * coordina el Modelo y entrega a la Vista datos simples para mostrar.
 */
public class EcosistemaController {

    private Ecosistema modelo;
    private final EcosistemaView vista;
    private final EcosistemaDAO dao;
    private ArrayList<String> ultimosEventos = new ArrayList<>();

    public EcosistemaController(
            Ecosistema modelo,
            EcosistemaView vista,
            EcosistemaDAO dao) {
        this.modelo = modelo;
        this.vista = vista;
        this.dao = dao;
    }

    /**
     * Inicia la aplicación mostrando la pantalla de configuración.
     */
    public void iniciar() {
        if (dao.existeGuardado() && vista.preguntarSiRestaurar()) {
            cargarSimulacion();
        } else {
            vista.mostrarPantallaConfiguracion();
        }
    }

    /**
     * Recibe todos los valores como texto desde la Vista, valida formato y
     * rangos, y ordena al Modelo crear el ecosistema inicial.
     */
    public void iniciarSimulacion(
            String plantasTexto,
            String conejosTexto,
            String lobosTexto,
            String climaTexto,
            String turnosTexto) {

        if (estaVacio(plantasTexto)
                || estaVacio(conejosTexto)
                || estaVacio(lobosTexto)
                || estaVacio(climaTexto)
                || estaVacio(turnosTexto)) {
            vista.mostrarError("Todos los campos son obligatorios.");
            return;
        }

        Integer plantas = convertirEntero(plantasTexto, "plantas");
        if (plantas == null) {
            return;
        }

        Integer conejos = convertirEntero(conejosTexto, "conejos");
        if (conejos == null) {
            return;
        }

        Integer lobos = convertirEntero(lobosTexto, "lobos");
        if (lobos == null) {
            return;
        }

        Integer turnos = convertirEntero(turnosTexto, "turnos");
        if (turnos == null) {
            return;
        }

        if (!validarRango(plantas, 5, 30, "plantas")) {
            return;
        }
        if (!validarRango(conejos, 2, 15, "conejos")) {
            return;
        }
        if (!validarRango(lobos, 1, 5, "lobos")) {
            return;
        }
        if (!validarRango(turnos, 10, 50, "turnos")) {
            return;
        }

        Clima clima;
        try {
            clima = Clima.valueOf(
                    climaTexto.trim().toLowerCase().replace("í", "i"));
        } catch (IllegalArgumentException e) {
            vista.mostrarError("Seleccione un clima válido.");
            return;
        }

        try {
            modelo.configurarSimulacion(plantas, conejos, lobos, clima, turnos);
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return;
        }

        refrescarPantallaSimulacion(new ArrayList<>());
        vista.actualizarEstadoSimulacion("Ecosistema listo para comenzar.");
        vista.habilitarAvanceTurno(true);
        vista.habilitarIntervencion(false);
        ultimosEventos = new ArrayList<>();
        vista.mostrarPantallaSimulacion();
    }

    /**
     * Procesa exactamente un turno de la simulación.
     */
    public void avanzarTurno() {
        if (!modelo.estaConfigurado()) {
            vista.mostrarError("Primero debe configurar la simulación.");
            return;
        }

        if (modelo.simulacionFinalizada()) {
            finalizarSimulacionEnVista();
            return;
        }

        try {
            ArrayList<String> eventos = modelo.procesarTurno();
            ultimosEventos = eventos;
            refrescarPantallaSimulacion(eventos);
        } catch (IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return;
        }

        if (modelo.simulacionFinalizada()) {
            finalizarSimulacionEnVista();
        } else {
            actualizarEstadoIntervencion();
        }
    }

    public void cambiarClima(String climaTexto) {
        if (estaVacio(climaTexto)) {
            vista.mostrarError("Seleccione un clima.");
            return;
        }
        Clima nuevo;
        try {
            nuevo = Clima.valueOf(climaTexto.trim().toLowerCase().replace("í", "i"));
        } catch (IllegalArgumentException e) {
            vista.mostrarError("Seleccione un clima válido.");
            return;
        }
        try {
            modelo.intervenirCambiarClima(nuevo);
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return;
        }
        refrescarDespuesDeIntervenir();
    }

    public void agregarEntidad(String tipoTexto) {
        if (estaVacio(tipoTexto)) {
            vista.mostrarError("Seleccione el tipo de entidad a agregar.");
            return;
        }
        try {
            modelo.intervenirAgregarEntidad(tipoTexto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return;
        }
        refrescarDespuesDeIntervenir();
    }

    private void refrescarDespuesDeIntervenir() {
        ArrayList<String> eventosAMostrar = new ArrayList<>(ultimosEventos);
        eventosAMostrar.addAll(modelo.obtenerEventosPendientes());
        refrescarPantallaSimulacion(eventosAMostrar);
        actualizarEstadoIntervencion();
    }

    private void actualizarEstadoIntervencion() {
        boolean puede = modelo.puedeIntervenir();
        vista.habilitarIntervencion(puede);
        if (puede) {
            vista.actualizarEstadoSimulacion("Turno " + modelo.obtenerTurnoActual()
                    + ": podés intervenir (cambiar clima o agregar una entidad) o avanzar.");
        } else if (modelo.obtenerTurnoActual() > 0
                && modelo.obtenerTurnoActual() % 3 == 0) {
            vista.actualizarEstadoSimulacion(
                    "Intervención realizada. Avanzá al siguiente turno.");
        } else {
            vista.actualizarEstadoSimulacion("Simulación en curso. Próxima intervención en el turno "
                    + ((modelo.obtenerTurnoActual() / 3 + 1) * 3) + ".");
        }
    }

    private void refrescarPantallaSimulacion(ArrayList<String> eventos) {
        vista.actualizarResumenSimulacion(
                modelo.obtenerTurnoActual(),
                modelo.obtenerTurnosTotales(),
                modelo.obtenerClimaActual().name(),
                modelo.contarPlantasVivas(),
                modelo.contarConejosVivos(),
                modelo.contarLobosVivos());

        ArrayList<String[]> filas = new ArrayList<>();
        for (Entidad entidad : modelo.obtenerEntidadesVivas()) {
            filas.add(new String[]{
                entidad.obtenerNombre(),
                entidad.getClass().getSimpleName(),
                String.valueOf(entidad.obtenerEdad()),
                String.format("%.1f", entidad.obtenerEnergia())
            });
        }

        vista.actualizarTablaEntidades(filas);
        vista.actualizarEventos(eventos);
        dao.guardar(modelo);
    }

    private void finalizarSimulacionEnVista() {
        dao.borrarGuardado();
        vista.habilitarAvanceTurno(false);
        vista.habilitarIntervencion(false);
        vista.actualizarEstadoSimulacion(modelo.obtenerCausaFin());
        vista.mostrarReporte(armarReporte());
        vista.mostrarPantallaReporte();
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private Integer convertirEntero(String texto, String campo) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            vista.mostrarError("La cantidad de " + campo + " debe ser un número entero.");
            return null;
        }
    }

    private boolean validarRango(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            vista.mostrarError(
                    "La cantidad de " + campo + " debe estar entre "
                    + minimo + " y " + maximo + ".");
            return false;
        }
        return true;
    }
    
    public void cargarSimulacion() {
        Ecosistema cargado = dao.restaurar();
        if (cargado == null) {
            vista.mostrarError("No se pudo leer la simulación guardada.");
            vista.mostrarPantallaConfiguracion();
            return;
        }
        modelo = cargado;

        ultimosEventos = new ArrayList<>();
        ultimosEventos.add("Simulación restaurada en el turno "
                + modelo.obtenerTurnoActual() + ".");
        refrescarPantallaSimulacion(ultimosEventos);

        vista.habilitarAvanceTurno(true);
        actualizarEstadoIntervencion();
        vista.mostrarPantallaSimulacion();
    }
    
    private String armarReporte() {
        StringBuilder sb = new StringBuilder();

        sb.append("CAUSA DE FIN\n  ").append(modelo.obtenerCausaFin()).append("\n\n");

        sb.append("TURNO DE MAYOR ACTIVIDAD\n");
        int turno = modelo.obtenerTurnoMayorActividad();
        if (turno == 0) {
            sb.append("  No se procesaron turnos.\n");
        } else {
            sb.append("  Turno ").append(turno).append(" (")
              .append(modelo.obtenerEventosDelTurno(turno)).append(" eventos)\n");
        }

        sb.append("\nENTIDAD MAS LONGEVA POR TIPO\n");
        sb.append(lineaLongeva("Planta", modelo.obtenerMasLongevo(Planta.class)));
        sb.append(lineaLongeva("Conejo", modelo.obtenerMasLongevo(Conejo.class)));
        sb.append(lineaLongeva("Lobo", modelo.obtenerMasLongevo(Lobo.class)));

        sb.append("\nLOBO CON MAS CACERIAS EXITOSAS\n");
        Lobo cazador = modelo.obtenerLoboMasCazador();
        if (cazador == null) {
            sb.append("  Sin datos.\n");
        } else {
            sb.append("  ").append(cazador.obtenerNombre()).append(": ")
              .append(cazador.obtenerExitosCaza()).append(" cacerias\n");
        }

        sb.append("\nNACIMIENTOS Y MUERTES POR TIPO\n");
        sb.append(String.format("  %-10s %12s %10s%n", "Tipo", "Nacimientos", "Muertes"));
        sb.append(filaTotales("Planta", Planta.class));
        sb.append(filaTotales("Conejo", Conejo.class));
        sb.append(filaTotales("Lobo", Lobo.class));

        return sb.toString();
    }

    private String lineaLongeva(String tipo, Entidad e) {
        if (e == null) {
            return String.format("  %-8s Sin datos%n", tipo + ":");
        }
        return String.format("  %-8s %s (%d turnos)%n", tipo + ":", e.obtenerNombre(), e.obtenerEdad());
    }

    private String filaTotales(String nombre, Class<? extends Entidad> tipo) {
        return String.format("  %-10s %12d %10d%n", nombre,
                modelo.contarNacimientos(tipo), modelo.contarMuertes(tipo));
    }
}
