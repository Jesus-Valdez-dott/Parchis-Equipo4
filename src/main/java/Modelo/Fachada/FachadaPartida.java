/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo.Fachada;

import Modelo.Dado;
import Modelo.Ficha;
import Modelo.Juego;
import Modelo.Jugador;
import Modelo.ReglasMovimiento;
import Modelo.Turno;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jesús
 */
public class FachadaPartida implements IModeloLectura{
    private Juego juego;
    private ReglasMovimiento reglasMovimiento;
    private Dado dado;
    private List<IObservadorPartida> observadores;

    public FachadaPartida() {
        this.juego = new Juego("PARTIDA_PARCHIS_01");
        this.reglasMovimiento = new ReglasMovimiento();
        this.dado = new Dado();
        this.observadores = new ArrayList<>();
    }

    // MÉTODOS DE NEGOCIO (Solo para el Controlador)
    
    public boolean lanzarDado(String idJugador) {
        if (!juego.puedeLanzar(idJugador)) {
            return false;
        }

        int valor = dado.lanzar();
        Turno turno = juego.getTurno();
        turno.registrarTirada(valor);

        // Regla 4: Penalización por tres 6 seguidos
        if (turno.esTercerSeis()) {
            String idUltimaFicha = turno.obtenerUltimaFicha();
            if (idUltimaFicha != null) {
                Ficha ultimaFicha = buscarFicha(idUltimaFicha);
                juego.getTablero().regresarUltimaFicha(ultimaFicha);
            }
            juego.avanzarTurno(); // Pierde el turno por la penalización
        }

        notificarObservadores();
        return true;
    }

    public boolean moverFicha(String idJugador, String idFicha) {
        if (!juego.puedeMover(idJugador, idFicha)) {
            return false;
        }

        // Verificamos si la ficha elegida es legal para moverse
        List<String> movibles = reglasMovimiento.obtenerFichasMovibles(juego);
        if (!movibles.contains(idFicha)) {
            return false;
        }

        Turno turno = juego.getTurno();
        Ficha ficha = buscarFicha(idFicha);
        
        // 1. ¿Es una salida del círculo de inicio? (Regla 3)[cite: 2]
        if ("EN_BASE".equals(ficha.getEstado())) {
            juego.getTablero().sacarFicha(ficha);
            turno.registrarMovimiento(idFicha, ficha.getPosicion());
        } else {
            // 2. Movimiento normal
            int destinoCalculado = reglasMovimiento.calcularDestino(juego, idFicha);
            
            // 3. Revisar si hay una captura (Regla 5)[cite: 2]
            String idFichaCapturada = reglasMovimiento.obtenerFichaCapturable(juego, idFicha, destinoCalculado);
            if (idFichaCapturada != null) {
                Ficha fichaRival = buscarFicha(idFichaCapturada);
                juego.getTablero().capturarFicha(fichaRival);
                // Recompensa de 20 pasos[cite: 2]
                turno.agregarBonoCaptura(idFicha); 
            }

            // Movemos la ficha físicamente
            juego.getTablero().moverFicha(ficha, destinoCalculado);
            turno.registrarMovimiento(idFicha, destinoCalculado);

            // 4. Revisar si llegó a la meta (Regla 8)[cite: 2]
            if (reglasMovimiento.esLlegadaExacta(juego, idFicha, destinoCalculado)) {
                juego.getTablero().llevarAMeta(ficha);
                // Recompensa de 10 pasos[cite: 2]
                turno.agregarBonoMeta(idFicha); 
            }
        }

        // 5. Resolver qué sigue usando la clase Turno
        procesarFinDeAccion();

        notificarObservadores();
        return true;
    }
    
    private void procesarFinDeAccion() {
        Turno turno = juego.getTurno();
        String continuacion = turno.resolverContinuacion(); 

        if ("FIN_TURNO".equals(continuacion)) {
            juego.avanzarTurno();
        } else if ("ACCION_PENDIENTE".equals(continuacion)) {
            // Hay un bono de 10 o 20 pasos pendiente en la cola del turno
            turno.consumirAccion();
            turno.esperarMovimiento(); 
        } else if ("REPETIR_TIRADA".equals(continuacion)) {
            // Regla 4: Sacó un 6 y puede volver a tirar[cite: 2]
            turno.habilitaLanzamiento();
        }
    }
    
    private Ficha buscarFicha(String idFicha) {
        for (Jugador j : juego.getJugadores()) { // Asumiendo un getter en Juego
            for (Ficha f : j.obtenerFichas()) {
                if (f.getIdFicha().equals(idFicha)) {
                    return f;
                }
            }
        }
        return null;
    }

    // GESTIÓN DEL PATRÓN OBSERVER

    public void agregarObservador(IObservadorPartida observador) {
        this.observadores.add(observador);
    }

    public void eliminarObservador(IObservadorPartida observador) {
        this.observadores.remove(observador);
    }

    private void notificarObservadores() {
        for (IObservadorPartida obs : observadores) {
            obs.actualizar();
        }
    }
    
    // MÉTODOS DE SOLO LECTURA (Exclusivos para la Vista)

    @Override
    public int obtenerValorDado() {
        return juego.obtenerValorDado();
    }

    @Override
    public String obtenerJugadorActual() {
        return juego.obtenerJugadorActual() != null ? juego.obtenerJugadorActual().getIdJugador() : "N/A";
    }

    @Override
    public List<String> obtenerFichasMovibles() {
        return reglasMovimiento.obtenerFichasMovibles(juego);
    }

    @Override
    public Map<String, Integer> obtenerPosiciones() {
        return juego.obtenerPosiciones();
    }

    @Override
    public String obtenerEstadoJuego() {
        return juego.obtenerEstadoJuego();
    }
}
