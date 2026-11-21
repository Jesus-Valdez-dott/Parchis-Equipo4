package Dominio;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Estado del turno en curso.
 *
 * La tirada del dado y los bonos (20 por captura, 10 por meta) se manejan igual:
 * son AccionPendiente en una cola. La acción en la cabeza es la que se está
 * jugando; consumirAccion() la retira (diagrama 06 y 11).
 *
 * Fases: ESPERANDO_LANZAMIENTO -> RESOLVIENDO -> ESPERANDO_MOVIMIENTO -> ... -> FINALIZADO
 *
 * @author Jesús
 */
public class Turno {

    public static final String ESPERANDO_LANZAMIENTO = "ESPERANDO_LANZAMIENTO";
    public static final String RESOLVIENDO = "RESOLVIENDO";
    public static final String ESPERANDO_MOVIMIENTO = "ESPERANDO_MOVIMIENTO";
    public static final String FINALIZADO = "FINALIZADO";

    // Valores que devuelve resolverContinuacion() (diagrama 12)
    public static final String MOVER = "MOVER";
    public static final String REPETIR = "REPETIR";
    public static final String CAMBIAR = "CAMBIAR";

    private int valorDado;
    private int contadorSeisesConsecutivos;
    private int bonificacionPendiente;
    private String ultimaFichaMovida;
    private final Queue<AccionPendiente> accionesPendientes;
    private String fase;
    private boolean repeticionPendiente;

    public Turno() {
        this.valorDado = 0;
        this.contadorSeisesConsecutivos = 0;
        this.bonificacionPendiente = 0;
        this.ultimaFichaMovida = null;
        this.accionesPendientes = new LinkedList<>();
        this.fase = ESPERANDO_LANZAMIENTO;
        this.repeticionPendiente = false;
    }

    public void registrarTirada(int valor) {
        this.valorDado = valor;
        if (valor == 6) {
            this.contadorSeisesConsecutivos++;
            this.repeticionPendiente = true;
        } else {
            this.contadorSeisesConsecutivos = 0;
            this.repeticionPendiente = false;
        }
        this.accionesPendientes.add(new AccionPendiente(AccionPendiente.DADO, valor, null, null));
        this.fase = RESOLVIENDO;
    }

    public boolean esTercerSeis() {
        return this.contadorSeisesConsecutivos >= 3;
    }

    public String obtenerUltimaFicha() {
        return this.ultimaFichaMovida;
    }

    /** Acción que se está jugando (cabeza de la cola) o null si no hay. */
    public AccionPendiente obtenerAccionActual() {
        return this.accionesPendientes.peek();
    }

    /** Retira la acción actual; si queda otra en cola, el turno vuelve a resolver. */
    public void consumirAccion() {
        this.accionesPendientes.poll();
        AccionPendiente siguiente = this.accionesPendientes.peek();
        this.bonificacionPendiente = (siguiente != null && !AccionPendiente.DADO.equals(siguiente.getTipo()))
                ? siguiente.getPasos() : 0;
        this.fase = RESOLVIENDO;
    }

    public void registrarMovimiento(String idFicha) {
        this.ultimaFichaMovida = idFicha;
    }

    public void agregarBonoCaptura(String idFicha) {
        // Avanzar 20 casillas con la ficha que capturó
        this.accionesPendientes.add(new AccionPendiente(AccionPendiente.BONO_CAPTURA, 20, idFicha, null));
    }

    public void agregarBonoMeta(String idFicha) {
        // Avanzar 10 casillas con cualquier OTRA ficha
        this.accionesPendientes.add(new AccionPendiente(AccionPendiente.BONO_META, 10, null, idFicha));
    }

    public void cancelarTurno() {
        this.accionesPendientes.clear();
        this.bonificacionPendiente = 0;
        this.fase = FINALIZADO;
        this.repeticionPendiente = false;
    }

    public boolean hayAccionPendiente() {
        return !this.accionesPendientes.isEmpty();
    }

    public String resolverContinuacion() {
        if (!this.accionesPendientes.isEmpty()) {
            return MOVER;
        }
        if (this.repeticionPendiente && !esTercerSeis()) {
            return REPETIR;
        }
        return CAMBIAR;
    }

    public void habilitarLanzamiento() {
        // Si es un turno nuevo (no repetición por 6) se limpia el historial
        if (!this.repeticionPendiente) {
            this.contadorSeisesConsecutivos = 0;
            this.ultimaFichaMovida = null;
        }
        this.repeticionPendiente = false;
        this.fase = ESPERANDO_LANZAMIENTO;
        // valorDado NO se borra para que la vista siga mostrando la última tirada
    }

    public void esperarMovimiento() {
        this.fase = ESPERANDO_MOVIMIENTO;
    }

    public int getValorDado() { return this.valorDado; }
    public String getFase() { return this.fase; }
    public int getBonificacionPendiente() { return this.bonificacionPendiente; }
}
