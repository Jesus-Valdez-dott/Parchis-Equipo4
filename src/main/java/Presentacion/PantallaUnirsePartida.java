package Presentacion;

import Presentacion.componentes.Boton;
import Presentacion.componentes.PanelDiseno;
import Presentacion.componentes.TituloDorado;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;

/**
 * Pantalla para unirse a una partida creada (storyboard: "Pantalla para unirse a una partida creada").
 * SIN lógica: muestra las tarjetas que le agreguen y expone los botones.
 *
 * @author Equipo 4
 */
public class PantallaUnirsePartida extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    /** Caben 3 tarjetas en la pantalla. */
    public static final int MAX_TARJETAS = 3;

    private final Boton atras = new Boton("Atras", Estilo.ROJO_PURO, new Color(0x30, 0x00, 0x00), 4, 10, false);
    private final List<TarjetaPartida> tarjetas = new ArrayList<>();

    public PantallaUnirsePartida() {
        agregar(new TituloDorado("Unirse a\npartida", 64), 430, 0, 380, 145);
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
}
