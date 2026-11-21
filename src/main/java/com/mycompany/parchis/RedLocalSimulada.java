package com.mycompany.parchis;

import Infraestructura.ConfirmacionAccion;
import Infraestructura.IGestorRed;
import Infraestructura.INotificadorRed;
import Infraestructura.MensajeRed;
import Infraestructura.RechazoAccion;
import Infraestructura.Solicitud;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TEMPORAL: sustituto de GestorRed SIN sockets, solo para probar el juego en
 * un mismo programa (modo demo) mientras se termina el GestorRed real.
 *
 * Cumple el contrato de IGestorRed igual que lo haría la red:
 * - topología en estrella (los peers solo hablan con el Host),
 * - entrega asíncrona y en orden (un hilo por peer),
 * - los mensajes se serializan y deserializan, como si viajaran por TCP.
 *
 * Cuando exista Infraestructura.GestorRed, se cambia una línea en
 * Parchis.crearRed() y esta clase se puede borrar.
 *
 * @author Jesús
 */
public class RedLocalSimulada implements IGestorRed {

    private static final int MAX_PEERS_REMOTOS = 3;
    /** "Puertos" abiertos dentro de este programa: puerto -> Host. */
    private static final Map<Integer, RedLocalSimulada> HOSTS = new ConcurrentHashMap<>();

    private INotificadorRed notificador;
    private final ExecutorService buzon = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "RedLocalSimulada");
        t.setDaemon(true);
        return t;
    });

    // Si es Host
    private final List<RedLocalSimulada> peers = new CopyOnWriteArrayList<>();
    private final Map<String, RedLocalSimulada> solicitantes = new ConcurrentHashMap<>();
    private volatile MensajeRed ultimaDifusion;
    private Integer puerto;

    // Si es peer
    private volatile RedLocalSimulada host;

    @Override
    public void setNotificador(INotificadorRed notificador) {
        this.notificador = notificador;
    }

    @Override
    public void iniciarComoHost(int puerto) throws IOException {
        if (HOSTS.putIfAbsent(puerto, this) != null) {
            throw new IOException("El puerto " + puerto + " ya está en uso");
        }
        this.puerto = puerto;
    }

    @Override
    public void conectarAHost(String ip, int puerto) throws IOException {
        RedLocalSimulada h = HOSTS.get(puerto);
        if (h == null) {
            throw new IOException("No hay Host en el puerto " + puerto + " (la red simulada solo funciona en el mismo programa)");
        }
        if (!h.aceptarPeer(this)) {
            throw new IOException("La partida está llena");
        }
        this.host = h;
    }

    private synchronized boolean aceptarPeer(RedLocalSimulada peer) {
        if (peers.size() >= MAX_PEERS_REMOTOS) {
            return false;
        }
        peers.add(peer);
        MensajeRed estado = ultimaDifusion;
        if (estado != null) {
            peer.entregar(this, estado); // el que llega tarde recibe el estado actual
        }
        return true;
    }

    @Override
    public void enviar(MensajeRed mensaje) {
        RedLocalSimulada h = host;
        if (h != null) {
            h.entregar(this, mensaje);
        }
    }

    @Override
    public synchronized void difundir(MensajeRed mensaje) {
        ultimaDifusion = mensaje;
        for (RedLocalSimulada p : peers) {
            p.entregar(this, mensaje);
        }
    }

    @Override
    public void responderAlSolicitante(String idSolicitud, MensajeRed mensaje) {
        RedLocalSimulada p = solicitantes.remove(idSolicitud);
        if (p != null) {
            p.entregar(this, mensaje);
        }
    }

    @Override
    public void cerrar() {
        if (puerto != null) {
            HOSTS.remove(puerto, this);
        }
        RedLocalSimulada h = host;
        if (h != null) {
            h.peers.remove(this);
        }
        buzon.shutdownNow();
    }

    /** "Llega" un mensaje a este peer: se copia como si viajara por la red y se entrega en orden. */
    private void entregar(RedLocalSimulada origen, MensajeRed mensaje) {
        MensajeRed copia = copiar(mensaje);
        buzon.execute(() -> recibir(origen, copia));
    }

    private void recibir(RedLocalSimulada origen, MensajeRed mensaje) {
        if (notificador == null) {
            return;
        }
        if (mensaje instanceof Solicitud s) {
            solicitantes.put(s.getIdSolicitud(), origen);
            notificador.solicitudRecibida(s);
        } else if (mensaje instanceof ConfirmacionAccion c) {
            notificador.confirmacionRecibida(c);
        } else if (mensaje instanceof RechazoAccion r) {
            notificador.rechazoRecibido(r);
        }
    }

    private static MensajeRed copiar(MensajeRed mensaje) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
                out.writeObject(mensaje);
            }
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                return (MensajeRed) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("El mensaje no se puede mandar por red: " + e.getMessage(), e);
        }
    }
}
