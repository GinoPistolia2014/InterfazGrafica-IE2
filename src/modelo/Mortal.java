package modelo;

public interface Mortal {
    double obtenerEnergia();
    String obtenerNombre();
    boolean estaVivo();
    void morir();

    default boolean verificarMuerte() {
        if (obtenerEnergia() <= 0 && estaVivo()) {
            morir();
            return true;
        }
        return false;
    }
}
