package dao;

import java.io.File;
import modelo.Ecosistema;

/**
 * DAO responsable de la persistencia del estado del ecosistema.
 * Esta clase no utiliza Swing ni conoce la Vista o el Controlador.
 */
public class EcosistemaDAO {

    public static final String ARCHIVO = "ecosistema.txt";

    public boolean existeGuardado() {
        return new File(ARCHIVO).exists();
    }

    /**
     * Guarda el estado de una simulación.
     */
    public boolean guardar(Ecosistema ecosistema) {
        return false;
    }

    /**
     * Carga el estado almacenado de una simulación.
     */
    public Ecosistema cargar() {
        return null;
    }
}
