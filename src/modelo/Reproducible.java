package modelo;

public interface Reproducible {
    default void intentarReproduccion(Ecosistema eco) {
        if (puedeReproducirse()) {
            reproducirse(eco);
        }
    }

    void reproducirse(Ecosistema eco);
    boolean puedeReproducirse();
}
