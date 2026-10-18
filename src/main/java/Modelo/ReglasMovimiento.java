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
    
    // Distancia total que recorre una ficha desde su salida hasta el triángulo central
    private final int DISTANCIA_TOTAL_META = 76; // 68 normales + 8 del carril final

    public ReglasMovimiento() {
    }

    /*
    ORQUESTADOR DE MOVIMIENTOS LEGALES
    */

    public List<String> obtenerFichasMovibles(Juego juego) {
        // Obtenemos todas las fichas que pueden moverse
        List<String> legales = obtenerMovimientosLegales(juego);
        
        // Regla 7: Filtramos aplicando la obligación de romper barreras
        return filtrarPrioridadBarrera(juego, legales); 
    }

    public List<String> obtenerMovimientosLegales(Juego juego) {
        List<String> legales = new ArrayList<>();
        Jugador jugador = juego.obtenerJugadorActual();

        for (Ficha f : jugador.obtenerFichas()) {
            if ("EN_BASE".equals(f.getEstado())) {
                // Regla 3: Se necesita sacar el valor de salida (5 o el numero especificado) para colocar la ficha
                if (esSalida(juego, f.getIdFicha())) {
                    legales.add(f.getIdFicha());
                }
            } else if ("ACTIVA".equals(f.getEstado())) {
                // Verificamos que el camino no esté bloqueado
                if (esMovimientoValido(juego, f.getIdFicha())) {
                    legales.add(f.getIdFicha());
                }
            }
        }
        return legales;
    }

    /*
    VALIDACIONES Y CÁLCULOS MATEMÁTICOS
    */

    public boolean esSalida(Juego juego, String idFicha) {
        // Requerimiento 1: Por defecto el número para salir es 5
        int numeroSalida = 5; 

        // Si la lista de opciones existe, buscamos si se configuró un número distinto
        if (juego.getOpciones()!= null) {
            for (String opcion : juego.getOpciones()) {
                if (opcion.startsWith("SALIDA:")) {
                    numeroSalida = Integer.parseInt(opcion.split(":")[1]);
                    break;
                }
            }
        }

        // Comparamos el valor del dado con el número requerido
        return juego.obtenerValorDado() == numeroSalida;
    }

    public boolean esMovimientoValido(Juego juego, String idFicha) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        int inicio = ficha.getPosicion();
        int destino = calcularDestino(juego, idFicha);
        
        // Regla 7: Ninguna ficha puede pasar sobre una barrera
        // Tablero deberá iterar entre 'inicio' y 'destino' para asegurar que no hay barreras.
        return !juego.getTablero().hayBarreraEnCamino(inicio, destino); 
    }

    public int calcularDestino(Juego juego, String idFicha) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        int valorDado = juego.obtenerValorDado();
        int posicionActual = ficha.getPosicion();
        
        int destinoTeorico = posicionActual + valorDado;
        int distanciaRecorrida = ficha.getDistanciaRecorrida();
        int nuevaDistancia = distanciaRecorrida + valorDado;

        // Regla 8: Rebote en la meta si el número no es exacto[cite: 2]
        if (nuevaDistancia > DISTANCIA_TOTAL_META) {
            int casillasSobrantes = nuevaDistancia - DISTANCIA_TOTAL_META;
            // Se calcula el rebote retrocediendo las casillas sobrantes[cite: 2]
            destinoTeorico = posicionActual + (valorDado - casillasSobrantes) - casillasSobrantes; 
        }

        return destinoTeorico;
    }

    /*
    CAPTURAS Y BARRERAS
    */

    public String obtenerFichaCapturable(Juego juego, String idFichaActual, int destino) {
        Tablero tablero = juego.getTablero();
        
        // Regla 6: Si es casilla segura (marcada con círculo), no hay captura[cite: 2]
        if (tablero.esCasillaSegura(destino)) {
            return null;
        }

        List<Ficha> fichasEnDestino = tablero.obtenerFichasEnCasilla(destino);
        
        // Regla 5: Captura si hay exactamente 1 ficha en la casilla y es rival[cite: 2]
        if (fichasEnDestino.size() == 1) {
            Ficha fichaRival = fichasEnDestino.get(0);
            Jugador jugadorActual = juego.obtenerJugadorActual();
            
            // Verificamos que la ficha encontrada no pertenezca al jugador en turno
            if (!perteneceAlJugador(jugadorActual, fichaRival.getIdFicha())) {
                return fichaRival.getIdFicha();
            }
        }
        return null;
    }
    
    public boolean esLlegadaExacta(Juego juego, String idFicha, int destino) {
        Ficha ficha = obtenerFichaDelJugador(juego, idFicha);
        
        // Si la ficha no fue encontrada, no es un movimiento válido
        if (ficha == null) {
            return false;
        }

        int valorDado = juego.obtenerValorDado();
        int posicionActual = ficha.getPosicion();
        
        // Calculamos la distancia total que llevaría la ficha con este movimiento
        int distanciaRecorrida = ficha.getDistanciaRecorrida();
        int nuevaDistancia = distanciaRecorrida + valorDado;

        // Regla 8: Verifica si el número del dado permite llegar exactamente al triángulo central
        // Comparamos la distancia que alcanzaría con la distancia total requerida para llegar a la meta
        return nuevaDistancia == DISTANCIA_TOTAL_META; 
    }

    public List<String> filtrarPrioridadBarrera(Juego juego, List<String> legales) {
        Jugador actual = juego.obtenerJugadorActual();
        // Le preguntamos al tablero si el jugador tiene fichas formando barrera
        List<String> fichasEnBarrera = juego.getTablero().obtenerFichasEnBarrera(actual.getIdJugador());

        // Regla 7: Si un jugador tiene una barrera, está obligado a mover una ficha de ella
        if (!fichasEnBarrera.isEmpty()) {
            List<String> legalesObligatorios = new ArrayList<>();
            for (String id : legales) {
                if (fichasEnBarrera.contains(id)) {
                    legalesObligatorios.add(id);
                }
            }
            // Si al menos una ficha de la barrera se puede mover, forzamos esa lista
            if (!legalesObligatorios.isEmpty()) {
                return legalesObligatorios;
            }
        }
        return legales;
    }

    /*
    MÉTODOS AUXILIARES (Uso interno)
    */
    
    private Ficha obtenerFichaDelJugador(Juego juego, String idFicha) {
        for (Ficha f : juego.obtenerJugadorActual().obtenerFichas()) {
            if (f.getIdFicha().equals(idFicha)) return f;
        }
        return null;
    }

    private boolean perteneceAlJugador(Jugador jugador, String idFicha) {
        for (Ficha f : jugador.obtenerFichas()) {
            if (f.getIdFicha().equals(idFicha)) return true;
        }
        return false;
    }
}
