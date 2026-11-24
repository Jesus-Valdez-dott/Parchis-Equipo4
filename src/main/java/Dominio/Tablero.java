package Dominio;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tablero con 68 casillas numeradas y un pasillo de 7 casillas por color
 * que lleva a la meta.
 * Compone a las fichas, por eso sus operaciones reciben el idFicha.
 *
 * @author Jesús
 */
public class Tablero {

    /** Casillas seguras del perímetro, incluidas las cuatro salidas. */
    public static final Set<Integer> SEGURAS_POR_DEFECTO =
            Set.of(5, 12, 17, 22, 29, 34, 39, 46, 51, 56, 63, 68);

    private final List<Casilla> casillas;
    private final Map<String, Ficha> fichas;
    private Map<ColorJugador, Integer> casillasDeSalida;

    public Tablero() {
        this.casillas = new ArrayList<>();
        this.fichas = new LinkedHashMap<>();
        this.casillasDeSalida = new EnumMap<>(ColorJugador.class);
        casillasDeSalida.put(ColorJugador.AMARILLO, 5);
        casillasDeSalida.put(ColorJugador.AZUL, 22);
        casillasDeSalida.put(ColorJugador.ROJO, 39);
        casillasDeSalida.put(ColorJugador.VERDE, 56);
        for (int numero = 1; numero <= Ficha.CASILLAS_TABLERO; numero++) {
            casillas.add(new Casilla(numero, SEGURAS_POR_DEFECTO.contains(numero)));
        }
    }

    // ------------------------------------------------------------------
    // Registro de fichas y conversiones
    // ------------------------------------------------------------------

    public void registrarFichas(List<Ficha> fichasJugador) {
        for (Ficha f : fichasJugador) {
            fichas.put(f.getIdFicha(), f);
        }
    }

    public Ficha obtenerFicha(String idFicha) {
        return fichas.get(idFicha);
    }

    public int obtenerCasillaSalida(ColorJugador color) {
        return casillasDeSalida.getOrDefault(color, 0);
    }

    /**
     * Convierte la distancia recorrida de una ficha en su posición:
     * 1..68 número de casilla, 69..75 casilla de su pasillo, 76 meta.
     * Ej. amarillo: distancia 0 -> casilla 5, distancia 63 -> casilla 68.
     */
    public int calcularCasilla(ColorJugador color, int distancia) {
        if (distancia >= Ficha.DISTANCIA_TOTAL_META) {
            return Ficha.POSICION_META;
        }
        if (distancia > Ficha.DISTANCIA_ENTRADA_PASILLO) {
            return Ficha.CASILLAS_TABLERO + (distancia - Ficha.DISTANCIA_ENTRADA_PASILLO);
        }
        return ((obtenerCasillaSalida(color) - 1 + distancia) % Ficha.CASILLAS_TABLERO) + 1;
    }

    public boolean esCasillaSegura(int idCasilla) {
        return enPerimetro(idCasilla) && casilla(idCasilla).isSegura();
    }

    public List<Ficha> obtenerFichasEnCasilla(int idCasilla) {
        if (enPerimetro(idCasilla)) {
            return casilla(idCasilla).getFichasOcupantes();
        }
        return new ArrayList<>();
    }

    // ------------------------------------------------------------------
    // Operaciones del tablero
    // ------------------------------------------------------------------

