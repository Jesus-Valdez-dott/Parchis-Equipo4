package Presentacion;

import Aplicacion.TipoAccion;
import Presentacion.componentes.Boton;
import Presentacion.componentes.Etiqueta;
import Presentacion.componentes.Icono;
import Presentacion.componentes.Notificacion;
import Presentacion.componentes.PanelDiseno;
import Presentacion.componentes.TituloDorado;
import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Lobby donde se espera el inicio de la partida (storyboard: "Lobby donde se espera el inicio de la partida").
 *
 * CU Iniciar partida: es la «view» del MVC mientras la partida está en
 * ESPERANDO_JUGADORES. Observa al ModeloPresentacion y, cuando éste cambia,
 * pide los DatosVista y redibuja:
 * - los lugares con los inscritos (en orden de entrada) y "LISTO" en quienes ya confirmaron;
 * - INICIAR PARTIDA habilitado solo si se puede pedir el inicio;
 * - el aviso de abajo a la izquierda "X quiere iniciar la partida" con el botón
 *   "Confirmar" (no hay botón de rechazar: la solicitud solo espera);
 * - el mensaje de abajo con quién falta por confirmar.
 * Lo que hace el jugador se lo pasa al ControladorJuego; nunca al Dominio.
 *
 * @author Equipo 4
 */
public class PantallaLobby extends PanelDiseno implements ObservadorModelo {

    private static final long serialVersionUID = 1L;

    private static final int[][] LUGARES = {{205, 224}, {630, 224}, {205, 371}, {630, 371}};
    private static final String MENSAJE_MINIMO = "Mínimo 2 jugadores para iniciar la partida";
    private static final int DURACION_AVISO_MS = 3000;

    private final ControladorJuego controlador;   // null en la vista previa sin lógica
    private final ModeloPresentacion modelo;      // null en la vista previa sin lógica

    private final Boton volver = new Boton("← Volver", Estilo.TAN, Color.WHITE, 5, 10.5f, false);
    private final TarjetaJugadorLobby[] lugares = new TarjetaJugadorLobby[4];
    private final Notificacion notificacion = new Notificacion();
    private final Etiqueta mensajeInferior = new Etiqueta(MENSAJE_MINIMO,
            Etiqueta.Tipo.TEXTO, 9, Estilo.TEXTO_GRIS);
    private final Boton iniciarPartida = new Boton("INICIAR PARTIDA", Estilo.VERDE, Color.BLACK, 8, 11, true);

    /**
     * Lo que hace el botón verde del aviso. El aviso es compartido: el CU
     * Unirse a partida puede usarlo para "X quiere unirse" con otra acción.
     */
    private Runnable accionNotificacion = () -> { };
    private final Timer quitarAviso = new Timer(DURACION_AVISO_MS, e -> redibujar());

    /** Solo para la vista previa de pantallas (sin lógica). */
    public PantallaLobby() {
        this(null, null);
    }

    public PantallaLobby(ControladorJuego controlador, ModeloPresentacion modelo) {
        this.controlador = controlador;
        this.modelo = modelo;
        inicializarComponentes();
        if (modelo != null) {
            iniciarPartida.addActionListener(e -> solicitarInicio());
            notificacion.getBotonAceptar().addActionListener(e -> accionNotificacion.run());
            quitarAviso.setRepeats(false);
            redibujar();
        }
    }

