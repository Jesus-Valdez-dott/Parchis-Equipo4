package com.mycompany.parchis;

import Aplicacion.GestorJuego;
import Dominio.ColorJugador;
import Dominio.FachadaPartida;
import Dominio.Juego;
import Dominio.Jugador;
import Infraestructura.GestorRed;
import Presentacion.ControladorJuego;
import Presentacion.ControladorSala;
import Presentacion.ControlUnirsePartida;
import Presentacion.ModeloPresentacion;
import Presentacion.PantallaInicio;
import Presentacion.PantallaLobby;
import Presentacion.PantallaPartida;
import Presentacion.PantallaUnirsePartida;
import Presentacion.VentanaParchis;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;

/**
 * Ensamblador de un peer (Parchis.jar). Todos los peers ejecutan este mismo
 * programa; el que arranca como "host" mantiene el estado oficial.
 *
 * Uso:
 *   (sin argumentos)            menú principal
 *   demo  [jugadores] [puerto]  igual, eligiendo 2, 3 o 4 jugadores
 *   host  [jugadores] [puerto]  este equipo es el Host y juega como J1
 *   cliente <idJugador> [ipHost] [puerto]   p. ej.: cliente J2 192.168.1.10 5000
 *
 * @author Jesús
 */
public class Parchis {

    private static final int PUERTO_POR_DEFECTO = 5000;
    private static final String ID_PARTIDA = "PARTIDA_PARCHIS_01";

