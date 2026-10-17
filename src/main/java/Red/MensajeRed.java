/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Red;

/**
 *
 * @author Jesús
 */
public class MensajeRed {
    private String idJugadorEmisor;
    private String tipo;

    public MensajeRed(String idJugadorEmisor, String tipo) {
        this.idJugadorEmisor = idJugadorEmisor;
        this.tipo = tipo;
    }

    public String getIdJugadorEmisor() {
        return idJugadorEmisor;
    }

    public String getTipo() {
        return tipo;
    }
}
