/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Jesús
 */
public class AccionPendiente {
    private String tipo;
    private int pasos;
    private String fichaObligatoria;
    private String fichaExcluida;

    public AccionPendiente(String tipo, int pasos, String fichaObligatoria, String fichaExcluida) {
        this.tipo = tipo;
        this.pasos = pasos;
        this.fichaObligatoria = fichaObligatoria;
        this.fichaExcluida = fichaExcluida;
    }

    // Getters
    public String getTipo() {
        return tipo;
    }

    public int getPasos() {
        return pasos;
    }

    public String getFichaObligatoria() {
        return fichaObligatoria;
    }

    public String getFichaExcluida() {
        return fichaExcluida;
    }

    // Setters
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setPasos(int pasos) {
        this.pasos = pasos;
    }

    public void setFichaObligatoria(String fichaObligatoria) {
        this.fichaObligatoria = fichaObligatoria;
    }

    public void setFichaExcluida(String fichaExcluida) {
        this.fichaExcluida = fichaExcluida;
    }
}
