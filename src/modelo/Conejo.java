package modelo;

public class Conejo extends Animal implements Reproducible {

    public Conejo(String nombre, double energia, int velocidad, double peso) {
        super(nombre, energia, velocidad, peso);
    }

    @Override
    protected void actuar(Ecosistema eco) {
        comer(eco);
        intentarReproduccion(eco);
    }

    @Override
    protected void comer(Ecosistema eco) {
        if (!estaVivo() || obtenerEnergia() <= 0) {
            return;
        }

        for (Planta planta : eco.obtenerPlantas()) {
            if (planta.obtenerViva() && planta.obtenerEnergia() > 0) {
                int valorNutritivo = planta.serComida();
                establecerEnergia(obtenerEnergia() + valorNutritivo);

                eco.registrarEvento(obtenerNombre() + " comio a "
                        + planta.obtenerNombre() + " (aporte de energia: "
                        + valorNutritivo + ").");
                eco.registrarMuerte(planta, "fue consumida por " + obtenerNombre());
                return;
            }
        }

        establecerEnergia(obtenerEnergia() - 15);
        eco.registrarEvento(obtenerNombre()
                + " no encontro plantas disponibles y perdio 15 de energia.");
    }

    @Override
    public boolean puedeReproducirse() {
        return estaVivo() && obtenerEnergia() > 60;
    }

    @Override
    public void reproducirse(Ecosistema eco) {
        if (!puedeReproducirse()) {
            return;
        }

        boolean hayOtroConejoVivo = false;
        for (Conejo otro : eco.obtenerConejos()) {
            if (otro != this && otro.estaVivo() && otro.obtenerEnergia() > 0) {
                hayOtroConejoVivo = true;
                break;
            }
        }

        if (hayOtroConejoVivo && Math.random() < 0.30) {
            Conejo nuevoConejo = new Conejo(eco.generarNombre("Conejo"), 30, 5, 2.5);
            eco.registrarNacimiento(nuevoConejo);
            establecerEnergia(obtenerEnergia() - 30);
        }
    }

    @Override
    public String describirEstado() {
        return obtenerNombre() + " | energia=" + (int) obtenerEnergia()
                + " | edad=" + obtenerEdad() + " | velocidad=" + obtenerVelocidad();
    }
}
