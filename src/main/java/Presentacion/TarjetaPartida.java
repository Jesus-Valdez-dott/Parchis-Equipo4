package Presentacion;

import Presentacion.componentes.Boton;
import Presentacion.componentes.Etiqueta;
import Presentacion.componentes.Icono;
import Presentacion.componentes.PillTexto;
import Presentacion.componentes.Tarjeta;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Tarjeta de una partida disponible (pantalla "Unirse a partida").
 * Solo muestra datos; el botón se conecta con getBotonUnirse().
 *
 * @author Equipo 4
 */
public class TarjetaPartida extends Tarjeta {

    private static final long serialVersionUID = 1L;

    private final PillTexto anfitrion = new PillTexto("", 9.5f, true, false, 3);
    private final Etiqueta jugadores = new Etiqueta("", Etiqueta.Tipo.NEGRITA, 26, new Color(0x1A, 0x10, 0x10), SwingConstants.LEFT);
    private final PillTexto fichas = new PillTexto("", 24, true, true, 12);
    private final PillTexto salida = new PillTexto("", 24, true, true, 12);
    private final PillTexto seguras = new PillTexto("", 24, true, true, 12);
    private final Boton unirse = new Boton("Unirse a partida", Estilo.VERDE, new Color(0x10, 0x20, 0x10), 3, 9.5f, false);

    public TarjetaPartida() {
        super(519, 161, Estilo.TAN, 16);
        agregar(new Etiqueta("JUGADOR", Etiqueta.Tipo.NEGRITA, 6, Estilo.ROSA, SwingConstants.LEFT), 16, 9, 80, 10);
        agregar(anfitrion, 16, 22, 239, 21);
        agregar(new Icono(Icono.Tipo.USUARIO, new Color(0x1A, 0x10, 0x10)), 432, 14, 26, 28);
        agregar(jugadores, 460, 12, 60, 32);
        agregar(fichas, 6, 51, 237, 44);
        agregar(salida, 255, 51, 223, 44);
        agregar(seguras, 8, 105, 238, 44);
        agregar(unirse, 394, 106, 84, 41);
    }

    public void setDatos(String nombreAnfitrion, int jugadoresUnidos, int maxJugadores,
            int numFichas, int numeroSalida, int casillasSeguras) {
        anfitrion.setTexto(nombreAnfitrion);
        jugadores.setText(jugadoresUnidos + "/" + maxJugadores);
        fichas.setTexto("# Fichas: " + numFichas);
        salida.setTexto("# Salida Dado: " + numeroSalida);
        seguras.setTexto("Casillas seguras: " + casillasSeguras);
    }

    public JButton getBotonUnirse() { return unirse; }
}
