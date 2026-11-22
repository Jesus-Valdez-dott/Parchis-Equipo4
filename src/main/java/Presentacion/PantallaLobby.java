package Presentacion;

import Presentacion.componentes.Boton;
import Presentacion.componentes.Etiqueta;
import Presentacion.componentes.Icono;
import Presentacion.componentes.Notificacion;
import Presentacion.componentes.PanelDiseno;
import Presentacion.componentes.TituloDorado;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Lobby donde se espera el inicio de la partida (storyboard: "Lobby donde se espera el inicio de la partida").
 * SIN lógica: muestra los 4 lugares, el aviso de abajo a la izquierda y los botones.
 *
 * @author Equipo 4
 */
public class PantallaLobby extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    private static final int[][] LUGARES = {{205, 224}, {630, 224}, {205, 371}, {630, 371}};

    private final Boton volver = new Boton("← Volver", Estilo.TAN, Color.WHITE, 5, 10.5f, false);
    private final TarjetaJugadorLobby[] lugares = new TarjetaJugadorLobby[4];
    private final Notificacion notificacion = new Notificacion();
    private final Etiqueta mensajeInferior = new Etiqueta("Mínimo 2 jugadores para iniciar la partida",
            Etiqueta.Tipo.TEXTO, 9, Estilo.TEXTO_GRIS);
    private final Boton iniciarPartida = new Boton("INICIAR PARTIDA", Estilo.VERDE, Color.BLACK, 8, 11, true);

    public PantallaLobby() {
        agregar(volver, 58, 31, 72, 24);
        agregar(new TituloDorado("Lobby", 88), 450, 15, 300, 125);
        agregar(new Icono(Icono.Tipo.USUARIOS, new Color(0xD9, 0x5C, 0xE0)), 203, 182, 16, 14);
        agregar(new Etiqueta("Jugadores (2 - 4)", Etiqueta.Tipo.NEGRITA, 15, Color.WHITE, SwingConstants.LEFT), 221, 179, 200, 20);
        for (int i = 0; i < 4; i++) {
            lugares[i] = agregar(new TarjetaJugadorLobby(), LUGARES[i][0], LUGARES[i][1], 396, 130);
        }
        agregar(notificacion, 15, 545, 223, 86);
        notificacion.setVisible(false);
        agregar(mensajeInferior, 400, 600, 414, 18);
        iniciarPartida.setBrillo(Estilo.VERDE);
        agregar(iniciarPartida, 1046, 610, 133, 31);
    }

    /** Lugar 0..3 del lobby (para setJugador / setEsperando). */
    public TarjetaJugadorLobby getLugar(int i) { return lugares[i]; }

    /** Aviso de abajo a la izquierda (oculto al inicio). */
    public Notificacion getNotificacion() { return notificacion; }

    public void setMensajeInferior(String texto) { mensajeInferior.setText(texto); }

    public JButton getBotonVolver() { return volver; }
    public JButton getBotonIniciarPartida() { return iniciarPartida; }
}
