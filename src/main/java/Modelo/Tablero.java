/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jesús
 */
public class Tablero {
    private Map<ColorJugador, Integer> casillasDeSalida;
    
    private List<Casilla> casillas; 
    
    public Tablero() {
        this.casillas = new ArrayList<>();
        this.casillasDeSalida = new HashMap<>(); // NUEVA LÍNEA
        inicializarTablero();
    }

    private void inicializarTablero() {
        // Aquí iría un bucle para inicializar las casillas (del 0 al 67 para las perimetrales, etc.)
        // Asignando cuáles son seguras según las reglas o configuración
        for(int i = 0; i < 68; i++){
            // Ejemplo: marcamos las casillas seguras (Requerimiento 1 dice que pueden ser configurables, 
            // pero para esta prueba asumimos que las múltiplo de 8 son seguras por poner un ejemplo)
            boolean segura = (i % 8 == 0); 
            casillas.add(new Casilla(i, segura));
        }
    }

    public boolean hayBarreraEnCamino(int inicio, int destino) {
        // Aseguramos de iterar en el sentido correcto, contemplando que el tablero es circular
        if(inicio == -1) return false; // Si está fuera del tablero no hay barrera

        int actual = inicio + 1; // Revisamos desde la siguiente casilla a la actual
        
        while (actual <= destino) {
            // Ajustamos el índice si da la vuelta al tablero perimetral (ej. pasa de 67 a 0)
            int indiceReal = actual % 68; // 68 es un ejemplo del tamaño perimetral
            
            Casilla casilla = casillas.get(indiceReal);
            if (casilla.esBarrera()) {
                return true; // Encontramos una barrera, no puede pasar
            }
            actual++;
        }
        return false;
    }

    public boolean esCasillaSegura(int idCasilla) {
         if (idCasilla >= 0 && idCasilla < casillas.size()) {
             return casillas.get(idCasilla).isSegura();
         }
         return false;
    }

    public List<Ficha> obtenerFichasEnCasilla(int idCasilla) {
         if (idCasilla >= 0 && idCasilla < casillas.size()) {
             return casillas.get(idCasilla).getFichasOcupantes();
         }
         return new ArrayList<>();
    }

    public boolean validarMovimiento(String idFicha, int pasos) {
        // En tu diseño final, este método podría delegar a ReglasMovimiento 
        // o usar hayBarreraEnCamino para responder.
        return true; 
    }

    public void moverFicha(Ficha ficha, int destino) {
        int posicionActual = ficha.getPosicion();
        
        // 1. Quitar la ficha de su casilla actual
        if (posicionActual != -1) {
            casillas.get(posicionActual).quitarFicha(ficha);
        }
        
        // 2. Actualizar el estado interno de la Ficha
        ficha.actualizarPosicion(destino);
        
        // 3. Colocar la ficha en su nueva casilla
        casillas.get(destino).agregarFicha(ficha);
        
        System.out.println("Ficha " + ficha.getIdFicha() + " se movió de " + posicionActual + " a " + destino);
    }

    public void sacarFicha(Ficha ficha) {
        // Regla 3: Se necesita sacar un 5 para colocar la ficha en la salida
        // Asumiremos que el método calcularSalidaPorColor() determina el índice de la casilla
        // correspondiente al color de la ficha.
        int casillaSalida = calcularSalidaPorColor(ficha); 
        
        // La ficha deja de estar "EN_BASE" y pasa a "ACTIVA"
        ficha.activar(casillaSalida);
        
        // Agregamos la ficha físicamente a la casilla del tablero
        casillas.get(casillaSalida).agregarFicha(ficha);
        
        System.out.println("Ficha " + ficha.getIdFicha() + " ha salido a la casilla " + casillaSalida);
    }

    public void capturarFicha(Ficha fichaCapturada) {
        // Regla 5: Si una ficha cae en una casilla ocupada por una ficha rival, la rival vuelve a su salida[cite: 2]
        int posicionActual = fichaCapturada.getPosicion();
        
        // 1. Quitar la ficha capturada del tablero
        if (posicionActual != -1) {
            casillas.get(posicionActual).quitarFicha(fichaCapturada);
        }
        
        // 2. Cambiar su estado interno a "EN_BASE"
        fichaCapturada.regresarABase();
        
        System.out.println("¡Ficha " + fichaCapturada.getIdFicha() + " ha sido capturada y regresada a la base!");
    }

    public void llevarAMeta(Ficha ficha) {
        int posicionActual = ficha.getPosicion();
        
        // 1. Quitar la ficha de su casilla actual
        if (posicionActual != -1) {
            casillas.get(posicionActual).quitarFicha(ficha);
        }
        
        // 2. Cambiar su estado a "EN_META"
        ficha.llegarAMeta();
        
        // Nota: Las fichas en meta ya no ocupan una Casilla en el arreglo principal.
        System.out.println("¡Ficha " + ficha.getIdFicha() + " ha llegado a la meta!");
    }

    public void regresarUltimaFicha(Ficha ficha) {
        // Regla 4: Penalización por sacar tres 6 seguidos. La última ficha movida vuelve al inicio[cite: 2].
        int posicionActual = ficha.getPosicion();
        
        if (posicionActual != -1) {
            casillas.get(posicionActual).quitarFicha(ficha);
        }
        
        ficha.regresarABase();
        System.out.println("Penalización (tres 6): Ficha " + ficha.getIdFicha() + " regresada a la base.");
    }

    public List<String> obtenerFichasEnBarrera(String idJugador) {
        List<String> fichasEnBarrera = new ArrayList<>();
        // Recorremos todas las casillas
        for (Casilla c : casillas) {
            if (c.esBarrera()) {
                // Si la barrera es de este jugador, agregamos los IDs de las fichas
                for(Ficha f : c.getFichasOcupantes()){
                    if(f.getIdFicha().startsWith(idJugador)){ // Asumiendo que el ID de ficha empieza con el del jugador
                        fichasEnBarrera.add(f.getIdFicha());
                    }
                }
            }
        }
        return fichasEnBarrera; 
    }
    
    public void configurarSalidas(Map<ColorJugador, Integer> configuracion) {
        this.casillasDeSalida = configuracion;
    }
    
    private int calcularSalidaPorColor(Ficha ficha) {
        // Asume que a tu clase Ficha le agregarás un método getColor() 
        // o que su ID está ligado a un ColorJugador específico.
        // Si el color no está configurado por alguna razón, devuelve 0 por defecto.
        return casillasDeSalida.getOrDefault(ficha.getColor(), 0); 
    }
}
