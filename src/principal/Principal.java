package principal;

import controlador.EcosistemaController;
import dao.EcosistemaDAO;
import modelo.Ecosistema;
import vista.EcosistemaView;

/**
 * Único punto de entrada de la aplicación.
 * Crea las piezas, las conecta y muestra la ventana.
 */
public class Principal {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            Ecosistema modelo = new Ecosistema();
            EcosistemaDAO dao = new EcosistemaDAO();
            EcosistemaView vista = new EcosistemaView();

            EcosistemaController controlador =
                    new EcosistemaController(modelo, vista, dao);

            vista.setControlador(controlador);
            controlador.iniciar();

            vista.setLocationRelativeTo(null);
            vista.setVisible(true);
        });
    }
}
