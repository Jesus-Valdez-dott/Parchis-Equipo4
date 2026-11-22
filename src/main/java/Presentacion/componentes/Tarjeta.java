package Presentacion.componentes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Rectángulo redondeado que contiene otros componentes en sus propias
 * coordenadas de diseño (0,0 = esquina de la tarjeta).
 *
 * @author Equipo 4
 */
public class Tarjeta extends PanelDiseno {

    private static final long serialVersionUID = 1L;

    private final int ancho;
    private final int alto;
    private Color fondo;
    private final int radio;
    private Color borde;
    private boolean punteado;
    private Color brillo;

    public Tarjeta(int anchoDiseno, int altoDiseno, Color fondo, int radio) {
        super(anchoDiseno, altoDiseno, null);
        this.ancho = anchoDiseno;
        this.alto = altoDiseno;
        this.fondo = fondo;
        this.radio = radio;
        setOpaque(false);
    }

    public Tarjeta conBorde(Color borde, boolean punteado) {
        this.borde = borde;
        this.punteado = punteado;
        repaint();
        return this;
    }

    public Tarjeta conBrillo(Color brillo) {
        this.brillo = brillo;
        repaint();
        return this;
    }

    public void setFondo(Color fondo) {
        this.fondo = fondo;
        repaint();
    }

    @Override
    protected void pintarFondo(Graphics2D g) {
        double m = brillo != null ? 4 : 1;
        RoundRectangle2D forma = new RoundRectangle2D.Double(m, m, ancho - 2 * m, alto - 2 * m, radio * 2.0, radio * 2.0);
        if (brillo != null) {
            for (int i = 3; i >= 1; i--) {
                g.setColor(new Color(brillo.getRed(), brillo.getGreen(), brillo.getBlue(), 40));
                double e = i * 1.3;
                g.fill(new RoundRectangle2D.Double(m - e, m - e, ancho - 2 * m + 2 * e, alto - 2 * m + 2 * e,
                        radio * 2.0 + e, radio * 2.0 + e));
            }
        }
        g.setColor(fondo);
        g.fill(forma);
        if (borde != null) {
            g.setColor(borde);
            g.setStroke(punteado
                    ? new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 3f}, 0f)
                    : new BasicStroke(2f));
            g.draw(forma);
        }
    }
}
