package Presentacion.componentes;

import Presentacion.estilo.Estilo;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JButton;

/**
 * Avatar redondo. Es un JButton (se le puede poner addActionListener para elegirlo).
 *
 * Si existe la imagen src/main/resources/avatares/avatarN.png (N = 1..12) se usa;
 * si no, se dibuja un avatar sencillo de ejemplo.
 *
 * @author Equipo 4
 */
public class Avatar extends JButton {

    private static final long serialVersionUID = 1L;

    public static final int TOTAL = 12;

    private static final Color[] FONDOS = {
        new Color(0xF6, 0xD3, 0x6B), new Color(0x8E, 0xC9, 0xF0), new Color(0xB9, 0xE4, 0x9C), new Color(0xF7, 0xB2, 0xC9),
        new Color(0xC9, 0xB6, 0xF2), new Color(0xF4, 0xA2, 0x6B), new Color(0x9E, 0xE0, 0xD2), new Color(0xF2, 0xE2, 0xB8),
        new Color(0xA9, 0xC4, 0xF5), new Color(0xF0, 0xC0, 0x9E), new Color(0xD7, 0xF0, 0x8C), new Color(0xE6, 0xB8, 0xE8)
    };
    private static final Color[] PIEL = {
        new Color(0xF5, 0xD0, 0xB0), new Color(0xE0, 0xAC, 0x82), new Color(0xB9, 0x7A, 0x55), new Color(0x7D, 0x4E, 0x33)
    };
    private static final Color[] PELO = {
        new Color(0x2B, 0x1B, 0x12), new Color(0x8A, 0x4B, 0x1E), new Color(0xE3, 0xB4, 0x4C),
        new Color(0xC4, 0x3B, 0x2B), new Color(0x4A, 0x3A, 0x8C), new Color(0x15, 0x15, 0x18)
    };
    private static final Color[] ROPA = {
        new Color(0x2F, 0x6F, 0xD1), new Color(0xD8, 0x25, 0x2B), new Color(0x1F, 0x9D, 0x55),
        new Color(0x7D, 0x3C, 0xB2), new Color(0xF0, 0x7C, 0x01), new Color(0x25, 0x2A, 0x40)
    };

    private int indice;              // 0..11
    private transient Image imagen;  // imagen opcional desde resources
    private String marca;            // texto encima (p. ej. "1" = elegido por el jugador 1)
    private Color anillo;            // borde de color (null = sin borde)
    private boolean resaltado;       // halo dorado (p. ej. jugador en turno)

    public Avatar(int indice) {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setIndice(indice);
    }

    public final void setIndice(int indice) {
        this.indice = Math.floorMod(indice, TOTAL);
        this.imagen = cargarImagen(this.indice + 1);
        repaint();
    }

    public int getIndice() {
        return indice;
    }

    public void setMarca(String marca) {
        this.marca = marca;
        repaint();
    }

    public void setAnillo(Color anillo) {
        this.anillo = anillo;
        repaint();
    }

    public void setResaltado(boolean resaltado) {
        this.resaltado = resaltado;
        repaint();
    }

    private static Image cargarImagen(int n) {
        try {
            URL url = Avatar.class.getResource("/avatares/avatar" + n + ".png");
            return url != null ? ImageIO.read(url) : null;
        } catch (java.io.IOException e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Estilo.suavizar(g);
        double s = Math.min(getWidth(), getHeight());
        double m = resaltado ? s * 0.07 : (anillo != null ? s * 0.04 : 0);
        double d = s - 2 * m;
        double ox = (getWidth() - s) / 2 + m, oy = (getHeight() - s) / 2 + m;

        if (resaltado) {
            for (int i = 3; i >= 1; i--) {
                g.setColor(new Color(0xFF, 0xD7, 0x4A, 60));
                double e = m * i / 3.0;
                g.fill(new Ellipse2D.Double(ox - e, oy - e, d + 2 * e, d + 2 * e));
            }
        }

        Shape circulo = new Ellipse2D.Double(ox, oy, d, d);
        Graphics2D gc = (Graphics2D) g.create();
        gc.clip(circulo);
        if (imagen != null) {
            gc.drawImage(imagen, (int) ox, (int) oy, (int) d, (int) d, null);
        } else {
            dibujarEjemplo(gc, ox, oy, d);
        }
        gc.dispose();

        if (anillo != null || resaltado) {
            g.setColor(resaltado ? new Color(0xFF, 0xD7, 0x4A) : anillo);
            g.setStroke(new BasicStroke((float) Math.max(1.5, s * 0.035)));
            g.draw(circulo);
        }
        if (marca != null) {
            g.setFont(Estilo.fuenteNegrita((float) (d * 0.36)));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Color.BLACK);
            g.drawString(marca, (float) (ox + d / 2 - fm.stringWidth(marca) / 2.0), (float) (oy + d / 2 + fm.getAscent() / 2.8));
        }
        g.dispose();
    }

