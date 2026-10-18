/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Red;

import java.io.Serializable;

/**
 *
 * @author Jesús
 */
public abstract class MensajeRed implements Serializable{
    private String idJugadorEmisor;
    private String tipoMensaje; // Nombre exacto del UML

    public MensajeRed(String idJugadorEmisor, String tipoMensaje) {
        this.idJugadorEmisor = idJugadorEmisor;
        this.tipoMensaje = tipoMensaje;
    }

    public String getIdJugadorEmisor() { return idJugadorEmisor; }
    public String getTipoMensaje() { return tipoMensaje; }
}
