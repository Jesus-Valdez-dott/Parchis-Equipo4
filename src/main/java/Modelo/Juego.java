/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jesús
 */
public class Juego {
    private String idPartida;
    // Cambiar EstadoJuego a un Enum más adelante
    private String estado; 
    private LinkedList<Jugador> ordenTurnos;
    private List<Jugador> jugadores;
    private Turno turno;
    private Tablero tablero;
    private List<String> opciones; // Opciones de configuración de la partida
    private int indiceJugadorActual;

    public Juego(String idPartida) {
        this.idPartida = idPartida;
        this.estado = "CONFIGURACION";
        this.ordenTurnos = new LinkedList<>();
        this.jugadores = new LinkedList<>();
        this.turno = new Turno();
        this.tablero = new Tablero();
        this.opciones = new LinkedList<>();
        this.indiceJugadorActual = 0;
    }

    public boolean puedeLanzar(String idJugador) {
        Jugador jugadorActual = obtenerJugadorActual();
        if (jugadorActual != null && jugadorActual.getIdJugador().equals(idJugador)) {
            return "ESPERANDO_LANZAMIENTO".equals(turno.getFase());
        }
        return false;
    }

    public boolean puedeMover(String idJugador, String idFicha) {
        Jugador jugadorActual = obtenerJugadorActual();
        if (jugadorActual != null && jugadorActual.getIdJugador().equals(idJugador)) {
             return "ESPERANDO_MOVIMIENTO".equals(turno.getFase());
             // La validación real de si la ficha específica se puede mover irá en ReglasMovimiento
        }
        return false;
    }

    public void guardarOpciones(List<String> opciones) {
        this.opciones = opciones;
        this.estado = "ESPERANDO_JUGADORES";
    }

    public boolean comprobarVictoria(String idJugador) {
        for (Jugador jugador : jugadores) {
            if (jugador.getIdJugador().equals(idJugador)) {
                return jugador.tieneTodasLasFichasEnMeta();
            }
        }
        return false;
    }

    public void finalizar(String idJugador) {
        this.estado = "FINALIZADO";
        // Aquí va la lógica para registrar al ganador si es necesario
    }

    public void avanzarTurno() {
        if (!jugadores.isEmpty()) {
            indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
            turno.habilitaLanzamiento(); // Se prepara el turno para el siguiente jugador
        }
    }

    public int obtenerValorDado() {
        return turno.getValorDado();
    }

    public Jugador obtenerJugadorActual() {
        if (!jugadores.isEmpty() && indiceJugadorActual >= 0 && indiceJugadorActual < jugadores.size()) {
            return jugadores.get(indiceJugadorActual);
        }
        return null;
    }

    public List<String> obtenerFichasMovibles() {
        // Esta es una delegación hacia la clase ReglasMovimiento.
        // Por ahora se devuelve una lista vacía para no romper el código.
        return new LinkedList<>(); 
    }

    public Map<String, Integer> obtenerPosiciones() {
        // Regla: "Las posiciones se determinarán sumando el número de casillas que le faltaron a cada una de las fichas de cada jugador para llegar a su meta."
        // Por ahora devolvemos un mapa vacío, la lógica matemática exacta se calculará después.
        Map<String, Integer> posiciones = new HashMap<>();
        for(Jugador j: jugadores){
             posiciones.put(j.getIdJugador(), 0); 
        }
        return posiciones;
    }

    public String obtenerEstadoJuego() {
        return this.estado;
    }

    // Métodos necesarios para la gestión interna que no están explícitos en el diagrama
    public void agregarJugador(Jugador jugador) {
        this.jugadores.add(jugador);
        this.ordenTurnos.add(jugador); // Se agregan en orden
    }

    public Tablero getTablero() {
        return tablero;
    }

    public Turno getTurno() {
        return turno;
    }
}
