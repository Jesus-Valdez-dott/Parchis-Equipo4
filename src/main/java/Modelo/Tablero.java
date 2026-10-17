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
public class Tablero {
    // List genérico, idealmente se debería crear una clase Casilla
    private List<Object> casillas; 

    public Tablero() {
        this.casillas = new ArrayList<>();
        // Aquí se inicializarían las casillas seguras, normales, etc.
    }

    public boolean validarMovimiento(String idFicha, int pasos) {
        // Lógica de reglas: rebotar en la meta, verificar barreras en el camino, etc.
        return true; 
    }

    public void moverFicha(String idFicha, int destino) {
        // Lógica para trasladar una ficha de su índice actual al destino
    }

    public void sacarFicha(String idFicha) {
        // Lógica para sacar la ficha del círculo de inicio
    }

    public void capturarFicha(String idFicha) {
        // Lógica donde la ficha rival regresa a su base y el jugador actual avanza 20 casillas
    }

    public void llevarAMeta(String idFicha) {
        // Lógica para ingresar a la recta final y llegar al triángulo
    }

    public void regresarUltimaFicha(String idFicha) {
        // Penalización: al sacar tres 6 seguidos, la última ficha vuelve a su inicio
    }

    public List<String> obtenerFichasEnBarrera(String idJugador) {
        // Logica para recorrer el tablero buscando casillas con 2 fichas del mismo color
        return new ArrayList<>(); 
    }
}
