package Presentacion;

import Presentacion.estilo.Estilo;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Ventana del juego. Muestra una pantalla a la vez con mostrar(pantalla).
 * No decide a qué pantalla ir: eso lo hace la lógica de cada CU.
 *
 * @author Equipo 4
 */
public class VentanaParchis extends JFrame {

    private static final long serialVersionUID = 1L;

    public VentanaParchis(String titulo) {
        super(titulo);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(Estilo.FONDO);
        setMinimumSize(new Dimension(640, 400));
        setSize(1216, 714); // 1200 x 675 de contenido + bordes de la ventana
        setLocationRelativeTo(null);
    }

    /** Cambia la pantalla que se ve. */
    public void mostrar(JComponent pantalla) {
        setContentPane(pantalla);
        revalidate();
        repaint();
    }
}
