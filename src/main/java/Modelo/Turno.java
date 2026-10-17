/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Jesús
 */
public class Turno {
    private int valorDado;
    private int contadorSeisesConsecutivos;
    private int bonificacionPendiente;
    private String ultimaFichaMovida;
    private Queue<AccionPendiente> accionesPendientes;
    private String fase;
    private boolean repeticionPendiente;

    public Turno() {
        this.valorDado = 0;
        this.contadorSeisesConsecutivos = 0;
        this.bonificacionPendiente = 0;
        this.ultimaFichaMovida = null;
        this.accionesPendientes = new LinkedList<>(); 
        this.fase = "ESPERANDO_LANZAMIENTO"; 
        this.repeticionPendiente = false;
    }

    public void registrarTirada(int valor) {
        this.valorDado = valor;
        if (valor == 6) {
            this.contadorSeisesConsecutivos++;
            this.repeticionPendiente = true;
        } else {
            this.contadorSeisesConsecutivos = 0;
            this.repeticionPendiente = false;
        }
        this.fase = "ESPERANDO_MOVIMIENTO";
    }

    public boolean esTercerSeis() {
        return this.contadorSeisesConsecutivos == 3;
    }

    public String obtenerUltimaFicha() {
        return this.ultimaFichaMovida;
    }

    public void consumirAccion() {
        if (!this.accionesPendientes.isEmpty()) {
            this.accionesPendientes.poll(); // Saca la acción de la cola
        }
    }

    public void registrarMovimiento(String idFicha, int destino) {
        this.ultimaFichaMovida = idFicha;
        // La lógica de movimiento actual (cambiar la posición) iría en Tablero, no aquí
        // Aquí solo registramos qué ficha se movió en este turno.
    }

    public void agregarBonoCaptura(String idFicha) {
        // Regla: avanzar 20 casillas tras capturar
        this.accionesPendientes.add(new AccionPendiente("BONO_CAPTURA", 20, idFicha, null));
    }

    public void agregarBonoMeta(String idFicha) {
        // Regla: recompensa de 10 casillas al llegar a meta
        this.accionesPendientes.add(new AccionPendiente("BONO_META", 10, null, idFicha)); // Puede ser cualquier OTRA ficha
    }

    public void cancelarTurno() {
        this.valorDado = 0;
        this.accionesPendientes.clear();
        this.fase = "FINALIZADO";
        this.repeticionPendiente = false;
    }

    public String resolverContinuacion() {
        if (!this.accionesPendientes.isEmpty()) {
            return "ACCION_PENDIENTE";
        } else if (this.repeticionPendiente && !esTercerSeis()) {
             return "REPETIR_TIRADA";
        }
        return "FIN_TURNO";
    }

    public void habilitaLanzamiento() {
         this.fase = "ESPERANDO_LANZAMIENTO";
         this.valorDado = 0;
    }

    public void esperarMovimiento() {
        this.fase = "ESPERANDO_MOVIMIENTO";
    }
    
    //Getters extra por si acaso
    public int getValorDado(){ return this.valorDado;}
    public String getFase(){ return this.fase;}
}
