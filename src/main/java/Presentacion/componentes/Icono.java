package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import javax.swing.JComponent;

/**
 * Íconos sencillos dibujados con figuras (no usan imágenes).
 *
 * @author Equipo 4
 */
public class Icono extends JComponent {

    private static final long serialVersionUID = 1L;

    public enum Tipo { USUARIO, USUARIOS, AGREGAR_USUARIO, LAPIZ, CORONA, MEDALLA }

    private final Tipo tipo;
    private Color color;
    private int numero; // para MEDALLA

    public Icono(Tipo tipo, Color color) {
        this.tipo = tipo;
        this.color = color;
        setOpaque(false);
    }

    /** Medalla con su número de lugar (2 = plata, 3 = bronce, otro = gris). */
    public static Icono medalla(int lugar) {
        Icono i = new Icono(Tipo.MEDALLA, null);
        i.numero = lugar;
        return i;
    }

    public void setNumero(int numero) {
        this.numero = numero;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        double w = getWidth(), h = getHeight(), s = Math.min(w, h);
        g.translate((w - s) / 2, (h - s) / 2);
        float trazo = (float) Math.max(1.2, s * 0.09);
        g.setStroke(new BasicStroke(trazo, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(color);
        switch (tipo) {
            case USUARIO:
                persona(g, s * 0.5, s);
                break;
            case USUARIOS:
                persona(g, s * 0.38, s * 0.85);
                g.translate(s * 0.3, 0);
                persona(g, s * 0.38, s * 0.85);
                break;
            case AGREGAR_USUARIO:
                persona(g, s * 0.42, s * 0.9);
                g.draw(new Line2D.Double(s * 0.72, s * 0.42, s * 0.98, s * 0.42));
                g.draw(new Line2D.Double(s * 0.85, s * 0.29, s * 0.85, s * 0.55));
                break;
            case LAPIZ:
                Path2D p = new Path2D.Double();
                p.moveTo(s * 0.15, s * 0.85);
                p.lineTo(s * 0.22, s * 0.62);
                p.lineTo(s * 0.68, s * 0.16);
                p.lineTo(s * 0.84, s * 0.32);
                p.lineTo(s * 0.38, s * 0.78);
                p.closePath();
                g.setStroke(new BasicStroke((float) Math.max(1, s * 0.07)));
                g.draw(p);
                break;
            case CORONA:
                corona(g, s);
                break;
            case MEDALLA:
                medalla(g, s);
                break;
            default:
                break;
        }
        g.dispose();
    }

    private void persona(Graphics2D g, double ancho, double alto) {
        double cx = ancho * 0.9;
        double r = alto * 0.2;
        g.draw(new Ellipse2D.Double(cx - r, alto * 0.08, 2 * r, 2 * r));
        g.draw(new Arc2D.Double(cx - ancho * 0.75, alto * 0.55, ancho * 1.5, alto * 0.8, 0, 180, Arc2D.OPEN));
    }

    private void corona(Graphics2D g, double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(s * 0.1, s * 0.8);
        p.lineTo(s * 0.05, s * 0.3);
        p.lineTo(s * 0.3, s * 0.55);
        p.lineTo(s * 0.5, s * 0.15);
        p.lineTo(s * 0.7, s * 0.55);
        p.lineTo(s * 0.95, s * 0.3);
        p.lineTo(s * 0.9, s * 0.8);
        p.closePath();
        g.setPaint(new GradientPaint(0, 0, new Color(0xFF, 0xE0, 0x6A), 0, (float) s, new Color(0xC8, 0x8A, 0x10)));
        g.fill(p);
        g.setColor(new Color(0xB0, 0x10, 0x30));
        g.fill(new java.awt.geom.Rectangle2D.Double(s * 0.12, s * 0.66, s * 0.76, s * 0.14));
        g.setColor(new Color(0x6B, 0x45, 0x05));
        g.setStroke(new BasicStroke((float) (s * 0.03)));
        g.draw(p);
        g.setColor(Color.WHITE);
        for (double x : new double[]{0.05, 0.5, 0.95}) {
            double y = x == 0.5 ? 0.15 : 0.3;
            g.fill(new Ellipse2D.Double(s * x - s * 0.05, s * y - s * 0.05, s * 0.1, s * 0.1));
        }
    }

    private void medalla(Graphics2D g, double s) {
        Color metal = numero == 2 ? new Color(0xB8, 0xB8, 0xC0)
                : numero == 3 ? new Color(0xC2, 0x6A, 0x2A) : new Color(0x7A, 0x6E, 0x7E);
        // listón
        g.setColor(new Color(0x2B, 0x4C, 0xC8));
        Path2D l = new Path2D.Double();
        l.moveTo(s * 0.28, 0);
        l.lineTo(s * 0.45, s * 0.45);
        l.lineTo(s * 0.55, s * 0.45);
        l.lineTo(s * 0.72, 0);
        l.closePath();
        g.fill(l);
        g.setColor(new Color(0xD8, 0x25, 0x2B));
        g.fill(new java.awt.geom.Rectangle2D.Double(s * 0.45, 0, s * 0.1, s * 0.4));
        // disco
        g.setColor(metal);
        g.fill(new Ellipse2D.Double(s * 0.18, s * 0.36, s * 0.64, s * 0.64));
        g.setColor(metal.darker());
        g.setStroke(new BasicStroke((float) (s * 0.04)));
        g.draw(new Ellipse2D.Double(s * 0.18, s * 0.36, s * 0.64, s * 0.64));
        String t = String.valueOf(numero);
        g.setFont(Estilo.fuenteNegrita((float) (s * 0.42)).deriveFont(Font.PLAIN));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(t, (float) (s * 0.5 - fm.stringWidth(t) / 2.0), (float) (s * 0.68 + fm.getAscent() / 2.8));
    }
}
