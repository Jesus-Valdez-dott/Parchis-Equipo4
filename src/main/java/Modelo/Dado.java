/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.Random;

/**
 *
 * @author Jesús
 */
public class Dado {
    private Random random;

    public Dado() {
        this.random = new Random();
    }

    public int lanzar() {
        // Genera un número aleatorio entre 1 y 6
        return random.nextInt(6) + 1;
    }
}
