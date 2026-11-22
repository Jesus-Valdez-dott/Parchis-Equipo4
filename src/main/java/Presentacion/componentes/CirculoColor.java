package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import javax.swing.JButton;

/**
 * Círculo de color seleccionable (pantalla "Color"). Es un JButton.
 * setMarca("1") muestra el número del jugador que lo eligió.
 *
 * @author Equipo 4
 */
public class CirculoColor extends JButton {

    private static final long serialVersionUID = 1L;

    private final Color color;
    private String marca;

    public CirculoColor(Color color) {
        this.color = color;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public Color getColor() {
        return color;
    }

    public void setMarca(String marca) {
        this.marca = marca;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        double s = Math.min(getWidth(), getHeight());
        double b = Math.max(2, s * 0.045);
        Ellipse2D c = new Ellipse2D.Double((getWidth() - s) / 2 + b / 2, (getHeight() - s) / 2 + b / 2, s - b, s - b);
        g.setColor(getModel().isRollover() ? color.brighter() : color);
        g.fill(c);
        g.setColor(new Color(0x77, 0x77, 0x77));
        g.setStroke(new BasicStroke((float) b));
        g.draw(c);
        if (marca != null) {
            g.setFont(Estilo.fuenteNegrita((float) (s * 0.6)));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Color.BLACK);
            g.drawString(marca, (float) (getWidth() / 2.0 - fm.stringWidth(marca) / 2.0),
                    (float) (getHeight() / 2.0 + fm.getAscent() / 2.8));
        }
        g.dispose();
    }
}
