package com.mycompany.parchis;

import Aplicacion.EstadoPartidaDTO;
import Aplicacion.GestorJuego;
import Infraestructura.GestorRed;
import Presentacion.ControladorJuego;
import Presentacion.ModeloPresentacion;
import Presentacion.PantallaPartida;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Herramienta de vista previa que arma una pantalla con estado estático.
 * No conecta a la red ni representa una partida jugable.
 *
 * @author Equipo 4
 */
final class PartidaDeEjemplo {

    private PartidaDeEjemplo() {
    }

    static PantallaPartida crear() {
        GestorJuego gestor = new GestorJuego("J3", new GestorRed(), null);
        ModeloPresentacion modelo = new ModeloPresentacion("J3");
        ControladorJuego controlador = new ControladorJuego(modelo, gestor);
        PantallaPartida pantalla = new PantallaPartida(controlador, modelo);
        modelo.agregarObservador(pantalla);

        Map<String, String> colores = new LinkedHashMap<>();
        colores.put("J1", "ROJO");
        colores.put("J2", "AZUL");
        colores.put("J3", "VERDE");
        colores.put("J4", "AMARILLO");
        Map<String, String> nombres = new LinkedHashMap<>();
        nombres.put("J1", "Devorador_de_galaxias");
        nombres.put("J2", "Patricio");
        nombres.put("J3", "Sabrosuras3000");
        nombres.put("J4", "Mr. Aura");
        Map<String, Integer> avatares = new LinkedHashMap<>();
        avatares.put("J1", 4);
        avatares.put("J2", 3);
        avatares.put("J3", 7);
        avatares.put("J4", 1);
        Map<String, Integer> posiciones = new LinkedHashMap<>();
        for (String j : colores.keySet()) {
            for (int f = 1; f <= 4; f++) {
                posiciones.put(j + "_F" + f, -1);
            }
        }
        posiciones.put("J3_F2", 56);   // salida verde
        posiciones.put("J1_F1", 44);
        posiciones.put("J2_F3", 27);

        modelo.reemplazarEstado(new EstadoPartidaDTO(1, "J3", List.of(), posiciones, colores,
                nombres, avatares, "EN_CURSO", null), 1);
        return pantalla;
    }
}
