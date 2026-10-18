/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Red;

/**
 *
 * @author Jesús
 */
public class MensajeDado extends MensajeRed{
    private int valorDado; // Nombre exacto del UML

    public MensajeDado(String idJugadorEmisor, int valorDado) {
        super(idJugadorEmisor, "LANZAMIENTO_DADO");
        this.valorDado = valorDado;
    }

    public int getValorDado() { return valorDado; }
}
