/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Modelo.Fachada;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Jesús
 */
public interface IModeloLectura {
    int obtenerValorDado();
    String obtenerJugadorActual();
    List<String> obtenerFichasMovibles();
    Map<String, Integer> obtenerPosiciones();
    String obtenerEstadoJuego();
}
