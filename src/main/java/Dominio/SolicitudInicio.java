package Dominio;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Solicitud para iniciar la partida (CU Iniciar partida, requerimiento 5):
 * un jugador la pide y todos los demás inscritos deben confirmarla.
 * No se puede rechazar; solo se espera a que todos confirmen.
 *
 * @author Jesús
 */
public class SolicitudInicio {

    private final String idSolicitante;
    private final Set<String> confirmaciones = new LinkedHashSet<>();

    /** El que pide iniciar ya está de acuerdo. */
    public SolicitudInicio(String idSolicitante) {
        this.idSolicitante = idSolicitante;
        this.confirmaciones.add(idSolicitante);
    }

    public void registrarConfirmacion(String idJugador) {
        confirmaciones.add(idJugador);
    }

    public boolean haConfirmado(String idJugador) {
        return confirmaciones.contains(idJugador);
    }

    /** true si todos los inscritos ya confirmaron. */
    public boolean todosConfirmaron(List<String> idsInscritos) {
        return confirmaciones.containsAll(idsInscritos);
    }

    /** Cuando se inscribe alguien más: solo queda confirmado el solicitante. */
    public void reiniciarConfirmaciones() {
        confirmaciones.clear();
        confirmaciones.add(idSolicitante);
    }

    public String getIdSolicitante() {
        return idSolicitante;
    }

    public List<String> getConfirmaciones() {
        return new ArrayList<>(confirmaciones);
    }
}