    private void inicializarComponentes() {
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

    // ==========================================
    // INTERACCIÓN DEL USUARIO (SD-01 paso 1 y SD-03 paso 1)
    // ==========================================

    /** Botón INICIAR PARTIDA. */
    public void solicitarInicio() {
        controlador.solicitarAccion(TipoAccion.SOLICITAR_INICIO, null);
    }

    /** Botón "Confirmar" del aviso. */
    public void confirmarInicio() {
        controlador.solicitarAccion(TipoAccion.CONFIRMAR_INICIO, null);
    }

    // ==========================================
    // OBSERVER
    // ==========================================

    @Override
    public void notificarCambio() {
        enHiloSwing(this::redibujar);
    }

    /** Rechazo del Host o validación local: se muestra abajo unos segundos. */
    @Override
    public void notificarAviso() {
        enHiloSwing(() -> {
            redibujar();
            String aviso = modelo.obtenerDatosVista().getAviso();
            if (aviso != null) {
                mensajeInferior.setForeground(Estilo.ROSA);
                mensajeInferior.setText(aviso);
                quitarAviso.restart();
            }
        });
    }

    private void enHiloSwing(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r); // los estados llegan del hilo de red
        }
    }

    private void redibujar() {
        if (modelo == null) {
            return;
        }
        DatosVista datos = modelo.obtenerDatosVista();
        mostrarJugadores(datos);
        mostrarSolicitudInicio(datos);
        iniciarPartida.setEnabled(datos.isPuedeSolicitarInicio());
        repaint();
    }

    // ==========================================
    // DIBUJO
    // ==========================================

    /** Inscritos en orden de entrada (Host = JUGADOR 1); "LISTO" = ya confirmó. */
    private void mostrarJugadores(DatosVista datos) {
        List<String> inscritos = datos.getJugadoresInscritos();
        for (int i = 0; i < lugares.length; i++) {
            if (i < inscritos.size()) {
                String id = inscritos.get(i);
                lugares[i].setJugador(i + 1, nombre(datos, id),
                        datos.getAvataresJugadores().getOrDefault(id, 0), datos.haConfirmado(id));
            } else {
                lugares[i].setEsperando();
            }
        }
    }

    /** Aviso de abajo a la izquierda y mensaje inferior. */
    private void mostrarSolicitudInicio(DatosVista datos) {
        if (datos.isDebeConfirmarInicio()) {
            String solicitante = datos.getSolicitanteInicio();
            notificacion.mostrar(datos.getAvataresJugadores().getOrDefault(solicitante, 0),
                    nombre(datos, solicitante) + " quiere iniciar la partida", "Confirmar", null);
            accionNotificacion = this::confirmarInicio;
        } else {
            notificacion.setVisible(false);
            accionNotificacion = () -> { };
        }
        quitarAviso.stop();
        mensajeInferior.setForeground(Estilo.TEXTO_GRIS);
        mensajeInferior.setText(textoInferior(datos));
    }

    private String textoInferior(DatosVista datos) {
        if (!datos.isHayEstado()) {
            return "Conectando con el Host...";
        }
        if (!datos.isEsperandoJugadores()) {
            return datos.isEnCurso() ? "¡La partida inició!" : MENSAJE_MINIMO;
        }
        if (datos.getJugadoresInscritos().size() < 2) {
            return MENSAJE_MINIMO;
        }
        if (!datos.haySolicitudInicio()) {
            return "Cualquier jugador puede pedir iniciar la partida";
        }
        List<String> pendientes = new ArrayList<>();
        for (String id : datos.getPendientesDeConfirmar()) {
            pendientes.add(id.equals(datos.getMiIdJugador()) ? "ti" : nombre(datos, id));
        }
        return "Esperando confirmación de: " + String.join(", ", pendientes);
    }

    private static String nombre(DatosVista datos, String id) {
        return datos.getNombresJugadores().getOrDefault(id, id);
    }

    // ==========================================
    // ACCESO A COMPONENTES (vista previa y otros CU)
    // ==========================================

    /** Lugar 0..3 del lobby (para setJugador / setEsperando). */
    public TarjetaJugadorLobby getLugar(int i) { return lugares[i]; }

    /** Aviso de abajo a la izquierda (oculto al inicio). */
    public Notificacion getNotificacion() { return notificacion; }

    public void setMensajeInferior(String texto) { mensajeInferior.setText(texto); }

    public JButton getBotonVolver() { return volver; }
    public JButton getBotonIniciarPartida() { return iniciarPartida; }
}
