package Presentacion;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * NUEVA (CU Iniciar partida). Cambia la pantalla que se ve dentro de la
 * ventana del peer. La usa el ControladorJuego cuando la partida pasa de
 * ESPERANDO_JUGADORES a EN_CURSO: cada peer cambia del Lobby a la partida
 * al recibir la ConfirmacionAccion del Host.
 *
 * Los otros CU pueden agregar aquí sus propias pantallas (resultados, etc.).
 *
 * @author Jesús
 */
public class NavegadorPantallas {

    private final VentanaParchis ventana;
    private final PantallaLobby pantallaLobby;
    private final PantallaPartida pantallaPartida;

    public NavegadorPantallas(VentanaParchis ventana, PantallaLobby pantallaLobby,
            PantallaPartida pantallaPartida) {
        this.ventana = ventana;
        this.pantallaLobby = pantallaLobby;
        this.pantallaPartida = pantallaPartida;
    }

    public void mostrarLobby() {
        mostrar(pantallaLobby);
    }

    public void mostrarPartida() {
        mostrar(pantallaPartida);
    }

    /** Los estados llegan del hilo de red; Swing solo se toca en su hilo. */
    private void mostrar(JComponent pantalla) {
        if (SwingUtilities.isEventDispatchThread()) {
            ventana.mostrar(pantalla);
        } else {
            SwingUtilities.invokeLater(() -> ventana.mostrar(pantalla));
        }
    }
}
