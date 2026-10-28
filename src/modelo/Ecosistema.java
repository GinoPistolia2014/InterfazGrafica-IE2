package modelo;

import java.util.ArrayList;

/**
 * Modelo principal de la simulación.
 *
 * Conserva la mecánica de la IE1 sin utilizar Scanner, System.out ni Swing. Los
 * eventos se representan como datos para que otras capas decidan cómo
 * mostrarlos.
 */
public class Ecosistema {

    private final ArrayList<Planta> plantas = new ArrayList<>();
    private final ArrayList<Conejo> conejos = new ArrayList<>();
    private final ArrayList<Lobo> lobos = new ArrayList<>();
    private final ArrayList<String> eventosTurno = new ArrayList<>();
    private final ArrayList<Entidad> muertesRegistradas = new ArrayList<>();
    private final ArrayList<Entidad> nacimientosRegistrados = new ArrayList<>();

    // Historial: posición 0 = estado inicial.
    private final ArrayList<Integer> historialPlantas = new ArrayList<>();
    private final ArrayList<Integer> historialConejos = new ArrayList<>();
    private final ArrayList<Integer> historialLobos = new ArrayList<>();
    private final ArrayList<ArrayList<String>> historialEventos = new ArrayList<>();

    private Clima climaActual;
    private int turnoActual;
    private int turnosTotales;
    private int siguienteId = 1;
    private int totalLobosCreados;
    private boolean configurado;
    private int ultimoTurnoIntervenido = -1;

    // -----------------------------------------------------------------
    // CONFIGURACIÓN INICIAL
    // -----------------------------------------------------------------
    public void configurarSimulacion(
            int cantidadPlantas,
            int cantidadConejos,
            int cantidadLobos,
            Clima clima,
            int turnos) {

        // El controlador también valida estos rangos. El Modelo los vuelve a
        // controlar porque son reglas del dominio y no deben poder romperse.
        validarRango(cantidadPlantas, 5, 30, "plantas");
        validarRango(cantidadConejos, 2, 15, "conejos");
        validarRango(cantidadLobos, 1, 5, "lobos");
        validarRango(turnos, 10, 50, "turnos");

        if (clima == null) {
            throw new IllegalArgumentException("Debe seleccionar un clima.");
        }

        reiniciarEstado();
        climaActual = clima;
        turnosTotales = turnos;

        // Regla de la IE1: una de cada cinco plantas iniciales es venenosa.
        for (int i = 1; i <= cantidadPlantas; i++) {
            agregarEntidad(i % 5 == 0 ? "planta venenosa" : "planta");
        }

        for (int i = 0; i < cantidadConejos; i++) {
            agregarEntidad("conejo");
        }

        for (int i = 0; i < cantidadLobos; i++) {
            agregarEntidad("lobo");
        }

        guardarPoblaciones();
        configurado = true;
    }

