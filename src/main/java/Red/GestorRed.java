/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Red;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Jesús
 */
public class GestorRed implements IGestorRed{
    private Socket socketCliente;
    private ServerSocket socketServidor;

    @Override
    public void transmitirEvento(MensajeRed mensaje) {
        // Lógica para enviar el objeto MensajeRed a través de los Sockets TCP
    }
}
