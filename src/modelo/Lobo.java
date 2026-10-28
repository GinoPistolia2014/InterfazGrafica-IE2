package modelo;

import java.util.ArrayList;

public class Lobo extends Animal implements Mortal, Peligroso {
    private int exitosCaza;

    public Lobo(String nombre, double energia, int velocidad, double peso, int cazas) {
        super(nombre, energia, velocidad, peso);
        establecerExitosCaza(cazas);
    }

    @Override
    protected void actuar(Ecosistema eco) {
        comer(eco);
    }

    @Override
    protected void comer(Ecosistema eco) {
        if (!estaVivo() || obtenerEnergia() <= 0) {
            return;
        }

        ArrayList<Conejo> presasDisponibles = new ArrayList<>();
        for (Conejo conejo : eco.obtenerConejos()) {
            if (conejo.estaVivo() && conejo.obtenerEnergia() > 0) {
                presasDisponibles.add(conejo);
            }
        }

        if (presasDisponibles.isEmpty()) {
            eco.registrarEvento(obtenerNombre() + " no encontro conejos para cazar.");
            return;
        }

        Conejo presa = presasDisponibles.get((int) (Math.random() * presasDisponibles.size()));
        double probabilidad = calcularProbabilidadCaza(eco.obtenerClimaActual());

        if (Math.random() < probabilidad) {
            presa.morir();
            establecerEnergia(obtenerEnergia() + 30);
            establecerExitosCaza(exitosCaza + 1);
            eco.registrarEvento(obtenerNombre() + " cazo a " + presa.obtenerNombre()
                    + " y gano 30 de energia.");
            eco.registrarMuerte(presa, "fue cazado por " + obtenerNombre());
        } else {
            eco.registrarEvento(obtenerNombre() + " fallo al intentar cazar a "
                    + presa.obtenerNombre() + ".");
        }
    }

    @Override
    public String describirEstado() {
        return obtenerNombre() + " | energia=" + (int) obtenerEnergia()
                + " | edad=" + obtenerEdad() + " | cazas=" + exitosCaza;
    }

    public int obtenerExitosCaza() {
        return exitosCaza;
    }

    public void establecerExitosCaza(int exitosCaza) {
        if (exitosCaza < 0) {
            throw new IllegalArgumentException("Las cacerias exitosas no pueden ser negativas.");
        }
        this.exitosCaza = exitosCaza;
    }

    public double calcularProbabilidadCaza(Clima clima) {
        if (clima == null) {
            throw new IllegalArgumentException("Debe configurar el clima antes de calcular la caza.");
        }

        double energia = obtenerEnergia();
        double probabilidad = energia / (energia + 50.0);
        if (clima == Clima.invierno) {
            probabilidad *= 1.20;
        }
        return Math.min(1.0, probabilidad);
    }

    @Override
    public int getNivelPeligro() {
        return 3 + exitosCaza;
    }
}
