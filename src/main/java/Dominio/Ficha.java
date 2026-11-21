package Dominio;

/**
 * Ficha de un jugador.
 *
 * Recorrido (Fig. 1 del documento del juego): la ficha sale en su casilla de
 * salida, avanza 63 casillas por el perímetro hasta su casilla de entrada
 * (68, 17, 34 o 51), entra a su pasillo de 7 casillas y llega a la meta.
 *
 * distanciaRecorrida: 0 = en su salida ... 63 = en su casilla de entrada,
 *                     64..70 = pasillo, 71 = meta.
 * posicion: -1 = en base, 1..68 = número de casilla del tablero,
 *           69..75 = casilla 1..7 de su pasillo, 76 = meta.
 *
 * @author Jesús
 */
public class Ficha {

    public static final int EN_BASE = -1;
    public static final int CASILLAS_TABLERO = 68;
    public static final int DISTANCIA_ENTRADA_PASILLO = 63; // salida -> casilla de entrada
    public static final int CASILLAS_PASILLO = 7;
    public static final int DISTANCIA_TOTAL_META = DISTANCIA_ENTRADA_PASILLO + CASILLAS_PASILLO + 1; // 71
    public static final int POSICION_META = 76;

    private final String idFicha;
    private final ColorJugador color;
    private int distanciaRecorrida;
    private String estado; // EN_BASE, ACTIVA, EN_META
    private int posicion;

    public Ficha(String idFicha, ColorJugador color) {
        this.idFicha = idFicha;
        this.color = color;
        this.estado = "EN_BASE";
        this.posicion = EN_BASE;
        this.distanciaRecorrida = 0;
    }

    public void activar(int salida) {
        this.estado = "ACTIVA";
        this.posicion = salida;
        this.distanciaRecorrida = 0;
    }

    public void actualizarPosicion(int destino) {
        this.posicion = destino;
    }

    public void setDistanciaRecorrida(int distanciaRecorrida) {
        this.distanciaRecorrida = distanciaRecorrida;
    }

    public void regresarABase() {
        this.estado = "EN_BASE";
        this.posicion = EN_BASE;
        this.distanciaRecorrida = 0;
    }

    public void llegarAMeta() {
        this.estado = "EN_META";
        this.distanciaRecorrida = DISTANCIA_TOTAL_META;
        this.posicion = POSICION_META;
    }

    public int calcularCasillasFaltantes() {
        if ("EN_META".equals(estado)) {
            return 0;
        }
        if ("EN_BASE".equals(estado)) {
            return DISTANCIA_TOTAL_META;
        }
        return DISTANCIA_TOTAL_META - distanciaRecorrida;
    }

    /** true si la ficha está en el perímetro (donde hay capturas y barreras). */
    public boolean estaEnPerimetro() {
        return "ACTIVA".equals(estado) && distanciaRecorrida <= DISTANCIA_ENTRADA_PASILLO;
    }

    public String getIdFicha() { return idFicha; }
    public int getDistanciaRecorrida() { return distanciaRecorrida; }
    public String getEstado() { return estado; }
    public int getPosicion() { return posicion; }
    public ColorJugador getColor() { return color; }
}
