package Presentacion;

import Aplicacion.TipoAccion;
import Presentacion.componentes.Avatar;
import Presentacion.componentes.Dado;
import Presentacion.componentes.Etiqueta;
import Presentacion.componentes.MensajeSuperpuesto;
import Presentacion.componentes.PanelDiseno;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * «view» del MVC (capa de Presentación). Diseño del storyboard: tablero al
 * centro, cada jugador en la esquina de su color y el dado junto a quien tiene
 * el turno.
 *
 * - Lo que hace el jugador se lo pasa al ControladorJuego (diagrama 3, paso 2):
 *   clic en el dado = lanzar; clic en una ficha resaltada = moverla.
 * - Observa al ModeloPresentacion: cuando este avisa, le pide los datos y se
 *   redibuja (diagrama 4, pasos 13-15 y 31).
 *
 * @author Jesús
 */
public class PantallaPartida extends PanelDiseno implements ObservadorModelo {

    private static final long serialVersionUID = 1L;

    /** Esquinas en orden: arriba-izq, arriba-der, abajo-izq, abajo-der. */
    private static final String[] COLOR_ESQUINA = {"ROJO", "AZUL", "VERDE", "AMARILLO"};
    private static final boolean[] ESQUINA_ARRIBA = {true, true, false, false};
    private static final int[] X_ESQUINA = {0, 938, 0, 938};

    private final transient ControladorJuego controlador;
    private final transient ModeloPresentacion modelo;

    private final PanelTablero panelTablero = new PanelTablero();
    private final Etiqueta[] nombres = new Etiqueta[4];
    private final Avatar[] avatares = new Avatar[4];
    private final Dado[] dados = new Dado[4];
    private final Etiqueta[] indicaciones = new Etiqueta[4];
    private final MensajeSuperpuesto mensaje = new MensajeSuperpuesto();

    public PantallaPartida(ControladorJuego controlador, ModeloPresentacion modelo) {
        this.controlador = controlador;
        this.modelo = modelo;
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        agregar(mensaje, 210, 192, 780, 222);
        for (int i = 0; i < 4; i++) {
            int x = X_ESQUINA[i];
            boolean arriba = ESQUINA_ARRIBA[i];
            nombres[i] = agregar(new Etiqueta("", Etiqueta.Tipo.NEGRITA, 18, Color.WHITE), x, arriba ? 6 : 634, 262, 28);
            avatares[i] = agregar(new Avatar(0), x + 64, arriba ? 38 : 500, 132, 132);
            avatares[i].setCursor(null);
            dados[i] = agregar(new Dado(), x + 94, arriba ? 176 : 413, 72, 82);
            dados[i].setPiquitoAbajo(!arriba);
            dados[i].addActionListener(e -> solicitarLanzamiento());
            indicaciones[i] = agregar(new Etiqueta("", Etiqueta.Tipo.TEXTO, 12, new Color(0xE8, 0xDC, 0xF0)),
                    x, arriba ? 262 : 386, 262, 22);
        }
        agregar(panelTablero, 263, 0, 675, 675);
        panelTablero.setAlSeleccionarFicha(this::seleccionarFicha);
        redibujar();
    }

    // ==========================================
    // INTERACCIÓN DEL USUARIO (diagrama 3, pasos 1-2)
    // ==========================================

    public void solicitarLanzamiento() {
        controlador.solicitarAccion(TipoAccion.LANZAR_DADO, null);
    }

    public void seleccionarFicha(String idFicha) {
        controlador.solicitarAccion(TipoAccion.MOVER_FICHA, idFicha);
    }

    // ==========================================
    // OBSERVER (diagrama 4, pasos 13-15 y 31)
    // ==========================================

    @Override
    public void notificarCambio() {
        enHiloSwing(this::redibujar);
    }

    @Override
    public void notificarAviso() {
        enHiloSwing(() -> {
            redibujar();
            String aviso = modelo.obtenerDatosVista().getAviso();
            if (aviso != null) {
                mensaje.mostrar(aviso, 2500);
            }
        });
    }

    private void enHiloSwing(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r); // los avisos pueden llegar del hilo de red
        }
    }

    private void redibujar() {
        DatosVista datos = modelo.obtenerDatosVista();
        mostrarJugadores(datos);
        mostrarDado(datos);
        actualizarTablero(datos);
        mostrarEstadoGeneral(datos);
    }

    // ==========================================
    // DIBUJO
    // ==========================================

    /** Cada jugador aparece en la esquina de su color; las esquinas sin jugador se ocultan. */
    private void mostrarJugadores(DatosVista datos) {
        for (int i = 0; i < 4; i++) {
            String id = jugadorDeColor(datos, COLOR_ESQUINA[i]);
            boolean hay = id != null;
            nombres[i].setVisible(hay);
            avatares[i].setVisible(hay);
            if (hay) {
                String nombre = datos.getNombresJugadores().getOrDefault(id, id);
                nombres[i].setText(id.equals(datos.getMiIdJugador()) ? nombre + " (tú)" : nombre);
                avatares[i].setIndice(datos.getAvataresJugadores().getOrDefault(id, 0));
                avatares[i].setResaltado(datos.isEnCurso() && id.equals(datos.getJugadorActual()));
            }
        }
    }

    /** El dado aparece solo junto al jugador en turno. */
    private void mostrarDado(DatosVista datos) {
        for (int i = 0; i < 4; i++) {
            String id = jugadorDeColor(datos, COLOR_ESQUINA[i]);
            boolean enTurno = datos.isEnCurso() && id != null && id.equals(datos.getJugadorActual());
            dados[i].setVisible(enTurno);
            dados[i].setValor(datos.getValorDado());
            dados[i].setEnabled(datos.isPuedeLanzar());
            dados[i].setDestacado(enTurno && datos.isPuedeLanzar());
            String texto = "";
            if (enTurno && datos.isMiTurno()) {
                texto = datos.isPuedeLanzar() ? "Haz clic en el dado para lanzar" : "Elige una ficha resaltada";
            }
            indicaciones[i].setText(texto);
            indicaciones[i].setVisible(enTurno);
        }
    }

    private void actualizarTablero(DatosVista datos) {
        panelTablero.mostrar(datos.getPosiciones(), datos.getColoresJugadores(),
                datos.getFichasSeleccionables());
    }

    private void mostrarEstadoGeneral(DatosVista datos) {
        if (!datos.isHayEstado()) {
            mensaje.mostrar("Conectando con el Host...", 0);
        } else if ("FINALIZADO".equals(datos.getEstadoJuego())) {
            boolean gane = datos.getMiIdJugador().equals(datos.getGanador());
            String ganador = datos.getNombresJugadores().getOrDefault(datos.getGanador(), datos.getGanador());
            mensaje.mostrar(gane ? "¡Ganaste la partida!" : "Partida terminada. Ganó " + ganador, 0);
        } else if (datos.getAviso() == null) {
            mensaje.ocultar();
        }
    }

    private static String jugadorDeColor(DatosVista datos, String color) {
        for (Map.Entry<String, String> e : datos.getColoresJugadores().entrySet()) {
            if (color.equals(e.getValue())) {
                return e.getKey();
            }
        }
        return null;
    }
}
