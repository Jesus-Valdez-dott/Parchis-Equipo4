package Presentacion;

import Presentacion.componentes.CirculoColor;
import Presentacion.componentes.Etiqueta;
import Presentacion.componentes.Tarjeta;
import Presentacion.estilo.Estilo;
import java.awt.Color;

/**
 * Pantalla para personalizar el color (storyboard: "Pantalla para personalizar el color del jugador").
 * SIN lógica: los 12 colores son botones; circulo.setMarca("1") marca el elegido.
 *
 * @author Equipo 4
 */
public class PantallaPersonalizarColor extends PantallaPersonalizacion {

    private static final long serialVersionUID = 1L;

    private final CirculoColor[] circulos = new CirculoColor[Estilo.PALETA_JUGADOR.length];

    public PantallaPersonalizarColor() {
        super("Color", 78);
        Tarjeta panel = new Tarjeta(284, 460, Estilo.BLANCO_PANEL, 0);
        panel.agregar(new Etiqueta("Escoge un color", Etiqueta.Tipo.TEXTO, 29, Color.BLACK), 0, 25, 284, 42);
        int[] xs = {48, 142, 235};
        int[] ys = {110, 202, 286, 380};
        for (int i = 0; i < circulos.length; i++) {
            circulos[i] = new CirculoColor(Estilo.PALETA_JUGADOR[i]);
            panel.agregar(circulos[i], xs[i % 3] - 36, ys[i / 3] - 36, 73, 73);
        }
        agregar(panel, 544, 141, 284, 460);
    }

    /** Los 12 colores, de izquierda a derecha y de arriba abajo (ver Estilo.PALETA_JUGADOR). */
    public CirculoColor[] getCirculosColor() { return circulos; }
}
