package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

/**
 * Botón redondeado del storyboard (tan, verde, rojo, azul...).
 * Es un JButton normal: la lógica se conecta con addActionListener().
 *
 * @author Equipo 4
 */
public class Boton extends JButton implements Escalable {

    private static final long serialVersionUID = 1L;

    /** Radio especial: extremos totalmente redondos (forma de píldora). */
    public static final int PILDORA = -1;

    private Color fondo;
    private Color colorTexto;
    private final int radioDiseno;
    private final float tamanoLetra;
    private final boolean negrita;
    private Color brillo;       // halo alrededor del botón (null = sin halo)
    private double escala = 1;

    public Boton(String texto, Color fondo, Color colorTexto, int radioDiseno, float tamanoLetra, boolean negrita) {
        super(texto);
        this.fondo = fondo;
        this.colorTexto = colorTexto;
        this.radioDiseno = radioDiseno;
        this.tamanoLetra = tamanoLetra;
        this.negrita = negrita;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Botón tan con texto oscuro (los de la pantalla de inicio y personalización). */
    public static Boton tan(String texto, int radio, float tamanoLetra) {
        return new Boton(texto, Estilo.TAN, Estilo.TEXTO_OSCURO, radio, tamanoLetra, false);
    }

    public static Boton verde(String texto, int radio, float tamanoLetra, boolean negrita) {
        return new Boton(texto, Estilo.VERDE, Color.BLACK, radio, tamanoLetra, negrita);
    }

    public static Boton rojo(String texto, int radio, float tamanoLetra, boolean negrita) {
        return new Boton(texto, Estilo.ROJO, Color.BLACK, radio, tamanoLetra, negrita);
    }

    public void setBrillo(Color brillo) {
        this.brillo = brillo;
        repaint();
    }

    public void setFondo(Color fondo) {
        this.fondo = fondo;
        repaint();
    }

    public void setColorTexto(Color colorTexto) {
        this.colorTexto = colorTexto;
        repaint();
    }

    @Override
    public void escalar(double escala) {
        this.escala = escala;
        float t = (float) (tamanoLetra * escala);
        setFont(negrita ? Estilo.fuenteNegrita(t) : Estilo.fuenteTexto(t));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        if (!isEnabled()) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        }
        double margen = brillo != null ? 4 * escala : 0;
        double w = getWidth() - 2 * margen, h = getHeight() - 2 * margen;
        double r = radioDiseno == PILDORA ? h : radioDiseno * 2 * escala;

        if (brillo != null) {
            for (int i = 3; i >= 1; i--) {
                g.setColor(new Color(brillo.getRed(), brillo.getGreen(), brillo.getBlue(), 35));
                double e = margen * i / 3.0;
                g.fill(new RoundRectangle2D.Double(margen - e, margen - e, w + 2 * e, h + 2 * e, r + e, r + e));
            }
        }
        Color c = fondo;
        if (getModel().isPressed()) {
            c = c.darker();
        } else if (getModel().isRollover() && isEnabled()) {
            c = aclarar(c);
        }
        RoundRectangle2D forma = new RoundRectangle2D.Double(margen, margen, w, h, r, r);
        g.setColor(c);
        g.fill(forma);
        g.setColor(new Color(0, 0, 0, 40));
        g.setStroke(new BasicStroke(1f));
        g.draw(forma);

        String t = getText();
        Font f = Estilo.paraTexto(getFont(), t);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(colorTexto);
        g.drawString(t, (float) ((getWidth() - fm.stringWidth(t)) / 2.0),
                (float) ((getHeight() - fm.getHeight()) / 2.0 + fm.getAscent()));
        g.dispose();
    }

    private static Color aclarar(Color c) {
        return new Color(Math.min(255, c.getRed() + 18), Math.min(255, c.getGreen() + 18), Math.min(255, c.getBlue() + 18));
    }
}
