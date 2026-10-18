/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jesús
 */
class Casilla {
    private int id;
    private boolean segura;
    private List<Ficha> fichasOcupantes;

    public Casilla(int id, boolean segura) {
        this.id = id;
        this.segura = segura;
        this.fichasOcupantes = new ArrayList<>();
    }

    public boolean isSegura() {
        return segura;
    }

    public List<Ficha> getFichasOcupantes() {
        return fichasOcupantes;
    }

    public void agregarFicha(Ficha ficha) {
        fichasOcupantes.add(ficha);
    }

    public void quitarFicha(Ficha ficha) {
        fichasOcupantes.remove(ficha);
    }

    // Regla 7: Si hay 2 o más fichas del MISMO color, es barrera
    public boolean esBarrera() {
        if (fichasOcupantes.size() >= 2) {
             // Verificamos que al menos 2 sean del mismo color. 
             // Como las fichas tienen el formato "IDJugador_F1", comparamos los prefijos
             String idJugador1 = fichasOcupantes.get(0).getIdFicha().split("_")[0];
             String idJugador2 = fichasOcupantes.get(1).getIdFicha().split("_")[0];
             return idJugador1.equals(idJugador2);
        }
        return false;
    }
}