    /**
     * Rechaza recorridos que cruzan una barrera, terminan en una casilla llena
     * o rebotan fuera del pasillo final.
     */
    public boolean validarMovimiento(String idFicha, int pasos) {
        Ficha ficha = fichas.get(idFicha);
        if (ficha == null || "EN_META".equals(ficha.getEstado())) {
            return false;
        }
        if ("EN_BASE".equals(ficha.getEstado())) {
            return !casilla(obtenerCasillaSalida(ficha.getColor())).estaLlena();
        }

        int inicio = ficha.getDistanciaRecorrida();
        int fin = Math.min(inicio + pasos, Ficha.DISTANCIA_TOTAL_META);
        for (int d = inicio + 1; d <= fin && d <= Ficha.DISTANCIA_ENTRADA_PASILLO; d++) {
            if (casilla(calcularCasilla(ficha.getColor(), d)).esBarrera()) {
                return false;
            }
        }

        int destino = calcularDistanciaDestino(inicio, pasos);
        if (destino <= Ficha.DISTANCIA_ENTRADA_PASILLO && inicio + pasos > Ficha.DISTANCIA_TOTAL_META) {
            return false; // el rebote no puede sacarla del pasillo
        }
        int casillaDestino = calcularCasilla(ficha.getColor(), destino);
        if (enPerimetro(casillaDestino)) {
            Casilla c = casilla(casillaDestino);
            // Se permite caer donde hay una sola ficha rival (captura o casilla segura compartida)
            return !c.estaLlena();
        }
        return true;
    }

    /** destino = nueva distancia recorrida (ya con rebote aplicado). */
    public void moverFicha(String idFicha, int destino) {
        Ficha ficha = fichas.get(idFicha);
        quitarDeCasilla(ficha);
        int casilla = calcularCasilla(ficha.getColor(), destino);
        ficha.setDistanciaRecorrida(destino);
        ficha.actualizarPosicion(casilla);
        if (enPerimetro(casilla)) {
            casilla(casilla).agregarFicha(ficha);
        }
    }

    public void sacarFicha(String idFicha) {
        Ficha ficha = fichas.get(idFicha);
        int salida = obtenerCasillaSalida(ficha.getColor());
        ficha.activar(salida);
        casilla(salida).agregarFicha(ficha);
    }

    public void capturarFicha(String idFicha) {
        Ficha victima = fichas.get(idFicha);
        quitarDeCasilla(victima);
        victima.regresarABase();
    }

    public void llevarAMeta(String idFicha) {
        Ficha ficha = fichas.get(idFicha);
        quitarDeCasilla(ficha);
        ficha.llegarAMeta();
    }

    /** Devuelve la ficha a la base, salvo que ya haya llegado a la meta. */
    public void regresarUltimaFicha(String idFicha) {
        Ficha ficha = fichas.get(idFicha);
        if (ficha == null || "EN_META".equals(ficha.getEstado())) {
            return;
        }
        quitarDeCasilla(ficha);
        ficha.regresarABase();
    }

    public List<String> obtenerFichasEnBarrera(String idJugador) {
        List<String> enBarrera = new ArrayList<>();
        String prefijo = idJugador + "_F";
        for (Casilla c : casillas) {
            if (c.esBarrera()) {
                for (Ficha f : c.getFichasOcupantes()) {
                    if (f.getIdFicha().startsWith(prefijo)) {
                        enBarrera.add(f.getIdFicha());
                    }
                }
            }
        }
        return enBarrera;
    }

    public void configurarSalidas(Map<ColorJugador, Integer> configuracion) {
        this.casillasDeSalida = new EnumMap<>(configuracion);
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Calcula la distancia final; los pasos que exceden la meta se recorren hacia atrás. */
    static int calcularDistanciaDestino(int distanciaActual, int pasos) {
        int nueva = distanciaActual + pasos;
        if (nueva > Ficha.DISTANCIA_TOTAL_META) {
            nueva = Ficha.DISTANCIA_TOTAL_META - (nueva - Ficha.DISTANCIA_TOTAL_META);
        }
        return nueva;
    }

    private void quitarDeCasilla(Ficha ficha) {
        if (enPerimetro(ficha.getPosicion())) {
            casilla(ficha.getPosicion()).quitarFicha(ficha);
        }
    }

    private boolean enPerimetro(int numeroCasilla) {
        return numeroCasilla >= 1 && numeroCasilla <= Ficha.CASILLAS_TABLERO;
    }

    private Casilla casilla(int numeroCasilla) {
        return casillas.get(numeroCasilla - 1);
    }
}
