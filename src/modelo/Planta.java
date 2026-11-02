package modelo;

public class Planta extends Entidad implements Reproducible, Mortal {
    private int tamanio;

    public Planta(String nombre) {
        this(nombre, 30.0 + Math.random() * 70.0);
    }

    public Planta(String nombre, double energia) {
        super(nombre, energia, 0);
        establecerTamanio((int) (Math.random() * 5) + 1);
    }

    @Override
    protected void actuar(Ecosistema eco) {
        intentarReproduccion(eco);
    }

    @Override
    public boolean puedeReproducirse() {
        return obtenerViva() && obtenerEnergia() > 30;
    }

    @Override
    public void reproducirse(Ecosistema eco) {
        if (!puedeReproducirse()) {
            return;
        }

        double probabilidad = calcularProbabilidadReproduccion(eco.obtenerClimaActual());
        if (Math.random() >= probabilidad) {
            return;
        }

        Planta nueva = crearDescendiente(eco.generarNombre("Planta"));
        eco.registrarNacimiento(nueva);
        establecerEnergia(obtenerEnergia() - 20);
    }

    @Override
    public String describirEstado() {
        return obtenerNombre() + " | energia=" + (int) obtenerEnergia()
                + " | edad=" + obtenerEdad() + " | tamanio=" + tamanio;
    }

    public int serComida() {
        if (!obtenerViva()) {
            return 0;
        }

        int valorNutritivo = tamanio * 10;
        establecerEnergia(0);
        establecerViva(false);
        return valorNutritivo;
    }

    @Override
    public boolean estaVivo() {
        return obtenerViva();
    }

    @Override
    public void morir() {
        establecerEnergia(0);
        establecerViva(false);
    }

    public int obtenerTamanio() {
        return tamanio;
    }

    public void establecerTamanio(int tamanio) {
        if (tamanio < 1 || tamanio > 5) {
            throw new IllegalArgumentException("El tamanio debe estar entre 1 y 5.");
        }
        this.tamanio = tamanio;
    }

    public double calcularProbabilidadReproduccion(Clima clima) {
        if (clima == null) {
            throw new IllegalArgumentException("Debe configurar el clima antes de reproducir plantas.");
        }

        double probabilidadBase = 0.60;
        return switch (clima) {
            case soleado -> probabilidadBase * 1.5;
            case lluvioso -> probabilidadBase * 2.0;
            case sequia -> probabilidadBase * 0.5;
            case invierno -> 0.0;
        };
    }

    protected Planta crearDescendiente(String nombre) {
        return new Planta(nombre, 40);
    }
}
