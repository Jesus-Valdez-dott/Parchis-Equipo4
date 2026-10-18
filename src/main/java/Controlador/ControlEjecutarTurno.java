/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import Modelo.Fachada.FachadaPartida;
import Red.IGestorRed;
import Red.INotificadorRed;
import Red.MensajeDado;
import Red.MensajeMovimiento;
import Red.MensajeRed;

/**
 *
 * @author Jesús
 */
public class ControlEjecutarTurno implements IController, INotificadorRed{
    private FachadaPartida fachada;
    private IGestorRed gestorRed;

    public ControlEjecutarTurno(FachadaPartida fachada, IGestorRed gestorRed) {
        this.fachada = fachada;
        this.gestorRed = gestorRed;
    }

    @Override
    public void ejecutarLanzamiento(String idJugador) {
        boolean exito = fachada.lanzarDado(idJugador);
        if (exito) {
            int valorObtenido = fachada.obtenerValorDado();
            // Notificamos a los demás jugadores en la red LAN a través de la abstracción
            MensajeDado mensaje = new MensajeDado(idJugador, valorObtenido);
            gestorRed.transmitirEvento(mensaje);
        }
    }

    @Override
    public void procesarMovimiento(String idJugador, String idFicha) {
        boolean exito = fachada.moverFicha(idJugador, idFicha);
        if (exito) {
            // Asumimos que la fachada calculó el destino internamente, 
            // pero para el mensaje necesitamos informar a dónde se movió.
            // (Esto requeriría un método en la fachada para obtener el último movimiento)
            int destinoCalculado = 0; // Valor de ejemplo
            MensajeMovimiento mensaje = new MensajeMovimiento(idJugador, idFicha, destinoCalculado);
            gestorRed.transmitirEvento(mensaje);
        }
    }
    
    @Override
    public void mensajeRecibido(MensajeRed mensaje) {
        if (mensaje instanceof MensajeDado) {
            MensajeDado msgDado = (MensajeDado) mensaje;
            System.out.println("El jugador " + msgDado.getIdJugadorEmisor() + " sacó un " + msgDado.getValorDado());
            // Aquí llamarías a fachada para replicar la acción en tu tablero local
        }
        else if (mensaje instanceof MensajeMovimiento) {
            MensajeMovimiento msgMov = (MensajeMovimiento) mensaje;
            // Aquí replicarías el movimiento del rival en tu tablero local
        }
    }
}
