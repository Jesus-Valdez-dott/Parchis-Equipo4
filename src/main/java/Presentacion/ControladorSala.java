package Presentacion;

import java.util.Map;
import javax.swing.SwingUtilities;

/**
 * Presents the waiting room and switches to the board when the Host starts the game.
 */
public final class ControladorSala implements ObservadorModelo {

    private final VentanaParchis ventana;
    private final PantallaLobby lobby;
    private final PantallaPartida partida;
    private final ModeloPresentacion modelo;
    private final boolean esHost;
    private final Runnable iniciarPartida;
    private final Runnable salir;

    public ControladorSala(VentanaParchis ventana, PantallaLobby lobby, PantallaPartida partida,
            ModeloPresentacion modelo, boolean esHost, Runnable iniciarPartida, Runnable salir) {
        this.ventana = ventana;
        this.lobby = lobby;
        this.partida = partida;
        this.modelo = modelo;
        this.esHost = esHost;
        this.iniciarPartida = iniciarPartida;
        this.salir = salir;
        lobby.getBotonIniciarPartida().setVisible(esHost);
        lobby.getBotonIniciarPartida().addActionListener(e -> iniciarPartida.run());
        lobby.getBotonVolver().addActionListener(e -> salir.run());
        redibujar();
    }

    @Override
    public void notificarCambio() {
        enHiloSwing(this::redibujar);
    }

    @Override
    public void notificarAviso() {
        enHiloSwing(() -> lobby.setMensajeInferior(modelo.obtenerDatosVista().getAviso()));
    }

    private void redibujar() {
        DatosVista datos = modelo.obtenerDatosVista();
        if (!datos.isHayEstado()) {
            lobby.setMensajeInferior("Conectando con el Host...");
            ventana.mostrar(lobby);
            return;
        }

        int asiento = 0;
        for (Map.Entry<String, String> jugador : datos.getColoresJugadores().entrySet()) {
            if (asiento == 4) {
                break;
            }
            String id = jugador.getKey();
            lobby.getLugar(asiento).setJugador(asiento + 1,
                    datos.getNombresJugadores().getOrDefault(id, id),
                    datos.getAvataresJugadores().getOrDefault(id, 0), true);
            asiento++;
        }
        for (int i = asiento; i < 4; i++) {
            lobby.getLugar(i).setEsperando();
        }

        if ("EN_CURSO".equals(datos.getEstadoJuego()) || "FINALIZADO".equals(datos.getEstadoJuego())) {
            ventana.mostrar(partida);
        } else {
            lobby.setMensajeInferior(esHost
                    ? (asiento < 2 ? "Mínimo 2 jugadores para iniciar la partida"
                            : "La sala está lista. Puedes iniciar la partida.")
                    : "Esperando a que el anfitrión inicie la partida");
            lobby.getBotonIniciarPartida().setEnabled(asiento >= 2);
            ventana.mostrar(lobby);
        }
    }

    private static void enHiloSwing(Runnable accion) {
        if (SwingUtilities.isEventDispatchThread()) {
            accion.run();
        } else {
            SwingUtilities.invokeLater(accion);
        }
    }
}
