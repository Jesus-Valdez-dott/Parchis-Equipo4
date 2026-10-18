/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Red;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jesús
 */
public class GestorRed implements IGestorRed{
    // Atributos de tu diagrama UML[cite: 2]
    private Socket socketCliente;
    private ServerSocket socketServidor;
    
    // Atributos de apoyo para gestionar la red LAN TCP múltiple
    private List<ObjectOutputStream> flujosSalida;
    private INotificadorRed notificador; 

    public GestorRed() {
        this.flujosSalida = new ArrayList<>();
    }

    @Override
    public void transmitirEvento(MensajeRed mensaje) {
        // Enviar el mensaje serializado a todas las computadoras conectadas
        for (ObjectOutputStream out : flujosSalida) {
            try {
                out.writeObject(mensaje);
                out.flush();
            } catch (Exception e) {
                System.err.println("Error enviando datos TCP: " + e.getMessage());
            }
        }
    }

    /*
    LÓGICA DE SOCKETS TCP
    */
    
    // El primer jugador en crear la sala llama a este método (Host)
    public void iniciarServidor(int puerto) {
        try {
            socketServidor = new ServerSocket(puerto);
            System.out.println("Servidor LAN iniciado en puerto " + puerto);
            
            // Hilo en segundo plano para aceptar hasta 3 jugadores más (max 4 en total)[cite: 3]
            new Thread(() -> {
                while (flujosSalida.size() < 3) {
                    try {
                        Socket clienteNuevo = socketServidor.accept();
                        ObjectOutputStream out = new ObjectOutputStream(clienteNuevo.getOutputStream());
                        flujosSalida.add(out);
                        
                        // Iniciar hilo para escuchar a este cliente
                        iniciarEscucha(clienteNuevo);
                    } catch (Exception e) {
                        break;
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Los jugadores 2, 3 y 4 llaman a este método para unirse a la LAN del anfitrión[cite: 3]
    public void conectarComoCliente(String ipServidor, int puerto) {
        try {
            socketCliente = new Socket(ipServidor, puerto);
            ObjectOutputStream out = new ObjectOutputStream(socketCliente.getOutputStream());
            flujosSalida.add(out);
            
            // Iniciar hilo para escuchar lo que manda el servidor
            iniciarEscucha(socketCliente);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    // HILO DE ESCUCHA CONTINUA
    */
    private void iniciarEscucha(Socket socket) {
        new Thread(() -> {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                while (true) {
                    // Nos quedamos esperando a que llegue un MensajeRed por TCP
                    MensajeRed mensajeRecibido = (MensajeRed) in.readObject();
                    
                    // Cuando llega, le avisamos al Controlador de forma abstracta
                    if (notificador != null) {
                        notificador.mensajeRecibido(mensajeRecibido);
                    }
                }
            } catch (Exception e) {
                System.out.println("Jugador desconectado de la red LAN.");
            }
        }).start();
    }
    
    public void setNotificador(INotificadorRed notificador) {
        this.notificador = notificador;
    }
}
