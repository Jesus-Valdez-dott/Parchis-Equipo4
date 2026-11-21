package com.mycompany.parchis;

import Aplicacion.GestorJuego;
import Dominio.ColorJugador;
import Dominio.FachadaPartida;
import Dominio.Juego;
import Dominio.Jugador;
import Infraestructura.IGestorRed;
import Presentacion.ControladorJuego;
import Presentacion.ModeloPresentacion;
import Presentacion.PantallaPartida;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Ensamblador de un peer (Parchis.jar). Todos los peers ejecutan este mismo
 * programa; el que arranca como "host" mantiene el estado oficial.
 *
 * Uso:
 *   (sin argumentos)            demo de 4 jugadores en este equipo
 *   demo  [jugadores] [puerto]  igual, eligiendo 2, 3 o 4 jugadores
 *   host  [jugadores] [puerto]  este equipo es el Host y juega como J1
 *   cliente <idJugador> [ipHost] [puerto]   p. ej.: cliente J2 192.168.1.10 5000
 *
 * NOTA: mientras no esté Infraestructura.GestorRed, se usa RedLocalSimulada,
 * así que solo funciona el modo demo (todo en el mismo programa). Los modos
 * host/cliente entre computadoras funcionarán al cambiar crearRed().
 *
 * @author Jesús
 */
public class Parchis {

    private static final int PUERTO_POR_DEFECTO = 5000;
    private static final ColorJugador[] COLORES = {
        ColorJugador.ROJO, ColorJugador.AZUL, ColorJugador.AMARILLO, ColorJugador.VERDE
    };

    public static void main(String[] args) {
        String modo = args.length > 0 ? args[0].toLowerCase() : "demo";
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
                    default: {
                        int jugadores = entero(args, 1, 4);
                        int puerto = entero(args, 2, PUERTO_POR_DEFECTO);
                        demoLocal(jugadores, puerto);
                        break;
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "No se pudo iniciar: " + e.getMessage(),
                        "Parchís", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /** Peer Host: tiene la partida (Dominio) y abre el puerto para los demás. */
    public static PantallaPartida crearHost(int numJugadores, int puerto) throws Exception {
        // Dominio (esto lo harán los CU de configurar/unirse/iniciar partida)
        Juego juego = new Juego("PARTIDA_PARCHIS_01");
        for (int i = 1; i <= numJugadores; i++) {
            juego.agregarJugador(new Jugador("J" + i, "Jugador " + i, "avatar" + i, COLORES[i - 1]));
        }
        juego.iniciar();
        FachadaPartida fachada = new FachadaPartida(juego);

        // Infraestructura + Aplicación + Presentación
        GestorJuego gestorJuego = new GestorJuego("J1", crearRed(), fachada);
        PantallaPartida vista = armarPresentacion(gestorJuego, "J1 (Host)");
        gestorJuego.iniciarComoHost(puerto);
        return vista;
    }

    /** Peer cliente: no modifica la partida; pide acciones al Host. */
    public static PantallaPartida crearCliente(String idJugador, String ipHost, int puerto) throws Exception {
        GestorJuego gestorJuego = new GestorJuego(idJugador, crearRed(), null);
        PantallaPartida vista = armarPresentacion(gestorJuego, idJugador);
        gestorJuego.unirseAPartida(ipHost, puerto);
        return vista;
    }

    /**
     * Único lugar donde se elige la implementación de la red.
     * TODO: cuando esté listo, cambiar por:  return new Infraestructura.GestorRed();
     */
    private static IGestorRed crearRed() {
        return new RedLocalSimulada();
    }

    /** Capa de Presentación (MVC): modelo, controlador y vista observando al modelo. */
    private static PantallaPartida armarPresentacion(GestorJuego gestorJuego, String titulo) {
        ModeloPresentacion modelo = new ModeloPresentacion(gestorJuego.getIdJugadorLocal());
        ControladorJuego controlador = new ControladorJuego(modelo, gestorJuego);
        PantallaPartida vista = new PantallaPartida(controlador, modelo, titulo);
        modelo.agregarObservador(vista);
        return vista;
    }

    /** Requerimiento 2: para efectos de prueba, todos los jugadores en el mismo equipo. */
    private static void demoLocal(int numJugadores, int puerto) throws Exception {
        PantallaPartida host = crearHost(numJugadores, puerto);
        host.setLocation(20, 20);
        host.setVisible(true);
        for (int i = 2; i <= numJugadores; i++) {
            PantallaPartida cliente = crearCliente("J" + i, "localhost", puerto);
            cliente.setLocation(20 + ((i - 1) % 2) * 570, 20 + ((i - 1) / 2) * 400);
            cliente.setVisible(true);
        }
    }

    private static int entero(String[] args, int i, int porDefecto) {
        try {
            return args.length > i ? Integer.parseInt(args[i]) : porDefecto;
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }
}
