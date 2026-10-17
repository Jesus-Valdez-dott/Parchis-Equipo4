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
public class ReglasMovimiento {
    // Suponiendo un tablero genérico de 68 casillas por vuelta antes del pasillo de meta
    private static final int LONGITUD_CARRIL_META = 8; 

    public ReglasMovimiento() {
    }

    public List<String> obtenerFichasMovibles(Juego juego) {
        List<String> legales = obtenerMovimientosLegales(juego);
        // Regla 7: Si hay barrera, es obligatorio mover la ficha que la forma
        return filtrarPrioridadBarrera(juego, legales); 
    }

    public boolean esMovimientoValido(Juego juego, String idFicha) {
        // Aquí se valida si el camino está libre de barreras enemigas o propias en el trayecto
        // Una barrera bloquea el camino por completo para cualquier ficha
        return true; 
    }

    public int calcularDestino(Juego juego, String idFicha) {
        int valorDado = juego.obtenerValorDado();
        // Lógica base: sumar el dado a la posición actual de la ficha
        int posicionActual = 0; // Se obtendría de la ficha
        int destinoTeorico = posicionActual + valorDado;
        
        // Regla 8: Rebote en la meta
        int casillasFaltantes = 0; // Se calcularía en base a la posición de meta del color
        if (valorDado > casillasFaltantes) {
            int sobrante = valorDado - casillasFaltantes;
            destinoTeorico = posicionActual + casillasFaltantes - sobrante; // Lógica de rebote hacia atrás
        }
        
        return destinoTeorico;
    }

    public String obtenerFichaCapturable(Juego juego, String idFicha, int destino) {
        // Regla 5 y 6: Retorna el ID de la ficha rival si la casilla destino está ocupada por una sola ficha enemiga 
        // y si no es una casilla segura.
        boolean esCasillaSegura = false; // Se consultaría al Tablero
        if (esCasillaSegura) {
            return null; // No se puede capturar en casilla segura
        }
        return "FichaRival_ID"; // Retornaría el ID si aplica
    }

    public List<String> obtenerMovimientosLegales(Juego juego) {
        List<String> legales = new ArrayList<>();
        int valorDado = juego.obtenerValorDado();
        Jugador jugador = juego.obtenerJugadorActual();
        
        for (Ficha f : jugador.obtenerFichas()) {
            if ("EN_BASE".equals(f.getEstado())) {
                // Regla 3: Se necesita sacar un 5 (o el valor configurado) para salir
                if (esSalida(juego, f.getIdFicha())) {
                    legales.add(f.getIdFicha());
                }
            } else if ("ACTIVA".equals(f.getEstado())) {
                if (esMovimientoValido(juego, f.getIdFicha())) {
                    legales.add(f.getIdFicha());
                }
            }
        }
        return legales;
    }

    public List<String> filtrarPrioridadBarrera(Juego juego, List<String> legales) {
        // Regla 7: Si un jugador tiene una barrera en su turno, está obligado a mover una de las fichas de la barrera.
        List<String> fichasEnBarrera = juego.getTablero().obtenerFichasEnBarrera(juego.obtenerJugadorActual().getIdJugador());
        
        if (!fichasEnBarrera.isEmpty()) {
            List<String> legalesConPrioridad = new ArrayList<>();
            for (String id : legales) {
                if (fichasEnBarrera.contains(id)) {
                    legalesConPrioridad.add(id);
                }
            }
            return !legalesConPrioridad.isEmpty() ? legalesConPrioridad : legales;
        }
        return legales;
    }

    public boolean esSalida(Juego juego, String idFicha) {
        // Regla 1 y 3: Evalúa si el dado cumple con el requisito para sacar una ficha de la base
        // Por defecto es 5, pero esto es configurable al inicio.
        int valorParaSalir = 5; // En una versión final, este valor se saca de las 'opciones' de Juego
        return juego.obtenerValorDado() == valorParaSalir;
    }

    public boolean esLlegadaExacta(Juego juego, String idFicha, int destino) {
        // Regla 8: Verifica si el número del dado permite llegar exactamente al triángulo central
        return true; // Implementación matemática dependiente de los índices de la meta
    }
}
