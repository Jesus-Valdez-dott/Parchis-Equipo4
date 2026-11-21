package Presentacion;

import Aplicacion.TipoAccion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * «view» del MVC (capa de Presentación).
 * - Lo que hace el jugador se lo pasa al ControladorJuego (diagrama 3, paso 2).
 * - Observa al ModeloPresentacion: cuando este avisa, le pide los datos y se
 *   redibuja (diagrama 4, pasos 13-15 y 31).
 *
 * @author Jesús
 */
public class PantallaPartida extends JFrame implements ObservadorModelo {

    private static final long serialVersionUID = 1L;

    private final transient ControladorJuego controlador;
    private final transient ModeloPresentacion modelo;

    private final JLabel lblTurno = new JLabel("Conectando con el Host...");
    private final JLabel lblDado = new JLabel("Dado: -");
    private final JLabel lblAviso = new JLabel(" ");
    private final JButton btnLanzar = new JButton("Lanzar dado");
    private final JPanel panelFichas = new JPanel(new GridLayout(1, 4, 6, 6));
    private final PanelTablero panelTablero = new PanelTablero();

    public PantallaPartida(ControladorJuego controlador, ModeloPresentacion modelo, String titulo) {
        this.controlador = controlador;
        this.modelo = modelo;

        setTitle("Parchís - " + titulo);
        setSize(560, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inicializarComponentes();
        btnLanzar.setEnabled(false);
    }

    private void inicializarComponentes() {
        lblDado.setFont(lblDado.getFont().deriveFont(Font.BOLD, 22f));
        lblAviso.setForeground(new Color(180, 0, 0));
        btnLanzar.addActionListener(e -> solicitarLanzamiento());
        panelTablero.setAlSeleccionarFicha(this::seleccionarFicha);

        JPanel arriba = new JPanel(new GridLayout(3, 1));
        arriba.add(lblTurno);
        JPanel filaDado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filaDado.add(btnLanzar);
        filaDado.add(lblDado);
        arriba.add(filaDado);
        arriba.add(lblAviso);

        panelFichas.setBorder(BorderFactory.createTitledBorder("Mis fichas"));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(panelFichas, BorderLayout.NORTH);
        centro.add(panelTablero, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout(6, 6));
        getContentPane().add(arriba, BorderLayout.NORTH);
        getContentPane().add(centro, BorderLayout.CENTER);
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
        enHiloSwing(this::redibujar);
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
        mostrarResultadoDado(datos);
        mostrarTurno(datos);
        actualizarTablero(datos);
        habilitarFichasMovibles(datos);
        mostrarAviso(datos.getAviso());
    }

    // ==========================================
    // DIBUJO
    // ==========================================

    private void mostrarResultadoDado(DatosVista datos) {
        lblDado.setText(datos.getValorDado() > 0 ? "Dado: " + datos.getValorDado() : "Dado: -");
    }

    private void mostrarTurno(DatosVista datos) {
        if (!datos.isHayEstado()) {
            lblTurno.setText("Conectando con el Host...");
        } else if ("FINALIZADO".equals(datos.getEstadoJuego())) {
            boolean gane = datos.getMiIdJugador().equals(datos.getGanador());
            lblTurno.setText(gane ? "¡Ganaste la partida!" : "Partida terminada. Ganó " + datos.getGanador());
        } else if (!datos.isEnCurso()) {
            lblTurno.setText("Estado: " + datos.getEstadoJuego());
        } else if (datos.isMiTurno()) {
            lblTurno.setText(datos.isPuedeLanzar() ? "Tu turno: lanza el dado" : "Tu turno: elige una ficha");
        } else {
            lblTurno.setText("Turno de " + datos.getJugadorActual());
        }
        btnLanzar.setEnabled(datos.isPuedeLanzar());
    }

    private void actualizarTablero(DatosVista datos) {
        panelTablero.mostrar(datos.getPosiciones(), datos.getColoresJugadores(),
                datos.getFichasSeleccionables());
    }

    private void habilitarFichasMovibles(DatosVista datos) {
        panelFichas.removeAll();
        String prefijo = datos.getMiIdJugador() + "_F";
        for (Map.Entry<String, Integer> e : datos.getPosiciones().entrySet()) {
            if (!e.getKey().startsWith(prefijo)) {
                continue;
            }
            String idFicha = e.getKey();
            JButton btn = new JButton("<html><center>F" + idFicha.substring(prefijo.length())
                    + "<br>" + describirPosicion(e.getValue()) + "</center></html>");
            btn.setEnabled(datos.getFichasSeleccionables().contains(idFicha));
            btn.addActionListener(ev -> seleccionarFicha(idFicha));
            panelFichas.add(btn);
        }
        panelFichas.revalidate();
        panelFichas.repaint();
    }

    private void mostrarAviso(String aviso) {
        lblAviso.setText(aviso != null ? aviso : " ");
    }

    private String describirPosicion(int pos) {
        if (pos < 0) {
            return "Base";
        }
        if (pos <= 68) {
            return "Casilla " + pos;
        }
        if (pos < 76) {
            return "Pasillo " + (pos - 68);
        }
        return "META";
    }
}