    /** Personaje sencillo de ejemplo, distinto para cada índice. */
    private void dibujarEjemplo(Graphics2D g, double x, double y, double d) {
        int i = indice;
        g.setColor(FONDOS[i]);
        g.fill(new Ellipse2D.Double(x, y, d, d));

        Color piel = PIEL[i % PIEL.length];
        Color pelo = PELO[(i * 5 + 1) % PELO.length];
        Color ropa = ROPA[(i * 7 + 2) % ROPA.length];
        double cx = x + d / 2;

        // Hombros
        g.setColor(ropa);
        g.fill(new Ellipse2D.Double(cx - d * 0.38, y + d * 0.72, d * 0.76, d * 0.6));
        // Cuello
        g.setColor(piel.darker());
        g.fill(new RoundRectangle2D.Double(cx - d * 0.07, y + d * 0.6, d * 0.14, d * 0.16, d * 0.05, d * 0.05));

        int estilo = i % 4;
        // Pelo largo detrás de la cabeza
        if (estilo == 1) {
            g.setColor(pelo);
            g.fill(new RoundRectangle2D.Double(cx - d * 0.25, y + d * 0.2, d * 0.5, d * 0.55, d * 0.25, d * 0.25));
        }
        // Cabeza
        double rx = d * 0.21, ry = d * 0.24;
        double cy = y + d * 0.44;
        g.setColor(piel);
        g.fill(new Ellipse2D.Double(cx - rx, cy - ry, 2 * rx, 2 * ry));

        // Peinado / accesorio
        g.setColor(pelo);
        switch (estilo) {
            case 0: // corto
                g.fill(new Arc2D.Double(cx - rx * 1.05, cy - ry * 1.1, rx * 2.1, ry * 1.5, 0, 180, Arc2D.CHORD));
                break;
            case 1: // largo
                g.fill(new Arc2D.Double(cx - rx * 1.1, cy - ry * 1.15, rx * 2.2, ry * 1.4, 0, 180, Arc2D.CHORD));
                break;
            case 2: // chongo
                g.fill(new Ellipse2D.Double(cx - rx * 0.45, cy - ry * 1.65, rx * 0.9, rx * 0.9));
                g.fill(new Arc2D.Double(cx - rx * 1.05, cy - ry * 1.1, rx * 2.1, ry * 1.3, 0, 180, Arc2D.CHORD));
                break;
            default: // gorra
                g.setColor(ropa.darker());
                g.fill(new Arc2D.Double(cx - rx * 1.1, cy - ry * 1.2, rx * 2.2, ry * 1.5, 0, 180, Arc2D.CHORD));
                g.fill(new RoundRectangle2D.Double(cx - rx * 0.2, cy - ry * 0.5, rx * 1.5, ry * 0.22, ry * 0.2, ry * 0.2));
                break;
        }

        // Ojos y sonrisa
        g.setColor(new Color(0x22, 0x18, 0x14));
        double ojo = d * 0.045;
        g.fill(new Ellipse2D.Double(cx - rx * 0.45 - ojo / 2, cy - ojo / 2, ojo, ojo * 1.2));
        g.fill(new Ellipse2D.Double(cx + rx * 0.45 - ojo / 2, cy - ojo / 2, ojo, ojo * 1.2));
        g.setStroke(new BasicStroke((float) Math.max(1, d * 0.025), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D boca = new Path2D.Double();
        boca.moveTo(cx - rx * 0.4, cy + ry * 0.38);
        boca.quadTo(cx, cy + ry * 0.7, cx + rx * 0.4, cy + ry * 0.38);
        g.draw(boca);
        // Mejillas
        g.setColor(new Color(255, 120, 120, 70));
        g.fill(new Ellipse2D.Double(cx - rx * 0.8, cy + ry * 0.15, rx * 0.35, rx * 0.25));
        g.fill(new Ellipse2D.Double(cx + rx * 0.45, cy + ry * 0.15, rx * 0.35, rx * 0.25));
    }
}