    public static void main(String[] args) {
        String modo = args.length > 0 ? args[0].toLowerCase() : "menu";
        SwingUtilities.invokeLater(() -> {
            try {
                switch (modo) {
                    case "host": {
                        int jugadores = entero(args, 1, 4);
                        int puerto = entero(args, 2, PUERTO_POR_DEFECTO);
                        crearHost(jugadores, puerto).setVisible(true);
                        break;
                    }
                    case "cliente": {
                        String id = args.length > 1 ? args[1] : "J2";
                        String ip = args.length > 2 ? args[2] : "localhost";
                        int puerto = entero(args, 3, PUERTO_POR_DEFECTO);
                        crearCliente(id, ip, puerto).setVisible(true);
                        break;
                    }
                    case "demo": {
                        int jugadores = entero(args, 1, 4);
                        int puerto = entero(args, 2, PUERTO_POR_DEFECTO);
                        demoLocal(jugadores, puerto);
                        break;
                    }
                    default:
                        crearMenu().setVisible(true);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "No se pudo iniciar: " + e.getMessage(),
                        "Parchís", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /** Peer Host: crea una sala y espera a que se unan los demás jugadores. */
    public static VentanaParchis crearHost(int numJugadores, int puerto) throws Exception {
        Juego juego = new Juego(ID_PARTIDA, numJugadores);
        if (!juego.agregarJugador(new Jugador("J1", "Anfitrión", "avatar1", ColorJugador.ROJO))) {
            throw new IllegalStateException("No se pudo registrar al anfitrión");
        }
        FachadaPartida fachada = new FachadaPartida(juego);
        GestorJuego gestorJuego = new GestorJuego("J1", crearRed(), fachada);
        new ControlUnirsePartida(gestorJuego.getGestorRed(), fachada, null, gestorJuego,
                gestorJuego::publicarEstadoOficial, ID_PARTIDA, "J1");
        VentanaParchis ventana = armarPresentacion(gestorJuego, "J1 (Host)", true);
        gestorJuego.iniciarComoHost(puerto);
        return ventana;
    }

    /** Peer cliente: se registra con el Host y espera en el lobby hasta que inicie. */
    public static VentanaParchis crearCliente(String idJugador, String ipHost, int puerto) throws Exception {
        GestorRed red = crearRed();
        GestorJuego gestorJuego = new GestorJuego(idJugador, red, null);
        ControlUnirsePartida controlUnion = new ControlUnirsePartida(
                red, null, null, gestorJuego, null, ID_PARTIDA, idJugador);
        VentanaParchis ventana = armarPresentacion(gestorJuego, idJugador, false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                controlUnion.unirsePartida(ipHost, puerto);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (java.util.concurrent.ExecutionException e) {
                    gestorJuego.cerrar();
                    JOptionPane.showMessageDialog(ventana,
                            "No se pudo unir a la partida: " + e.getCause().getMessage(),
                            "Parchís", JOptionPane.ERROR_MESSAGE);
                    ventana.dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    gestorJuego.cerrar();
                    ventana.dispose();
                }
            }
        }.execute();
        return ventana;
    }

    private static GestorRed crearRed() {
        return new GestorRed();
    }

    /** Capa de Presentación (MVC): modelo, controlador y vista observando al modelo. */
    private static VentanaParchis armarPresentacion(GestorJuego gestorJuego, String titulo, boolean esHost) {
        ModeloPresentacion modelo = new ModeloPresentacion(gestorJuego.getIdJugadorLocal());
        ControladorJuego controlador = new ControladorJuego(modelo, gestorJuego);
        PantallaPartida vista = new PantallaPartida(controlador, modelo);
        modelo.agregarObservador(vista);
        VentanaParchis ventana = new VentanaParchis("Parchís - " + titulo);
        PantallaLobby lobby = new PantallaLobby();
        ControladorSala sala = new ControladorSala(ventana, lobby, vista, modelo, esHost,
                () -> {
                    if (!gestorJuego.iniciarPartida()) {
                        lobby.setMensajeInferior("Se requieren 2 jugadores para iniciar");
                    }
                },
                () -> {
                    gestorJuego.cerrar();
                    ventana.setTitle("Parchís");
                    ventana.mostrar(crearPantallaInicio(ventana));
                });
        modelo.agregarObservador(sala);
        ventana.mostrar(lobby);
        return ventana;
    }

    /** Abre varios clientes TCP locales para probar una partida completa en un solo equipo. */
    private static void demoLocal(int numJugadores, int puerto) throws Exception {
        // Ventanas más chicas para que quepan varias en la pantalla (todo se escala)
        VentanaParchis host = crearHost(numJugadores, puerto);
        host.setBounds(10, 10, 680, 410);
        host.setVisible(true);
        for (int i = 2; i <= numJugadores; i++) {
            VentanaParchis cliente = crearCliente("J" + i, "localhost", puerto);
            cliente.setBounds(10 + ((i - 1) % 2) * 690, 10 + ((i - 1) / 2) * 420, 680, 410);
            cliente.setVisible(true);
        }
    }

    private static VentanaParchis crearMenu() {
        VentanaParchis ventana = new VentanaParchis("Parchís");
        ventana.mostrar(crearPantallaInicio(ventana));
        return ventana;
    }

    private static PantallaInicio crearPantallaInicio(VentanaParchis ventana) {
        PantallaInicio inicio = new PantallaInicio();
        inicio.getBotonUnirsePartida().addActionListener(e -> mostrarPantallaUnion(ventana, inicio));
        inicio.getBotonCrearPartida().addActionListener(e -> {
            try {
                VentanaParchis host = crearHost(4, PUERTO_POR_DEFECTO);
                host.setVisible(true);
                ventana.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ventana,
                        "No se pudo crear la partida: " + ex.getMessage(),
                        "Parchís", JOptionPane.ERROR_MESSAGE);
            }
        });
        return inicio;
    }

    private static void mostrarPantallaUnion(VentanaParchis ventana, PantallaInicio inicio) {
        PantallaUnirsePartida pantalla = new PantallaUnirsePartida();
        pantalla.getBotonAtras().addActionListener(e -> ventana.mostrar(inicio));
        pantalla.setAccionSolicitarConexion(() -> solicitarConexion(ventana, pantalla));
        pantalla.getBotonConectarPorIp().addActionListener(e -> pantalla.solicitarDatosConexion());
        ventana.mostrar(pantalla);
    }

    private static void solicitarConexion(VentanaParchis ventana, PantallaUnirsePartida pantalla) {
        String ip = JOptionPane.showInputDialog(ventana,
                "Ingresa la IP o el nombre del equipo anfitrión:", "localhost");
        if (ip == null) {
            return;
        }
        ip = ip.trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(ventana, "La IP no puede estar vacía.",
                    "Parchís", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String puertoTexto = JOptionPane.showInputDialog(ventana,
                "Ingresa el puerto de la partida:", Integer.toString(PUERTO_POR_DEFECTO));
        if (puertoTexto == null) {
            return;
        }
        int puerto;
        try {
            puerto = Integer.parseInt(puertoTexto.trim());
            if (puerto < 1 || puerto > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(ventana, "Ingresa un puerto entre 1 y 65535.",
                    "Parchís", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String idJugador = "J-" + UUID.randomUUID();
        GestorRed red = new GestorRed();
        GestorJuego gestorJuego = new GestorJuego(idJugador, red, null);
        ControlUnirsePartida controlUnion = new ControlUnirsePartida(
                red, null, pantalla, gestorJuego, null, ID_PARTIDA, idJugador);
        pantalla.setControlUnirsePartida(controlUnion);
        ModeloPresentacion modelo = new ModeloPresentacion(idJugador);
        ControladorJuego controlador = new ControladorJuego(modelo, gestorJuego);
        PantallaPartida partida = new PantallaPartida(controlador, modelo);
        modelo.agregarObservador(partida);
        PantallaLobby lobby = new PantallaLobby();
        VentanaParchis sala = ventana;
        ControladorSala controladorSala = new ControladorSala(sala, lobby, partida, modelo, false,
                () -> { },
                () -> {
                    gestorJuego.cerrar();
                    sala.setTitle("Parchís");
                    sala.mostrar(crearPantallaInicio(sala));
                });
        modelo.agregarObservador(controladorSala);
        sala.setTitle("Parchís - " + idJugador);
        sala.mostrar(lobby);
        pantalla.getBotonConectarPorIp().setEnabled(false);

        String hostIp = ip;
        SwingWorker<Void, Void> conexion = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                pantalla.unirsePartida(hostIp, puerto);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (java.util.concurrent.ExecutionException e) {
                    red.cerrarComunicacion();
                    JOptionPane.showMessageDialog(sala,
                            "No se pudo unir a la partida: " + e.getCause().getMessage(),
                            "Parchís", JOptionPane.ERROR_MESSAGE);
                    sala.setTitle("Parchís");
                    sala.mostrar(pantalla);
                    pantalla.getBotonConectarPorIp().setEnabled(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    red.cerrarComunicacion();
                    sala.mostrar(pantalla);
                    pantalla.getBotonConectarPorIp().setEnabled(true);
                }
            }
        };
        conexion.execute();
    }

    private static int entero(String[] args, int i, int porDefecto) {
        try {
            return args.length > i ? Integer.parseInt(args[i]) : porDefecto;
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }
}
