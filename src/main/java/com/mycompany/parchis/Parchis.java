/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.parchis;

import Controlador.ControlEjecutarTurno;
import Modelo.Fachada.FachadaPartida;
import Red.GestorRed;
import Vista.PantallaPartida;

/**
 *
 * @author Jesús
 */
public class Parchis {

    public static void main(String[] args) {
        // 1. Instanciamos las dependencias
        FachadaPartida fachada = new FachadaPartida();
        GestorRed gestorRed = new GestorRed();
        
        // 2. Inyectamos la fachada y la red al controlador
        ControlEjecutarTurno controlador = new ControlEjecutarTurno(fachada, gestorRed);
        
        // 3. Le decimos a la red quién la va a escuchar
        gestorRed.setNotificador(controlador);
        
        // 4. Creamos la vista, inyectando la versión de solo lectura del modelo y la abstracción del controlador
        String miJugador = "JUGADOR_LOCAL_01";
        PantallaPartida vista = new PantallaPartida(fachada, controlador, miJugador);
        
        // 5. Registramos la vista como observador de la partida
        fachada.agregarObservador(vista);
        
        // 6. Mostramos la ventana
        vista.setVisible(true);
    }
}
