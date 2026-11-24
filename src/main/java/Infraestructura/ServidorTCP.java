package Infraestructura;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

/**
 * Accepts TCP peers and delegates their sockets to GestorRed.
 */
public final class ServidorTCP {

    private final Consumer<Socket> alConectar;
    private final Consumer<IOException> alFallar;
    private volatile ServerSocket serverSocket;
    private volatile boolean activo;

    public ServidorTCP(Consumer<Socket> alConectar, Consumer<IOException> alFallar) {
        this.alConectar = alConectar;
        this.alFallar = alFallar;
    }

    public synchronized void iniciar(int puerto) throws IOException {
        if (activo) {
            throw new IllegalStateException("El servidor TCP ya está iniciado");
        }
        serverSocket = new ServerSocket(puerto);
        activo = true;
        Thread aceptador = new Thread(this::aceptarConexiones, "Parchis-AceptadorTCP");
        aceptador.setDaemon(true);
        aceptador.start();
    }

    public synchronized void detener() {
        activo = false;
        ServerSocket servidor = serverSocket;
        serverSocket = null;
        if (servidor != null) {
            try {
                servidor.close();
            } catch (IOException e) {
                alFallar.accept(e);
            }
        }
    }

    private void aceptarConexiones() {
        while (activo) {
            try {
                Socket socket = serverSocket.accept();
                alConectar.accept(socket);
            } catch (SocketException e) {
                if (activo) {
                    alFallar.accept(e);
                }
                return;
            } catch (IOException e) {
                if (activo) {
                    alFallar.accept(e);
                }
            }
        }
    }
}
