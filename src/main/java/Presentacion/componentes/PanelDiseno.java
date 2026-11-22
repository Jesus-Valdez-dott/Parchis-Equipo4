package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Panel base de las pantallas: fondo morado del storyboard y componentes
 * colocados en coordenadas de diseño (1200 x 675 = una pantalla del storyboard).
 *
 * Las subclases pueden dibujar decoraciones de fondo en pintarFondo(),
 * también en coordenadas de diseño.
 *
 * @author Equipo 4
 */
public class PanelDiseno extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final int ANCHO = 1200;
    public static final int ALTO = 675;

    private final LayoutEscalado layout;
    private final Color colorFondo;

    public PanelDiseno() {
        this(ANCHO, ALTO, Estilo.FONDO);
    }

    public PanelDiseno(int anchoDiseno, int altoDiseno, Color colorFondo) {
        this.layout = new LayoutEscalado(anchoDiseno, altoDiseno);
        this.colorFondo = colorFondo;
        setLayout(layout);
        setOpaque(colorFondo != null);
        if (colorFondo != null) {
            setBackground(colorFondo);
        }
    }

    /** Agrega un componente en la posición (x, y, ancho, alto) del storyboard. */
    public <T extends JComponent> T agregar(T componente, int x, int y, int ancho, int alto) {
        add(componente, new Rectangle(x, y, ancho, alto));
        return componente;
    }

    /** Mueve un componente ya agregado a otra posición del storyboard. */
    public void mover(JComponent componente, int x, int y, int ancho, int alto) {
        layout.mover(componente, new Rectangle(x, y, ancho, alto));
        revalidate();
        repaint();
    }

    public double getEscala() {
        return layout.getEscala();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        if (colorFondo != null) {
            g.setColor(colorFondo);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        g.translate(layout.getOrigenX(), layout.getOrigenY());
        g.scale(layout.getEscala(), layout.getEscala());
        pintarFondo(g);
        g.dispose();
    }

    /** Decoraciones de fondo, en coordenadas de diseño. */
    protected void pintarFondo(Graphics2D g) {
        // por defecto, nada
    }
}
