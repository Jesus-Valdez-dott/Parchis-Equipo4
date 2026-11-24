package Infraestructura;

import java.io.EOFException;
import java.io.IOException;

/**
 * Receives and dispatches messages from a connected peer.
 */
public final class ManejadorConexion implements Runnable {

    private final ConexionPeer conexion;

    public ManejadorConexion(ConexionPeer conexion) {
        this.conexion = conexion;
    }

    @Override
    public void run() {
        while (conexion.isActiva()) {
            try {
                conexion.entregar((MensajeRed) conexion.recibir());
            } catch (EOFException e) {
                conexion.notificarDesconexion("El peer cerró la conexión");
            } catch (IOException e) {
                if (conexion.isActiva()) {
                    conexion.notificarDesconexion("Se perdió la conexión TCP: " + e.getMessage());
                }
            } catch (ClassNotFoundException e) {
                conexion.notificarDesconexion("Se recibió un mensaje de tipo desconocido");
            } catch (IllegalStateException e) {
                conexion.notificarDesconexion("Se rechazó un mensaje de red: " + e.getMessage());
            }
        }
    }
}
