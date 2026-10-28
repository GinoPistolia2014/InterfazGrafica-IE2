package modelo;

public class PlantaVenenosa extends Planta implements Peligroso {

    public PlantaVenenosa(String nombre) {
        this(nombre, 30.0 + Math.random() * 70.0);
    }

    public PlantaVenenosa(String nombre, double energia) {
        super(nombre, energia);
    }

    @Override
    public int serComida() {
        if (!obtenerViva()) {
            return 0;
        }
        super.serComida();
        return -30;
    }

    @Override
    public int getNivelPeligro() {
        return 2;
    }

    @Override
    protected Planta crearDescendiente(String nombre) {
        return new PlantaVenenosa(nombre, 40);
    }
}
