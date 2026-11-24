package Dominio;

import java.util.ArrayList;
import java.util.List;

/**
 * Casilla del perímetro del tablero.
 *
 * @author Jesús
 */
class Casilla {

    /** En Parchís una casilla admite como máximo 2 fichas. */
    static final int CAPACIDAD = 2;

    private final int id;
    private final boolean segura;
    private final List<Ficha> fichasOcupantes;

    public Casilla(int id, boolean segura) {
        this.id = id;
        this.segura = segura;
        this.fichasOcupantes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public boolean isSegura() {
        return segura;
    }

    public List<Ficha> getFichasOcupantes() {
        return fichasOcupantes;
    }

    public void agregarFicha(Ficha ficha) {
        if (!fichasOcupantes.contains(ficha)) {
            fichasOcupantes.add(ficha);
        }
    }

    public void quitarFicha(Ficha ficha) {
        fichasOcupantes.remove(ficha);
    }

    public boolean estaLlena() {
        return fichasOcupantes.size() >= CAPACIDAD;
    }

    /** Dos fichas del mismo color en una casilla forman una barrera. */
    public boolean esBarrera() {
        return fichasOcupantes.size() >= 2
                && fichasOcupantes.get(0).getColor() == fichasOcupantes.get(1).getColor();
    }
}
