/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Red;

/**
 *
 * @author Jesús
 */
public class MensajeMovimiento extends MensajeRed{
    private String idFicha;
    private int idCasillaDestino;

    public MensajeMovimiento(String idJugadorEmisor, String idFicha, int idCasillaDestino) {
        super(idJugadorEmisor, "MOVIMIENTO_FICHA");
        this.idFicha = idFicha;
        this.idCasillaDestino = idCasillaDestino;
    }

    public String getIdFicha() {
        return idFicha;
    }

    public int getIdCasillaDestino() {
        return idCasillaDestino;
    }
}