    private void validarRango(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(
                    "La cantidad de " + campo + " debe estar entre "
                    + minimo + " y " + maximo + ".");
        }
    }

    private void reiniciarEstado() {
        plantas.clear();
        conejos.clear();
        lobos.clear();
        eventosTurno.clear();
        muertesRegistradas.clear();
        nacimientosRegistrados.clear();
        historialPlantas.clear();
        historialConejos.clear();
        historialLobos.clear();
        historialEventos.clear();

        climaActual = null;
        turnoActual = 0;
        turnosTotales = 0;
        siguienteId = 1;
        totalLobosCreados = 0;
        configurado = false;
        ultimoTurnoIntervenido = -1;
    }

    // -----------------------------------------------------------------
    // PROCESAMIENTO DE TURNO
    // -----------------------------------------------------------------
    /**
     * Procesa un turno y devuelve los eventos generados. No muestra nada por
     * consola ni usa Swing.
     */
    public ArrayList<String> procesarTurno() {
        if (!configurado) {
            throw new IllegalStateException("Primero debe configurarse la simulacion.");
        }
        if (simulacionFinalizada()) {
            throw new IllegalStateException("La simulacion ya finalizo.");
        }

        // Si una intervención anterior dejó un evento pendiente, se conserva.
        turnoActual++;

        ArrayList<Reproducible> reproducibles = new ArrayList<>();
        for (Planta planta : plantas) {
            if (planta.estaVivo()) {
                reproducibles.add(planta);
            }
        }
        for (Conejo conejo : conejos) {
            if (conejo.estaVivo()) {
                reproducibles.add(conejo);
            }
        }

        // Plantas: reproducción. Conejos: comer y luego reproducción.
        for (Reproducible reproducible : reproducibles) {
            Mortal ser = (Mortal) reproducible;
            if (!ser.estaVivo()) {
                continue;
            }

            if (reproducible instanceof Conejo conejo) {
                conejo.comer(this);
            }
            reproducible.intentarReproduccion(this);
        }

        // Lobos: caza.
        for (Lobo lobo : new ArrayList<>(lobos)) {
            if (lobo.estaVivo()) {
                lobo.actuar(this);
            }
        }

        // Todas las entidades vivas envejecen y gastan energía base.
        for (Entidad entidad : obtenerEntidadesVivas()) {
            entidad.envejecer();
        }

        aplicarEfectosClima();
        verificarMuertes();
        guardarPoblaciones();

        ArrayList<String> resultado = new ArrayList<>(eventosTurno);
        historialEventos.add(new ArrayList<>(resultado));
        eventosTurno.clear();
        return resultado;
    }

    private void aplicarEfectosClima() {
        int efectoConejos = 0;
        int efectoLobos = 0;

        switch (climaActual) {
            case soleado ->
                efectoConejos = 5;
            case lluvioso -> {
                efectoConejos = 3;
                efectoLobos = -5;
            }
            case sequia ->
                efectoConejos = -5;
            case invierno ->
                efectoConejos = -8;
        }

        for (Conejo conejo : conejos) {
            if (conejo.estaVivo()) {
                conejo.establecerEnergia(conejo.obtenerEnergia() + efectoConejos);
            }
        }
        for (Lobo lobo : lobos) {
            if (lobo.estaVivo()) {
                lobo.establecerEnergia(lobo.obtenerEnergia() + efectoLobos);
            }
        }

        String efecto = "Efecto del clima " + climaActual
                + ": conejos " + String.format("%+d", efectoConejos) + " de energia";
        if (efectoLobos != 0) {
            efecto += ", lobos " + String.format("%+d", efectoLobos) + " de energia";
        }
        registrarEvento(efecto + ".");
    }

    private void verificarMuertes() {
        for (Planta planta : plantas) {
            if (planta.verificarMuerte()) {
                registrarMuerte(planta, "se seco por falta de energia");
            }
        }
        for (Conejo conejo : conejos) {
            if (conejo.verificarMuerte()) {
                registrarMuerte(conejo, "de inanicion (sin energia)");
            }
        }
        for (Lobo lobo : lobos) {
            if (lobo.verificarMuerte()) {
                registrarMuerte(lobo, "de inanicion (sin energia)");
            }
        }
    }

    private void guardarPoblaciones() {
        historialPlantas.add(contarPlantasVivas());
        historialConejos.add(contarConejosVivos());
        historialLobos.add(contarLobosVivos());
    }

    // -----------------------------------------------------------------
    // ENTIDADES / INTERVENCIÓN
    // -----------------------------------------------------------------
    public Entidad agregarEntidad(String tipo) {
        return agregarEntidad(tipo, 30.0 + Math.random() * 70.0);
    }

    public Entidad agregarEntidad(String tipo, double energiaInicial) {
        if (tipo == null) {
            throw new IllegalArgumentException("Debe indicar el tipo de entidad.");
        }
        if (!Double.isFinite(energiaInicial) || energiaInicial <= 0) {
            throw new IllegalArgumentException(
                    "La entidad debe ingresar con energia positiva y finita.");
        }

        Entidad nueva;
        String tipoNormalizado = tipo.trim().toLowerCase();

        switch (tipoNormalizado) {
            case "planta" -> {
                Planta planta = new Planta(generarNombre("Planta"), energiaInicial);
                plantas.add(planta);
                nueva = planta;
            }
            case "planta venenosa" -> {
                Planta planta = new PlantaVenenosa(generarNombre("Planta"), energiaInicial);
                plantas.add(planta);
                nueva = planta;
            }
            case "conejo" -> {
                Conejo conejo = new Conejo(generarNombre("Conejo"), energiaInicial, 5, 2.5);
                conejos.add(conejo);
                nueva = conejo;
            }
            case "lobo" -> {
                if (totalLobosCreados >= 5) {
                    throw new IllegalStateException(
                            "Ya se incorporaron los cinco lobos permitidos.");
                }
                Lobo lobo = new Lobo(generarNombre("Lobo"), energiaInicial, 8, 35.0, 0);
                lobos.add(lobo);
                totalLobosCreados++;
                nueva = lobo;
            }
            default ->
                throw new IllegalArgumentException(
                        "Tipo de entidad desconocido: " + tipo);
        }

        return nueva;
    }

    public void cambiarClima(Clima nuevo) {
        if (nuevo == null) {
            throw new IllegalArgumentException("El clima no puede ser nulo.");
        }
        climaActual = nuevo;
    }

    public void intervenirCambiarClima(Clima nuevo) {
        if (!puedeIntervenir()) {
            throw new IllegalStateException(
                    "Solo se puede intervenir cada 3 turnos, una vez por turno.");
        }
        if (nuevo == null) {
            throw new IllegalArgumentException("Debe seleccionar un clima.");
        }
        if (nuevo == climaActual) {
            throw new IllegalArgumentException("El clima ya es " + nuevo + ".");
        }
        cambiarClima(nuevo);
        ultimoTurnoIntervenido = turnoActual;
        registrarEvento("Intervencion del jugador: el clima cambio a " + nuevo + ".");
    }

    public Entidad intervenirAgregarEntidad(String tipo) {
        if (!puedeIntervenir()) {
            throw new IllegalStateException(
                    "Solo se puede intervenir cada 3 turnos, una vez por turno.");
        }
        Entidad nueva = agregarEntidad(tipo); // si ya hay 5 lobos, lanza el error
        ultimoTurnoIntervenido = turnoActual;
        registrarEvento("Intervencion del jugador: se agrego " + nueva.obtenerNombre() + ".");
        return nueva;
    }

    public String generarNombre(String prefijo) {
        String nombre = prefijo + "-" + siguienteId;
        siguienteId++;
        return nombre;
    }

    // -----------------------------------------------------------------
    // ESTADO CONSULTABLE POR EL CONTROLADOR
    // -----------------------------------------------------------------
    public ArrayList<Entidad> obtenerEntidadesVivas() {
        ArrayList<Entidad> vivas = new ArrayList<>();
        for (Planta planta : plantas) {
            if (planta.estaVivo()) {
                vivas.add(planta);
            }
        }
        for (Conejo conejo : conejos) {
            if (conejo.estaVivo()) {
                vivas.add(conejo);
            }
        }
        for (Lobo lobo : lobos) {
            if (lobo.estaVivo()) {
                vivas.add(lobo);
            }
        }
        return vivas;
    }

    public int contarPlantasVivas() {
        int cantidad = 0;
        for (Planta planta : plantas) {
            if (planta.estaVivo()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public int contarConejosVivos() {
        int cantidad = 0;
        for (Conejo conejo : conejos) {
            if (conejo.estaVivo()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public int contarLobosVivos() {
        int cantidad = 0;
        for (Lobo lobo : lobos) {
            if (lobo.estaVivo()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public boolean ecosistemaColapsado() {
        return configurado && (contarPlantasVivas() == 0
                || contarConejosVivos() == 0
                || contarLobosVivos() == 0);
    }

    public boolean simulacionFinalizada() {
        return configurado && (ecosistemaColapsado() || turnoActual >= turnosTotales);
    }

    public boolean puedeIntervenir() {
        return configurado && turnoActual > 0 && turnoActual % 3 == 0
                && turnoActual < turnosTotales && !ecosistemaColapsado()
                && ultimoTurnoIntervenido != turnoActual;
    }

    public String obtenerCausaFin() {
        if (!simulacionFinalizada()) {
            return "Simulacion en curso";
        }
        if (!ecosistemaColapsado()) {
            return "Se completaron los " + turnosTotales + " turnos configurados.";
        }

        ArrayList<String> extintas = new ArrayList<>();
        if (contarPlantasVivas() == 0) {
            extintas.add("plantas");
        }
        if (contarConejosVivos() == 0) {
            extintas.add("conejos");
        }
        if (contarLobosVivos() == 0) {
            extintas.add("lobos");
        }
        return "Colapso del ecosistema. Poblacion extinguida: "
                + String.join(", ", extintas) + ".";
    }

    public Clima obtenerClimaActual() {
        return climaActual;
    }

    public int obtenerTurnoActual() {
        return turnoActual;
    }

    public int obtenerTurnosTotales() {
        return turnosTotales;
    }

    public int obtenerTotalLobosCreados() {
        return totalLobosCreados;
    }

    public boolean estaConfigurado() {
        return configurado;
    }

    public ArrayList<Planta> obtenerPlantas() {
        return new ArrayList<>(plantas);
    }

    public ArrayList<Conejo> obtenerConejos() {
        return new ArrayList<>(conejos);
    }

    public ArrayList<Lobo> obtenerLobos() {
        return new ArrayList<>(lobos);
    }

    public ArrayList<Integer> obtenerHistorialPlantas() {
        return new ArrayList<>(historialPlantas);
    }

    public ArrayList<Integer> obtenerHistorialConejos() {
        return new ArrayList<>(historialConejos);
    }

    public ArrayList<Integer> obtenerHistorialLobos() {
        return new ArrayList<>(historialLobos);
    }

    public ArrayList<ArrayList<String>> obtenerHistorialEventos() {
        ArrayList<ArrayList<String>> copia = new ArrayList<>();
        for (ArrayList<String> turno : historialEventos) {
            copia.add(new ArrayList<>(turno));
        }
        return copia;
    }

    // -----------------------------------------------------------------
    // EVENTOS / NACIMIENTOS / MUERTES
    // -----------------------------------------------------------------
    public void registrarEvento(String evento) {
        if (evento == null || evento.trim().isEmpty()) {
            throw new IllegalArgumentException("El evento no puede estar vacio.");
        }
        eventosTurno.add(evento.trim());
    }

    public ArrayList<String> obtenerEventosPendientes() {
        return new ArrayList<>(eventosTurno);
    }

    public void registrarMuerte(Entidad entidad, String causa) {
        if (entidad == null || entidad.obtenerViva()) {
            throw new IllegalArgumentException("Solo se puede registrar una entidad muerta.");
        }
        if (causa == null || causa.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar la causa de muerte.");
        }
        if (muertesRegistradas.contains(entidad)) {
            return;
        }

        muertesRegistradas.add(entidad);
        registrarEvento(entidad.obtenerNombre() + " murio: " + causa.trim() + ".");
    }

    public void registrarNacimiento(Entidad nueva) {
        if (nueva == null || !nueva.obtenerViva() || nueva.obtenerEnergia() <= 0) {
            throw new IllegalArgumentException("La cria debe estar viva y tener energia.");
        }
        if (nacimientosRegistrados.contains(nueva)) {
            return;
        }
        if (plantas.contains(nueva) || conejos.contains(nueva) || lobos.contains(nueva)) {
            throw new IllegalArgumentException("La entidad ya pertenece al ecosistema.");
        }

        if (nueva instanceof Planta planta) {
            plantas.add(planta);
        } else if (nueva instanceof Conejo conejo) {
            conejos.add(conejo);
        } else {
            throw new IllegalArgumentException("Solo pueden nacer plantas y conejos.");
        }

        nacimientosRegistrados.add(nueva);
        registrarEvento("Nacio " + nueva.obtenerNombre() + ".");
    }

    public ArrayList<Entidad> obtenerMuertesRegistradas() {
        return new ArrayList<>(muertesRegistradas);
    }

    public ArrayList<Entidad> obtenerNacimientosRegistrados() {
        return new ArrayList<>(nacimientosRegistrados);
    }

}
