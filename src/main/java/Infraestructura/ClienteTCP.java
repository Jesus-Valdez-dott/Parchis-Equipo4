package Infraestructura;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Opens a TCP connection to the Host.
 */
public final class ClienteTCP {

    private static final int TIMEOUT_CONEXION_MS = 5000;

    public Socket conectar(String ip, int puerto) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, puerto), TIMEOUT_CONEXION_MS);
        return socket;
    }
}
