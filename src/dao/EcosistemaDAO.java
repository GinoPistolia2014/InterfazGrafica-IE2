package dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import modelo.Ecosistema;
import java.util.ArrayList;
import modelo.Animal;
import modelo.Clima;
import modelo.Conejo;
import modelo.Entidad;
import modelo.Lobo;
import modelo.Planta;


/**
 * DAO responsable de la persistencia del estado del ecosistema.
 * Esta clase no utiliza Swing ni conoce la Vista o el Controlador.
 */
public class EcosistemaDAO {

    public static final String ARCHIVO = "ecosistema.txt";
    public static final String SEPARADOR = " / ";

    public boolean existeGuardado() {
        return new File(ARCHIVO).exists();
    }

    /**
     * Guarda el estado de una simulación.
     */
   public boolean guardar(Ecosistema ecosistema) {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ARCHIVO))) {

            // 1) Encabezado
            escritor.write("ESTADO" + SEPARADOR
                    + ecosistema.obtenerClimaActual().name() + SEPARADOR
                    + ecosistema.obtenerTurnoActual() + SEPARADOR
                    + ecosistema.obtenerTurnosTotales() + SEPARADOR
                    + ecosistema.obtenerSiguienteId() + SEPARADOR
                    + ecosistema.obtenerTotalLobosCreados() + SEPARADOR
                    + ecosistema.obtenerUltimoTurnoIntervenido());
            escritor.newLine();

            // 2) Actividad por turno (para el "turno de mayor actividad")
            StringBuilder actividad = new StringBuilder("ACTIVIDAD");
            for (int cantidad : ecosistema.obtenerActividadPorTurno()) {
                actividad.append(SEPARADOR).append(cantidad);
            }
            escritor.write(actividad.toString());
            escritor.newLine();

            // 3) Historial de poblaciones: una línea por turno (turno 0 = inicio)
            ArrayList<Integer> plantas = ecosistema.obtenerHistorialPlantas();
            ArrayList<Integer> conejos = ecosistema.obtenerHistorialConejos();
            ArrayList<Integer> lobos = ecosistema.obtenerHistorialLobos();
            for (int turno = 0; turno < plantas.size(); turno++) {
                escritor.write("HISTORIAL" + SEPARADOR
                        + plantas.get(turno) + SEPARADOR
                        + conejos.get(turno) + SEPARADOR
                        + lobos.get(turno));
                escritor.newLine();
            }

            // 4) Todas las entidades, vivas y muertas
            ArrayList<Entidad> nacidas = ecosistema.obtenerNacimientosRegistrados();
            for (Entidad e : ecosistema.obtenerTodasLasEntidades()) {
                escritor.write(pasarAArchivo(e, nacidas.contains(e)));
                escritor.newLine();
            }
            return true;

            } catch (IOException e) {
            // El DAO no muestra mensajes: avisa con false y el controlador decide.
            return false;
            }
    }

    public Ecosistema restaurar() {
        if (!existeGuardado()) {
            return null;
        }
        
        Ecosistema ecosistema = new Ecosistema();

        try (BufferedReader lector = new BufferedReader(new FileReader(ARCHIVO))) {

            // 1) Encabezado: ESTADO / clima / turno / turnosTotales / siguienteId / lobosCreados / ultimoTurnoIntervenido
            String encabezado = lector.readLine();
            if (encabezado == null) {
                return null;
            }
            String[] h = encabezado.split(SEPARADOR);
            if (h.length < 7 || !h[0].equals("ESTADO")) {
                throw new IllegalArgumentException("Archivo sin encabezado valido.");
            }
            ecosistema.iniciarRestauracion(
                    Clima.valueOf(h[1]),
                    Integer.parseInt(h[2]),
                    Integer.parseInt(h[3]),
                    Integer.parseInt(h[4]),
                    Integer.parseInt(h[5]),
                    Integer.parseInt(h[6]));

            // 2) Resto del archivo: línea ACTIVIDAD y una línea por entidad
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] p = linea.split(SEPARADOR);

                    if (p[0].equals("ACTIVIDAD")) {
                        ArrayList<Integer> actividad = new ArrayList<>();
                        for (int i = 1; i < p.length; i++) {
                            actividad.add(Integer.parseInt(p[i].trim()));
                        }
                        ecosistema.restaurarActividad(actividad);
                                        } else if (p[0].equals("HISTORIAL")) {
                        ecosistema.restaurarHistorial(
                                Integer.parseInt(p[1].trim()),
                                Integer.parseInt(p[2].trim()),
                                Integer.parseInt(p[3].trim()));
                    } else {
                        ecosistema.restaurarEntidad(
                                p[0],                           // especie
                                p[1],                           // nombre
                                Integer.parseInt(p[2]),         // edad
                                Double.parseDouble(p[3]),       // energía
                                Boolean.parseBoolean(p[4]),     // viva
                                Boolean.parseBoolean(p[5]),     // nacida durante la simulación
                                Integer.parseInt(p[6]));        // éxitos de caza (0 si no es lobo)
                    }
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                // Línea con formato inválido: se ignora y se sigue con las demás.
                }
            }

            ecosistema.finalizarRestauracion();
            return ecosistema;

        } catch (IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            // Archivo ilegible o con encabezado inválido: avisa con null.
            return null;
        }
    }
    
    private String pasarAArchivo(Entidad e, boolean nacida) {
        int cacerias = (e instanceof Lobo l) ? l.obtenerExitosCaza() : 0;
        return e.getClass().getSimpleName() + SEPARADOR
             + e.obtenerNombre() + SEPARADOR
             + e.obtenerEdad() + SEPARADOR
             + e.obtenerEnergia() + SEPARADOR
             + e.obtenerViva() + SEPARADOR
             + nacida + SEPARADOR
             + cacerias;
    }
    
    /**
     * Borra la simulación guardada. Devuelve false solo si el archivo existía
     * y no se pudo borrar.
     */
    public boolean borrarGuardado() {
        File archivo = new File(ARCHIVO);
        return !archivo.exists() || archivo.delete();
    }

}
