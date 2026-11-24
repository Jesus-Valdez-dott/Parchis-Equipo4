package Presentacion;

import Presentacion.componentes.Boton;
import Presentacion.componentes.PanelDiseno;
import Presentacion.componentes.TituloDorado;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JButton;

/**
 * Pantalla para ingresar los datos de conexión al Host y mostrar el resultado
 * de la solicitud de unión.
 *
 * @author Equipo 4
 */
public class PantallaUnirsePartida extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    /** Caben 3 tarjetas en la pantalla. */
    public static final int MAX_TARJETAS = 3;

    private final Boton atras = new Boton("Atras", Estilo.ROJO_PURO, new Color(0x30, 0x00, 0x00), 4, 10, false);
    private final Boton conectarPorIp = new Boton("Conectar por IP", Estilo.VERDE, Color.BLACK, 4, 10, false);
    private final List<TarjetaPartida> tarjetas = new ArrayList<>();
    private ControlUnirsePartida controlUnirsePartida;
    private Runnable accionSolicitarConexion;

    public PantallaUnirsePartida() {
        agregar(new TituloDorado("Unirse a\npartida", 64), 430, 0, 380, 145);
        agregar(conectarPorIp, 900, 606, 176, 42);
        agregar(atras, 1092, 606, 84, 42);
    }

    /**
     * Agrega la tarjeta de una partida (máximo 3) y la regresa para conectar su botón.
     * Regresa null si ya no caben más.
     */
    public TarjetaPartida agregarPartida(String nombreAnfitrion, int jugadoresUnidos, int maxJugadores,
            int numFichas, int numeroSalida, int casillasSeguras) {
        if (tarjetas.size() >= MAX_TARJETAS) {
            return null;
        }
        TarjetaPartida t = new TarjetaPartida();
        t.setDatos(nombreAnfitrion, jugadoresUnidos, maxJugadores, numFichas, numeroSalida, casillasSeguras);
        agregar(t, 340, 165 + tarjetas.size() * 176, 519, 161);
        tarjetas.add(t);
        revalidate();
        repaint();
        return t;
    }

    public void limpiarPartidas() {
        for (TarjetaPartida t : tarjetas) {
            remove(t);
        }
        tarjetas.clear();
        revalidate();
        repaint();
    }

    public List<TarjetaPartida> getTarjetas() { return tarjetas; }
    public JButton getBotonAtras() { return atras; }
    public JButton getBotonConectarPorIp() { return conectarPorIp; }

    public void setControlUnirsePartida(ControlUnirsePartida controlUnirsePartida) {
        this.controlUnirsePartida = controlUnirsePartida;
    }

    public void setAccionSolicitarConexion(Runnable accionSolicitarConexion) {
        this.accionSolicitarConexion = accionSolicitarConexion;
    }

    public void solicitarDatosConexion() {
        if (accionSolicitarConexion != null) {
            accionSolicitarConexion.run();
        }
    }

    public void unirsePartida(String ip, int puerto) throws java.io.IOException {
        if (controlUnirsePartida == null) {
            throw new IllegalStateException("No se configuró el controlador de unión");
        }
        controlUnirsePartida.unirsePartida(ip, puerto);
    }

    public void mostrarResultado(String resultado) {
        Runnable mostrar = () -> JOptionPane.showMessageDialog(
                this, resultado, "Unirse a partida", JOptionPane.INFORMATION_MESSAGE);
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            mostrar.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(mostrar);
        }
    }

    public void mostrarError(String mensaje) {
        Runnable mostrar = () -> JOptionPane.showMessageDialog(
                this, mensaje, "No se pudo unir", JOptionPane.ERROR_MESSAGE);
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            mostrar.run();
        } else {
            javax.swing.SwingUtilities.invokeLater(mostrar);
        }
    }
}
