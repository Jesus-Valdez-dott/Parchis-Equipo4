package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.Color;
import javax.swing.JButton;

/**
 * Aviso del lobby con avatar, texto y dos botones
 * (p. ej. "Mr. Aura quiere unirse a tu partida" - Aceptar / Rechazar).
 * Sirve para cualquier solicitud: se cambian el texto y los botones.
 * Si un botón no se necesita, se oculta con getBotonRechazar().setVisible(false).
 *
 * @author Equipo 4
 */
public class Notificacion extends Tarjeta {

    private static final long serialVersionUID = 1L;

    private final Avatar avatar;
    private final Etiqueta texto;
    private final Boton aceptar;
    private final Boton rechazar;

    public Notificacion() {
        super(223, 86, Estilo.TAN, 0);
        avatar = agregar(new Avatar(1), 3, 27, 32, 32);
        texto = agregar(new Etiqueta("", Etiqueta.Tipo.NEGRITA, 10.5f, Color.BLACK), 34, 18, 96, 50);
        aceptar = agregar(Boton.verde("Aceptar", 8, 11, true), 128, 11, 90, 31);
        rechazar = agregar(new Boton("Rechazar", Estilo.ROJO_RECHAZAR, Color.BLACK, 8, 11, true), 128, 48, 90, 31);
    }

    /** Cambia el contenido del aviso. */
    public void mostrar(int indiceAvatar, String mensaje, String textoAceptar, String textoRechazar) {
        avatar.setIndice(indiceAvatar);
        texto.setText("<html><center>" + mensaje + "</center></html>");
        aceptar.setText(textoAceptar);
        rechazar.setText(textoRechazar != null ? textoRechazar : "");
        rechazar.setVisible(textoRechazar != null);
        // con un solo botón (p. ej. "Confirmar" del CU Iniciar partida) se centra
        mover(aceptar, 128, textoRechazar != null ? 11 : 28, 90, 31);
        setVisible(true);
    }

    public JButton getBotonAceptar() { return aceptar; }
    public JButton getBotonRechazar() { return rechazar; }
}
