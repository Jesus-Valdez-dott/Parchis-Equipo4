package Presentacion;

import Presentacion.componentes.Boton;
import Presentacion.componentes.PanelDiseno;
import Presentacion.componentes.SelectorNumerico;
import Presentacion.componentes.TituloDorado;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import javax.swing.JButton;

/**
 * Pantalla para configurar una partida (storyboard: "Pantalla para configurar una partida").
 * SIN lógica: los botones "-" y "+" no cambian nada por sí solos.
 *
 * Agregado al storyboard: botón "← Volver" (como el del Lobby) para poder regresar.
 *
 * @author Equipo 4
 */
public class PantallaConfiguracionPartida extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    private final Boton volver = new Boton("← Volver", Estilo.TAN, Color.WHITE, 5, 10.5f, false);
    private final SelectorNumerico fichasPorJugador = new SelectorNumerico("NÚMERO DE FICHAS POR JUGADOR", 4);
    private final SelectorNumerico casillasSeguras = new SelectorNumerico("CASILLAS SEGURAS POR ALA", 3);
    private final SelectorNumerico numeroSalida = new SelectorNumerico("NÚMERO PARA SALIR DEL CÍRCULO", 5);
    private final Boton crearPartida = new Boton("Crear partida", Estilo.VERDE, new Color(0x10, 0x20, 0x10), 4, 11, false);

    public PantallaConfiguracionPartida() {
        agregar(volver, 58, 31, 72, 24);
        agregar(new TituloDorado("CONFIGURACIÓN\nDE LA PARTIDA", 50), 380, 22, 440, 140);
        agregar(fichasPorJugador, 301, 251, 597, 66);
        agregar(casillasSeguras, 301, 328, 597, 66);
        agregar(numeroSalida, 301, 406, 597, 66);
        agregar(crearPartida, 1077, 600, 85, 41);
    }

    public JButton getBotonVolver() { return volver; }
    public JButton getBotonCrearPartida() { return crearPartida; }
    public SelectorNumerico getSelectorFichasPorJugador() { return fichasPorJugador; }
    public SelectorNumerico getSelectorCasillasSeguras() { return casillasSeguras; }
    public SelectorNumerico getSelectorNumeroSalida() { return numeroSalida; }
}
