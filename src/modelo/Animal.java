package modelo;

public abstract class Animal extends Entidad implements Mortal {
    private int velocidad;
    private double peso;

    public Animal(String nombre, double energia, int velocidad, double peso) {
        super(nombre, energia, 0);
        establecerVelocidad(velocidad);
        establecerPeso(peso);
    }

    protected abstract void comer(Ecosistema eco);

    @Override
    public boolean estaVivo() {
        return obtenerViva();
    }

    @Override
    public void morir() {
        establecerEnergia(0);
        establecerViva(false);
    }

    public int obtenerVelocidad() {
        return velocidad;
    }

    public void establecerVelocidad(int velocidad) {
        if (velocidad < 0) {
            throw new IllegalArgumentException("La velocidad no puede ser negativa.");
        }
        this.velocidad = velocidad;
    }

    public double obtenerPeso() {
        return peso;
    }

    public void establecerPeso(double peso) {
        if (!Double.isFinite(peso) || peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser un numero positivo.");
        }
        this.peso = peso;
    }
}
