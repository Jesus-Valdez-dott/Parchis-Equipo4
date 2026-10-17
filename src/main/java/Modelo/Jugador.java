/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Jesús
 */
public class Jugador {
    private String idJugador;
    private String nombre;
    private String avatar;
    private ColorJugador color;
    private List<Ficha> fichas;

    public Jugador(String idJugador, String nombre, String avatar, ColorJugador color) {
        this.idJugador = idJugador;
        this.nombre = nombre;
        this.avatar = avatar;
        this.color = color;
        this.fichas = new ArrayList<>();
        
        // Inicializamos las 4 fichas requeridas para cada jugador
        for (int i = 1; i <= 4; i++) {
            this.fichas.add(new Ficha(idJugador + "_F" + i));
        }
    }

    public List<Ficha> obtenerFichas() {
        return this.fichas;
    }

    public int obtenerCantidadFichasEnMeta() {
        int contador = 0;
        for (Ficha ficha : fichas) {
            if ("EN_META".equals(ficha.getEstado())) {
                contador++;
            }
        }
        return contador;
    }

    public boolean tieneTodasLasFichasEnMeta() {
        // El jugador gana cuando sus 4 fichas llegan a la meta
        return obtenerCantidadFichasEnMeta() == 4; 
    }

    // Getters y Setters tradicionales
    public String getIdJugador() { return idJugador; }
    public String getNombre() { return nombre; }
    public String getAvatar() { return avatar; }
    public ColorJugador getColor() { return color; }
}
