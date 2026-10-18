/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        if (jugadorActual != null && jugadorActual.equals(idJugador)) {
            return "ESPERANDO_LANZAMIENTO".equals(turno.getFase());
        }
        return false;
    }

    public boolean puedeMover(String idJugador, String idFicha) {
        Jugador jugadorActual = obtenerJugadorActual();
        if (jugadorActual != null && jugadorActual.equals(idJugador)) {
             return "ESPERANDO_MOVIMIENTO".equals(turno.getFase());
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
        // Juego delega esta complejidad a ReglasMovimiento.
        ReglasMovimiento reglas = new ReglasMovimiento();
        return reglas.obtenerFichasMovibles(this);
    }

    public Map<String, Integer> obtenerPosiciones() {
        // Mapa temporal para guardar los cálculos
        Map<String, Integer> puntajesDesordenados = new HashMap<>();

        // Calculamos la puntuación de cada jugador
        for (Jugador jugador : jugadores) {
            int casillasFaltantesTotales = 0;
            
            // Sumamos lo que le falta a cada una de sus 4 fichas
            for (Ficha ficha : jugador.obtenerFichas()) {
                casillasFaltantesTotales += ficha.calcularCasillasFaltantes();
            }
            
            puntajesDesordenados.put(jugador.getIdJugador(), casillasFaltantesTotales);
        }

        // El requerimiento exige orden descendente (el ganador arriba, o sea, el de MENOR puntaje)
        // Convertimos el mapa a una lista para ordenarlo
        List<Map.Entry<String, Integer>> listaPuntajes = new LinkedList<>(puntajesDesordenados.entrySet());

        // Ordenamos la lista de menor a mayor (el que le faltan 0 casillas va primero)
        Collections.sort(listaPuntajes, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        // Guardamos el resultado ordenado en un LinkedHashMap para mantener el orden de inserción
        Map<String, Integer> posicionesOrdenadas = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entrada : listaPuntajes) {
            posicionesOrdenadas.put(entrada.getKey(), entrada.getValue());
        }

        return posicionesOrdenadas;
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

    public List<String> getOpciones() {
        return opciones;
    }

    public List<Jugador> getJugadores() {
        return jugadores;
    }
}
