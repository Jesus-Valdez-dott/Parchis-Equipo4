package Infraestructura;

import Aplicacion.EstadoPartidaDTO;
import java.io.ObjectInputFilter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordina conexiones TCP reales entre un Host y sus clientes.
 */
public final class GestorRed implements IGestorRed {

    private static final int MAX_CLIENTES = 3;
    // Solo se aceptan mensajes de red y los tipos de datos que contienen.
    private static final Set<String> CLASES_SERIALIZABLES = Set.of(
            MensajeRed.class.getName(),
            Solicitud.class.getName(),
            ConfirmacionAccion.class.getName(),
            RechazoAccion.class.getName(),
            MensajeSolicitudUnion.class.getName(),
            MensajeRespuestaUnion.class.getName(),
            EstadoPartidaDTO.class.getName(),
            String.class.getName(),
            Integer.class.getName(),
            Number.class.getName(),
            ArrayList.class.getName(),
            LinkedHashMap.class.getName(),
            HashMap.class.getName(),
            Map.Entry.class.getName(),
            "java.util.Collections$UnmodifiableCollection",
            "java.util.Collections$UnmodifiableList",
            Collections.unmodifiableList(new ArrayList<>()).getClass().getName(),
            Collections.unmodifiableMap(new LinkedHashMap<>()).getClass().getName()
    );

    private final ClienteTCP cliente = new ClienteTCP();
    // En el Host, cada conexión se identifica por su dirección hasta registrarse.
    private final Map<String, ConexionPeer> conexionesClientes = new ConcurrentHashMap<>();
    // Permite responder a cada solicitud por el mismo socket que la originó.
    private final Map<String, ConexionPeer> solicitantes = new ConcurrentHashMap<>();
    private volatile ServidorTCP servidor;
    private volatile ConexionPeer conexionHost;
    private volatile RolPeer rol;
    private volatile INotificadorRed notificador;
    private volatile boolean cerrado;

    @Override
    public void setNotificador(INotificadorRed notificador) {
        this.notificador = notificador;
    }

    @Override
    public synchronized void iniciarComoHost(int puerto) throws IOException {
        if (servidor != null) {
            throw new IllegalStateException("El Host TCP ya está iniciado");
        }
        cerrado = false;
        rol = RolPeer.HOST;
        servidor = new ServidorTCP(this::registrarCliente, this::errorServidor);
        servidor.iniciar(puerto);
    }

    @Override
    public void conectarAHost(String ip, int puerto) throws IOException {
        if (conexionHost != null) {
            throw new IllegalStateException("Este peer ya está conectado a un Host");
        }
        cerrado = false;
        rol = RolPeer.CLIENTE;
        ConexionPeer conexion = new ConexionPeer(cliente.conectar(ip, puerto));
        conexionHost = conexion;
        configurarConexion(conexion);
        conexion.iniciarRecepcion();
        notificarConexionActualizada();
    }

    @Override
    public void solicitarUnion(String idPartida, String idJugador) {
        if (rol != RolPeer.CLIENTE || conexionHost == null) {
            throw new IllegalStateException("Primero hay que conectarse al Host");
        }
        conexionHost.enviar(new MensajeSolicitudUnion(idPartida, idJugador));
    }

    @Override
    public void enviarRespuestaUnion(MensajeRespuestaUnion mensaje) {
        ConexionPeer conexion = solicitantes.remove(mensaje.getIdMensaje());
        if (conexion == null) {
            throw new IllegalStateException("No se encontró el peer que solicitó unirse");
        }
        if (mensaje.isAceptada()) {
            String idJugador = conexion.getIdJugadorPendiente();
            if (idJugador == null || conexionesClientes.containsKey(idJugador)) {
                throw new IllegalStateException("No se puede registrar la identidad del peer");
            }
            // Sustituye la clave temporal de la conexión por la identidad registrada.
            conexionesClientes.remove(conexion.getIdPeer());
            conexion.setIdJugador(idJugador);
            conexion.setIdPeer(idJugador);
            conexionesClientes.put(idJugador, conexion);
        }
        conexion.setIdJugadorPendiente(null);
        conexion.enviar(mensaje);
    }

    @Override
    public void transmitirEvento(MensajeRed mensaje) {
        if (rol == RolPeer.CLIENTE) {
            ConexionPeer host = conexionHost;
            if (host == null) {
                throw new IllegalStateException("No hay conexión con el Host");
            }
            host.enviar(mensaje);
            return;
        }
        if (rol != RolPeer.HOST) {
            throw new IllegalStateException("El rol del peer no está inicializado");
        }
        if (mensaje instanceof RechazoAccion rechazo) {
            ConexionPeer destino = solicitantes.remove(rechazo.getIdSolicitud());
            if (destino == null) {
                throw new IllegalStateException("No se encontró el peer que solicitó la acción");
            }
            // Los rechazos son privados; las confirmaciones de estado se difunden.
            destino.enviar(mensaje);
            return;
        }
        for (ConexionPeer conexion : Set.copyOf(conexionesClientes.values())) {
            if (conexion.isActiva()) {
                conexion.enviar(mensaje);
            }
        }
        if (mensaje instanceof ConfirmacionAccion) {
            solicitantes.clear();
        }
    }

    @Override
    public synchronized void cerrarComunicacion() {
        cerrado = true;
        ServidorTCP servidorActual = servidor;
        servidor = null;
        if (servidorActual != null) {
            servidorActual.detener();
        }
        for (ConexionPeer conexion : Set.copyOf(conexionesClientes.values())) {
            conexion.cerrar();
        }
        conexionesClientes.clear();
        ConexionPeer host = conexionHost;
        conexionHost = null;
        if (host != null) {
            host.cerrar();
        }
        solicitantes.clear();
        rol = null;
    }

