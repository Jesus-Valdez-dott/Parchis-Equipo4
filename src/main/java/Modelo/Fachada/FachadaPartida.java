/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo.Fachada;

import Modelo.Dado;
import Modelo.Juego;
import Modelo.ReglasMovimiento;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jesús
 */
public class FachadaPartida implements IModeloLectura{
    private Juego juego;
    private ReglasMovimiento reglasMovimiento;
    private Dado dado;
    private List<IObservadorPartida> observadores;

    public FachadaPartida() {
        this.juego = new Juego("PARTIDA_PARCHIS_01");
        this.reglasMovimiento = new ReglasMovimiento();
        this.dado = new Dado();
        this.observadores = new ArrayList<>();
    }

    // MÉTODOS DE NEGOCIO (Solo para el Controlador)
    
    public boolean lanzarDado(String idJugador) {
        if (juego.puedeLanzar(idJugador)) {
            int valor = dado.lanzar();
            juego.getTurno().registrarTirada(valor);
            notificarObservadores(); // Avisa a la vista que el dado cambió
            return true;
        }
        return false;
    }

    public boolean moverFicha(String idJugador, String idFicha) {
        if (juego.puedeMover(idJugador, idFicha)) {
            // Aquí la Fachada orquesta el movimiento apoyándose en Juego y Reglas
            // juego.getTablero().moverFicha(...);
            juego.avanzarTurno();
            notificarObservadores(); // Avisa a la vista que las fichas se movieron
            return true;
        }
        return false;
    }

    // GESTIÓN DEL PATRÓN OBSERVER

    public void agregarObservador(IObservadorPartida observador) {
        this.observadores.add(observador);
    }

    public void eliminarObservador(IObservadorPartida observador) {
        this.observadores.remove(observador);
    }

    private void notificarObservadores() {
        for (IObservadorPartida obs : observadores) {
            obs.actualizar();
        }
    }
    
    // MÉTODOS DE SOLO LECTURA (Exclusivos para la Vista)

    @Override
    public int obtenerValorDado() {
        return juego.obtenerValorDado();
    }

    @Override
    public String obtenerJugadorActual() {
        return juego.obtenerJugadorActual() != null ? juego.obtenerJugadorActual().getIdJugador() : "N/A";
    }

    @Override
    public List<String> obtenerFichasMovibles() {
        return reglasMovimiento.obtenerFichasMovibles(juego);
    }

    @Override
    public Map<String, Integer> obtenerPosiciones() {
        return juego.obtenerPosiciones();
    }

    @Override
    public String obtenerEstadoJuego() {
        return juego.obtenerEstadoJuego();
    }
}
