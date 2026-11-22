package Presentacion;

import Presentacion.componentes.Avatar;

/**
 * Pantalla para personalizar el avatar (storyboard: "Pantalla para personalizar el avatar del jugador").
 * SIN lógica: los 12 avatares son botones; avatar.setMarca("1") marca el elegido.
 *
 * @author Equipo 4
 */
public class PantallaPersonalizarAvatar extends PantallaPersonalizacion {

    private static final long serialVersionUID = 1L;

    private static final int[] CENTROS_X = {356, 588, 818, 1013};
    private static final int[] CENTROS_Y = {174, 358, 545};
    private static final int DIAMETRO = 130;

    private final Avatar[] avatares = new Avatar[Avatar.TOTAL];

    public PantallaPersonalizarAvatar() {
        super("AVATAR", 74);
        for (int i = 0; i < Avatar.TOTAL; i++) {
            avatares[i] = new Avatar(i);
            int cx = CENTROS_X[i % 4], cy = CENTROS_Y[i / 4];
            agregar(avatares[i], cx - DIAMETRO / 2, cy - DIAMETRO / 2, DIAMETRO, DIAMETRO);
        }
    }

    /** Los 12 avatares, de izquierda a derecha y de arriba abajo. */
    public Avatar[] getAvatares() { return avatares; }
}
