/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Jesús
 */
public class Ficha {
    private String idFicha;
    private int distanciaRecorrida;
    private String estado; // Se podría cambiar a un Enum
    private int posicion;

    public Ficha(String idFicha) {
        this.idFicha = idFicha;
        this.distanciaRecorrida = 0;
        this.estado = "EN_BASE";
        this.posicion = -1; // -1 puede representar que está fuera del tablero
    }

    public void activar(int salida) {
        this.estado = "ACTIVA";
        this.posicion = salida;
        this.distanciaRecorrida = 0;
    }

    public void actualizarPosicion(int destino) {
        // Logica para calcular la diferencia para sumarla a la distanciaRecorrida
        this.posicion = destino;
    }

    public void regresarABase() {
        this.estado = "EN_BASE";
        this.posicion = -1;
        this.distanciaRecorrida = 0;
    }

    public void llegarAMeta() {
        this.estado = "EN_META";
        // La posición final y lógica exacta de meta dependerá del array del tablero
    }

    // Getters
    public String getIdFicha() { return idFicha; }
    public int getDistanciaRecorrida() { return distanciaRecorrida; }
    public String getEstado() { return estado; }
    public int getPosicion() { return posicion; }
}