    public void procesarMensaje(MensajeRed mensaje) {
        INotificadorRed receptor = notificador;
        if (receptor == null) {
            throw new IllegalStateException("No se ha configurado el receptor de mensajes");
        }
        receptor.mensajeRecibido(mensaje);
    }

    public void registrarCliente(ConexionPeer conexion) {
        if (conexionesClientes.size() >= MAX_CLIENTES + 1) {
            conexion.cerrar();
            return;
        }
        conexionesClientes.put(conexion.getIdPeer(), conexion);
        configurarConexion(conexion);
        conexion.iniciarRecepcion();
        notificarConexionActualizada();
    }

    public void eliminarPeer(String idPeer) {
        ConexionPeer conexion = conexionesClientes.remove(idPeer);
        if (conexion == null) {
            return;
        }
        solicitantes.values().removeIf(peer -> peer == conexion);
        conexion.cerrar();
        notificarConexionActualizada();
    }

    private void registrarCliente(Socket socket) {
        try {
            registrarCliente(new ConexionPeer(socket));
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException cierre) {
                e.addSuppressed(cierre);
            }
            errorServidor(e);
        }
    }

    private void configurarConexion(ConexionPeer conexion) {
        conexion.configurarRecepcion(
                mensaje -> procesarMensaje(conexion, mensaje),
                motivo -> conexionPerdida(conexion, motivo));
    }

    private void procesarMensaje(ConexionPeer origen, MensajeRed mensaje) {
        INotificadorRed receptor = notificador;
        if (receptor == null) {
            throw new IllegalStateException("No se ha configurado el receptor de mensajes");
        }
        if (rol == RolPeer.HOST) {
            procesarMensajeCliente(origen, mensaje, receptor);
        } else if (mensaje instanceof ConfirmacionAccion
                || mensaje instanceof RechazoAccion
                || mensaje instanceof MensajeRespuestaUnion) {
            receptor.mensajeRecibido(mensaje);
        } else {
            throw new IllegalStateException("El Host envió un tipo de mensaje no permitido");
        }
    }

    private void procesarMensajeCliente(ConexionPeer origen, MensajeRed mensaje,
            INotificadorRed receptor) {
        if (mensaje instanceof MensajeSolicitudUnion solicitud) {
            if (origen.getIdJugador() != null) {
                solicitantes.put(solicitud.getIdMensaje(), origen);
                enviarRespuestaUnion(new MensajeRespuestaUnion("HOST",
                        solicitud.getIdMensaje(), false, "Este peer ya está registrado en la sala"));
                return;
            }
            origen.setIdJugadorPendiente(solicitud.getIdJugador());
            solicitantes.put(solicitud.getIdMensaje(), origen);
            long jugadoresConectados = conexionesClientes.values().stream()
                    .filter(conexion -> conexion.isActiva() && conexion.getIdJugador() != null)
                    .count();
            if (jugadoresConectados >= MAX_CLIENTES) {
                enviarRespuestaUnion(new MensajeRespuestaUnion("HOST",
                        solicitud.getIdMensaje(), false, "La sala está llena"));
                eliminarPeer(origen.getIdPeer());
                return;
            }
            receptor.mensajeRecibido(solicitud);
        } else if (mensaje instanceof Solicitud solicitud) {
            if (origen.getIdJugador() == null
                    || !origen.getIdJugador().equals(solicitud.getIdJugador())) {
                solicitantes.put(solicitud.getIdSolicitud(), origen);
                transmitirEvento(new RechazoAccion("HOST", solicitud.getIdSolicitud(),
                        "La solicitud no pertenece al jugador conectado"));
            } else {
                solicitantes.put(solicitud.getIdSolicitud(), origen);
                receptor.mensajeRecibido(solicitud);
            }
        } else {
            throw new IllegalStateException("El cliente envió un tipo de mensaje no permitido");
        }
    }

    private void conexionPerdida(ConexionPeer conexion, String motivo) {
        conexionesClientes.remove(conexion.getIdPeer(), conexion);
        if (conexion == conexionHost) {
            conexionHost = null;
            if (!cerrado) {
                notificarDesconexion(motivo);
            }
        } else {
            solicitantes.values().removeIf(peer -> peer == conexion);
            notificarConexionActualizada();
        }
    }

    private void errorServidor(IOException error) {
        if (!cerrado) {
            notificarDesconexion("Error del servidor TCP: " + error.getMessage());
        }
    }

    private void notificarConexionActualizada() {
        INotificadorRed receptor = notificador;
        if (receptor != null) {
            receptor.conexionActualizada();
        }
    }

    private void notificarDesconexion(String motivo) {
        INotificadorRed receptor = notificador;
        if (receptor != null) {
            receptor.conexionPerdida(motivo);
        }
    }

    static ObjectInputFilter.Status filtrarObjeto(ObjectInputFilter.FilterInfo info) {
        // Rechaza grafos demasiado grandes antes de inspeccionar sus clases.
        if (info.depth() > 20 || info.references() > 10000
                || info.streamBytes() > 64_000_000 || info.arrayLength() > 10000) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> tipo = info.serialClass();
        if (tipo == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        if (tipo.isArray()) {
            tipo = tipo.getComponentType();
        }
        return CLASES_SERIALIZABLES.contains(tipo.getName())
                ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
    }
}
