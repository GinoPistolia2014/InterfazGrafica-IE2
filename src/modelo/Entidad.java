package modelo;

/**
 * Clase base de todas las entidades del ecosistema.
 *
 * Parte heredada de la IE1. En la IE2 el Modelo no imprime por consola ni
 * conoce Swing: solo mantiene estado y reglas de negocio.
 */
public abstract class Entidad {
    private String nombre;
    private double energia;
    private int edad;
    private boolean viva;

    public Entidad(String nombre, double energia, int edad) {
        establecerNombre(nombre);
        establecerEnergia(energia);
        establecerEdad(edad);
        this.viva = obtenerEnergia() > 0;
    }

    protected abstract void actuar(Ecosistema eco);

    /**
     * Devuelve una descripción textual del estado. Otra capa decide cómo
     * mostrarla.
     */
    public abstract String describirEstado();

    public String obtenerNombre() {
        return nombre;
    }

    public double obtenerEnergia() {
        return energia;
    }

    public int obtenerEdad() {
        return edad;
    }

    public boolean obtenerViva() {
        return viva;
    }

    protected void establecerNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio.");
        }
        this.nombre = nombre.trim();
    }

    protected void establecerEnergia(double energia) {
        if (!Double.isFinite(energia)) {
            throw new IllegalArgumentException("La energia debe ser un numero finito.");
        }
        this.energia = Math.max(0.0, energia);
    }

    protected void establecerEdad(int edad) {
        this.edad = Math.max(0, edad);
    }

    protected void establecerViva(boolean viva) {
        this.viva = viva;
    }

    protected void envejecer() {
        edad++;
        establecerEnergia(energia - 5);
    }
}
