package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

/**
 * Dado del storyboard: cuadro amarillo con el dado blanco y un piquito que
 * apunta al avatar del jugador. Es un JButton (clic = lanzar).
 * Valor 0 = todavía no se lanza (muestra "?").
 *
 * @author Equipo 4
 */
public class Dado extends JButton {

    private static final long serialVersionUID = 1L;

    private int valor;
    private boolean piquitoAbajo = true;
    private boolean destacado;   // cuando el jugador local puede lanzar

    public Dado() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setValor(int valor) {
        this.valor = valor;
        repaint();
    }

    public int getValor() {
        return valor;
    }

    /** true: el piquito apunta hacia abajo (avatar debajo); false: hacia arriba. */
    public void setPiquitoAbajo(boolean abajo) {
        this.piquitoAbajo = abajo;
        repaint();
    }

    public void setDestacado(boolean destacado) {
        this.destacado = destacado;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        double w = getWidth(), h = getHeight();
        double pico = h * 0.12;
        double lado = Math.min(w, h - pico);
        double x = (w - lado) / 2;
        double y = piquitoAbajo ? 0 : pico;

        if (destacado) {
            g.setColor(new Color(0xFF, 0xF0, 0x90, 90));
            g.fill(new RoundRectangle2D.Double(x - lado * 0.08, y - lado * 0.08, lado * 1.16, lado * 1.16, lado * 0.2, lado * 0.2));
        }
        // Marco amarillo con piquito
        g.setColor(Estilo.AMARILLO_DADO);
        g.fill(new java.awt.geom.Rectangle2D.Double(x, y, lado, lado));
        Path2D p = new Path2D.Double();
        double px = w / 2;
        if (piquitoAbajo) {
            p.moveTo(px - pico * 0.7, lado);
            p.lineTo(px + pico * 0.7, lado);
            p.lineTo(px, lado + pico);
        } else {
            p.moveTo(px - pico * 0.7, pico);
            p.lineTo(px + pico * 0.7, pico);
            p.lineTo(px, 0);
        }
        p.closePath();
        g.fill(p);

        // Dado blanco
        double m = lado * 0.14;
        double dl = lado - 2 * m;
        RoundRectangle2D dado = new RoundRectangle2D.Double(x + m, y + m, dl, dl, dl * 0.25, dl * 0.25);
        g.setColor(getModel().isPressed() ? new Color(0xE8, 0xE8, 0xE8) : new Color(0xF4, 0xF4, 0xF4));
        g.fill(dado);
        g.setColor(new Color(0, 0, 0, 60));
        g.setStroke(new BasicStroke((float) Math.max(1, lado * 0.02)));
        g.draw(dado);

        g.setColor(Color.BLACK);
        if (valor < 1 || valor > 6) {
            g.setFont(Estilo.fuenteNegrita((float) (dl * 0.6)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("?", (float) (x + m + dl / 2 - fm.stringWidth("?") / 2.0), (float) (y + m + dl / 2 + fm.getAscent() / 2.8));
        } else {
            double r = dl * 0.09;
            double[][] pos = puntos(valor);
            for (double[] pt : pos) {
                g.fill(new Ellipse2D.Double(x + m + dl * pt[0] - r, y + m + dl * pt[1] - r, 2 * r, 2 * r));
            }
        }
        g.dispose();
    }

    private static double[][] puntos(int v) {
        double a = 0.27, b = 0.5, c = 0.73;
        switch (v) {
            case 1: return new double[][]{{b, b}};
            case 2: return new double[][]{{a, a}, {c, c}};
            case 3: return new double[][]{{a, a}, {b, b}, {c, c}};
            case 4: return new double[][]{{a, a}, {c, a}, {a, c}, {c, c}};
            case 5: return new double[][]{{a, a}, {c, a}, {b, b}, {a, c}, {c, c}};
            default: return new double[][]{{a, a}, {c, a}, {a, b}, {c, b}, {a, c}, {c, c}};
        }
    }
}
